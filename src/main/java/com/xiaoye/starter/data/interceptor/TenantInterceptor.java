package com.xiaoye.starter.data.interceptor;

import com.xiaoye.starter.data.permission.DataPermission;
import com.xiaoye.starter.data.permission.DataPermissionContext;
import com.xiaoye.starter.data.permission.DataPermissionRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 多租户数据隔离拦截器（增强版）
 * <p>
 * 支持三种租户隔离模式：
 * 1. Schema 隔离模式 - 不同租户使用不同数据库 Schema
 * 2. 字段隔离模式 - 同一表中通过 tenant_id 字段隔离（默认）
 * 3. 组合权限模式 - 结合数据权限和租户隔离
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class TenantInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

    /**
     * 租户字段名
     */
    private String tenantColumn = "tenant_id";

    /**
     * 隔离模式
     */
    private IsolationMode isolationMode = IsolationMode.COLUMN;

    /**
     * 排除的表
     */
    private Set<String> excludeTables = new HashSet<>(Arrays.asList(
        "sys_user", "sys_role", "sys_permission", "sys_dict", "sys_config"
    ));

    /**
     * 排除的 Mapper ID 前缀
     */
    private Set<String> excludeMapperPrefixes = new HashSet<>(Arrays.asList(
        "com.xiaoye.starter.data"
    ));

    /**
     * 数据权限规则列表
     */
    private List<DataPermissionRule> dataPermissionRules = new ArrayList<>();

    /**
     * 租户隔离模式枚举
     */
    public enum IsolationMode {
        /**
         * Schema 隔离模式
         */
        SCHEMA,
        /**
         * 字段隔离模式（默认）
         */
        COLUMN,
        /**
         * 组合权限模式（租户+数据权限）
         */
        COMBINED
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object param = args[1];

        // 检查是否应该排除
        if (shouldExclude(ms)) {
            return invocation.proceed();
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            // 租户ID为空，不进行隔离
            return invocation.proceed();
        }

        BoundSql boundSql = ms.getBoundSql(param);
        String originalSql = boundSql.getSql();
        String modifiedSql = originalSql;

        switch (isolationMode) {
            case SCHEMA:
                modifiedSql = applySchemaIsolation(originalSql, tenantId);
                break;
            case COLUMN:
                modifiedSql = addTenantCondition(originalSql, tenantId);
                break;
            case COMBINED:
                modifiedSql = addTenantCondition(originalSql, tenantId);
                // 添加数据权限条件
                modifiedSql = addDataPermissionCondition(modifiedSql, ms);
                break;
        }

        if (!originalSql.equals(modifiedSql)) {
            BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), modifiedSql,
                boundSql.getParameterMappings(), param);
            copyAdditionalParameters(boundSql, newBoundSql);

            MappedStatement newMs = newMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
            args[0] = newMs;
            logger.debug("Tenant isolation applied: tenantId={}, mode={}", tenantId, isolationMode);
        }

        return invocation.proceed();
    }

    /**
     * 判断是否应该排除
     */
    private boolean shouldExclude(MappedStatement ms) {
        // 检查表名
        String resource = ms.getResource();
        if (resource != null) {
            for (String excludeTable : excludeTables) {
                if (resource.toLowerCase().contains(excludeTable.toLowerCase())) {
                    return true;
                }
            }
        }

        // 检查 Mapper ID
        String mapperId = ms.getId();
        for (String prefix : excludeMapperPrefixes) {
            if (mapperId.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Schema 隔离模式
     */
    private String applySchemaIsolation(String sql, Long tenantId) {
        // Schema 隔离需要在数据源层面处理，这里仅做日志记录
        logger.debug("Schema isolation mode - tenant: {}", tenantId);
        TenantContext.setTenantId(tenantId);
        return sql;
    }

    /**
     * 添加租户条件
     */
    private String addTenantCondition(String sql, Long tenantId) {
        String upperSql = sql.trim().toUpperCase();

        // INSERT 语句不添加条件，但需要在值中添加 tenant_id
        if (upperSql.startsWith("INSERT")) {
            return sql;
        }

        if (upperSql.startsWith("SELECT")) {
            // 检查是否已经包含租户字段
            String upperColumn = tenantColumn.toUpperCase();
            if (!upperSql.contains(upperColumn)) {
                if (upperSql.contains("WHERE")) {
                    return sql + " AND " + tenantColumn + " = " + tenantId;
                } else if (upperSql.contains("GROUP BY")) {
                    int groupByIndex = upperSql.indexOf("GROUP BY");
                    return sql.substring(0, groupByIndex) + " WHERE " + tenantColumn + " = " + tenantId + " " + sql.substring(groupByIndex);
                } else if (upperSql.contains("ORDER BY")) {
                    int orderByIndex = upperSql.indexOf("ORDER BY");
                    return sql.substring(0, orderByIndex) + " WHERE " + tenantColumn + " = " + tenantId + " " + sql.substring(orderByIndex);
                } else if (upperSql.contains("LIMIT")) {
                    int limitIndex = upperSql.indexOf("LIMIT");
                    return sql.substring(0, limitIndex) + " WHERE " + tenantColumn + " = " + tenantId + " " + sql.substring(limitIndex);
                } else {
                    return sql + " WHERE " + tenantColumn + " = " + tenantId;
                }
            }
        }

        return sql;
    }

    /**
     * 添加数据权限条件
     */
    private String addDataPermissionCondition(String sql, MappedStatement ms) {
        // 检查是否有数据权限注解
        DataPermission annotation = getDataPermissionAnnotation(ms);
        if (annotation == null || !annotation.enabled()) {
            return sql;
        }

        // 获取匹配的规则
        String tableName = annotation.table();
        DataPermissionRule matchedRule = dataPermissionRules.stream()
            .filter(rule -> rule.match(tableName))
            .findFirst()
            .orElse(null);

        if (matchedRule == null) {
            return sql;
        }

        String alias = getTableAlias(sql, tableName);
        String condition = matchedRule.getCondition(tableName, alias);

        if (condition == null || condition.isEmpty()) {
            return sql;
        }

        return addCondition(sql, condition);
    }

    /**
     * 添加条件到 SQL
     */
    private String addCondition(String sql, String condition) {
        String upperSql = sql.trim().toUpperCase();

        if (upperSql.contains("WHERE")) {
            int whereIndex = upperSql.lastIndexOf("WHERE");
            return sql.substring(0, whereIndex + 5) + " " + condition + " AND " + sql.substring(whereIndex + 5).trim();
        } else if (upperSql.contains("GROUP BY")) {
            int groupByIndex = upperSql.indexOf("GROUP BY");
            return sql.substring(0, groupByIndex) + " WHERE " + condition + " " + sql.substring(groupByIndex);
        } else if (upperSql.contains("ORDER BY")) {
            int orderByIndex = upperSql.indexOf("ORDER BY");
            return sql.substring(0, orderByIndex) + " WHERE " + condition + " " + sql.substring(orderByIndex);
        } else if (upperSql.contains("LIMIT")) {
            int limitIndex = upperSql.indexOf("LIMIT");
            return sql.substring(0, limitIndex) + " WHERE " + condition + " " + sql.substring(limitIndex);
        } else {
            return sql + " WHERE " + condition;
        }
    }

    /**
     * 获取数据权限注解
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement ms) {
        try {
            Class<?> mapperClass = Class.forName(ms.getNamespace());
            String methodName = ms.getId().contains(".") ? ms.getId().substring(ms.getId().lastIndexOf(".") + 1) : ms.getId();

            for (java.lang.reflect.Method method : mapperClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    DataPermission annotation = method.getAnnotation(DataPermission.class);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        } catch (Exception e) {
            logger.trace("Failed to get DataPermission annotation", e);
        }
        return null;
    }

    /**
     * 获取表别名
     */
    private String getTableAlias(String sql, String tableName) {
        String upperSql = sql.toUpperCase();
        String upperTableName = tableName.toUpperCase();

        int fromIndex = upperSql.indexOf("FROM");
        if (fromIndex == -1) {
            return tableName;
        }

        String fromPart = sql.substring(fromIndex);
        int tableIndex = fromPart.toUpperCase().indexOf(upperTableName);

        if (tableIndex == -1) {
            return tableName;
        }

        int afterTable = tableIndex + upperTableName.length();
        if (afterTable < fromPart.length()) {
            String rest = fromPart.substring(afterTable).trim();
            if (rest.startsWith("AS")) {
                rest = rest.substring(2).trim();
            }
            int spaceIndex = rest.indexOf(' ');
            int commaIndex = rest.indexOf(',');
            int endIndex = rest.length();

            if (spaceIndex > 0) {
                endIndex = Math.min(endIndex, spaceIndex);
            }
            if (commaIndex > 0 && commaIndex < endIndex) {
                endIndex = commaIndex;
            }

            String alias = rest.substring(0, endIndex).trim();
            if (!alias.isEmpty() && !isKeyword(alias)) {
                return alias;
            }
        }

        return tableName;
    }

    /**
     * 检查是否为 SQL 关键字
     */
    private boolean isKeyword(String word) {
        String[] keywords = {"JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "WHERE",
            "AND", "OR", "GROUP", "ORDER", "BY", "LIMIT", "OFFSET", "AS", ","};
        for (String keyword : keywords) {
            if (keyword.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    private void copyAdditionalParameters(BoundSql source, BoundSql target) {
        try {
            Field additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
            Object additionalParameters = additionalParametersField.get(source);
            additionalParametersField.set(target, additionalParameters);
        } catch (Exception e) {
            logger.warn("Failed to copy additional parameters", e);
        }
    }

    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null) {
            builder.keyProperty(String.join(",", ms.getKeyProperties()));
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String column = properties.getProperty("tenantColumn", "tenant_id");
        if (column != null && !column.isEmpty()) {
            this.tenantColumn = column;
        }

        String mode = properties.getProperty("isolationMode", "COLUMN");
        try {
            this.isolationMode = IsolationMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid isolation mode: {}, using default COLUMN", mode);
        }

        String excludeTableStr = properties.getProperty("excludeTables");
        if (excludeTableStr != null && !excludeTableStr.isEmpty()) {
            this.excludeTables = new HashSet<>(Arrays.asList(excludeTableStr.split(",")));
        }
    }

    // ==================== Getters and Setters ====================

    public void setTenantColumn(String tenantColumn) {
        this.tenantColumn = tenantColumn;
    }

    public void setIsolationMode(IsolationMode isolationMode) {
        this.isolationMode = isolationMode;
    }

    public void setExcludeTables(Set<String> excludeTables) {
        this.excludeTables = excludeTables;
    }

    public void setExcludeMapperPrefixes(Set<String> excludeMapperPrefixes) {
        this.excludeMapperPrefixes = excludeMapperPrefixes;
    }

    public void setDataPermissionRules(List<DataPermissionRule> dataPermissionRules) {
        this.dataPermissionRules = dataPermissionRules;
    }

    /**
     * 内部类：包装 BoundSql
     */
    static class BoundSqlSqlSource implements SqlSource {
        private final BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}