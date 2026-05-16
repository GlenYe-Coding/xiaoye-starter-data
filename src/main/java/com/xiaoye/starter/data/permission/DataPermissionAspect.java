package com.xiaoye.starter.data.permission;

import com.xiaoye.starter.data.interceptor.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 数据权限切面
 * <p>
 * 通过 MyBatis 拦截器自动为 SQL 添加数据权限过滤条件
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataPermissionAspect implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DataPermissionAspect.class);

    private List<DataPermissionRule> rules = new ArrayList<>();

    public DataPermissionAspect() {
    }

    public DataPermissionAspect(List<DataPermissionRule> rules) {
        this.rules = rules;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) getTarget(invocation);
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();

        // 获取 Mapper 方法上的注解
        MappedStatement ms = getMappedStatement(statementHandler);
        if (ms == null) {
            return invocation.proceed();
        }

        DataPermission annotation = getDataPermissionAnnotation(ms);
        if (annotation == null || !annotation.enabled()) {
            return invocation.proceed();
        }

        // 检查是否为管理员
        if (DataPermissionContext.checkAdmin()) {
            return invocation.proceed();
        }

        // 匹配规则
        DataPermissionRule matchedRule = null;
        for (DataPermissionRule rule : rules) {
            if (rule.match(annotation.table())) {
                matchedRule = rule;
                break;
            }
        }

        // 如果注解指定了类型，使用对应规则
        if (matchedRule == null) {
            matchedRule = createDefaultRule(annotation);
        }

        if (matchedRule == null) {
            return invocation.proceed();
        }

        // 生成权限条件
        String tableName = annotation.table();
        String condition = matchedRule.getCondition(tableName, getTableAlias(originalSql, tableName));

        if (condition == null || condition.isEmpty()) {
            return invocation.proceed();
        }

        // 修改 SQL
        String modifiedSql = addPermissionCondition(originalSql, condition);

        if (!originalSql.equals(modifiedSql)) {
            // 替换 BoundSql - 使用反射获取 Configuration
            Object config = getFieldValue(statementHandler, "configuration");
            BoundSql newBoundSql = new BoundSql(
                (org.apache.ibatis.session.Configuration) config,
                modifiedSql,
                boundSql.getParameterMappings(),
                boundSql.getParameterObject()
            );
            copyAdditionalParameters(boundSql, newBoundSql);

            // 使用反射设置新的 BoundSql
            setFieldValue(statementHandler, "boundSql", newBoundSql);

            log.debug("SQL added permission condition: table={}, condition={}", tableName, condition);
        }

        return invocation.proceed();
    }

    /**
     * 创建默认规则
     */
    private DataPermissionRule createDefaultRule(DataPermission annotation) {
        DataPermission.Type type = annotation.type();
        switch (type) {
            case DEPARTMENT:
                return new DepartmentPermissionRule();
            case REGION:
                return new RegionPermissionRule();
            case ALL:
                // 管理员权限，不添加条件
                return new DataPermissionRule() {
                    @Override
                    public String getName() {
                        return "admin";
                    }

                    @Override
                    public String[] getTables() {
                        return new String[0];
                    }

                    @Override
                    public String getColumn(String tableName) {
                        return null;
                    }

                    @Override
                    public String getCondition(String tableName, String alias) {
                        return null;
                    }

                    @Override
                    public Object[] getPermissionValues(String column) {
                        return null;
                    }
                };
            default:
                return null;
        }
    }

    /**
     * 添加权限条件到 SQL
     */
    private String addPermissionCondition(String sql, String condition) {
        String upperSql = sql.trim().toUpperCase();

        if (upperSql.contains("WHERE")) {
            // 已有 WHERE 条件，追加 AND
            int whereIndex = upperSql.lastIndexOf("WHERE");
            return sql.substring(0, whereIndex + 5) + " " + condition + " AND " + sql.substring(whereIndex + 5).trim();
        } else if (upperSql.contains("GROUP BY")) {
            // 有 GROUP BY，在 GROUP BY 前添加 WHERE
            int groupByIndex = upperSql.indexOf("GROUP BY");
            return sql.substring(0, groupByIndex) + " WHERE " + condition + " " + sql.substring(groupByIndex);
        } else if (upperSql.contains("ORDER BY")) {
            // 有 ORDER BY，在 ORDER BY 前添加 WHERE
            int orderByIndex = upperSql.indexOf("ORDER BY");
            return sql.substring(0, orderByIndex) + " WHERE " + condition + " " + sql.substring(orderByIndex);
        } else if (upperSql.contains("LIMIT")) {
            // 有 LIMIT，在 LIMIT 前添加 WHERE
            int limitIndex = upperSql.indexOf("LIMIT");
            return sql.substring(0, limitIndex) + " WHERE " + condition + " " + sql.substring(limitIndex);
        } else {
            // 无 WHERE，直接添加 WHERE
            return sql + " WHERE " + condition;
        }
    }

    /**
     * 获取表别名
     */
    private String getTableAlias(String sql, String tableName) {
        // 简单实现，实际场景可能更复杂
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
            char nextChar = fromPart.charAt(afterTable);
            if (nextChar == ' ' || nextChar == '\n' || nextChar == '\t') {
                // 查找别名
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
            } else if (nextChar == ',' || nextChar == 'J' || nextChar == 'O' || nextChar == 'W' || nextChar == 'G') {
                // 紧跟 JOIN、ON、WHERE、GROUP 等关键字，说明无别名
            } else {
                // 可能是别名
                String rest = fromPart.substring(afterTable).trim();
                int spaceIndex = rest.indexOf(' ');
                if (spaceIndex > 0 && spaceIndex < 20) {
                    String potentialAlias = rest.substring(0, spaceIndex).trim();
                    if (!isKeyword(potentialAlias)) {
                        return potentialAlias;
                    }
                }
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

    /**
     * 获取 MappedStatement
     */
    private MappedStatement getMappedStatement(StatementHandler handler) {
        try {
            Object metaObject = handler;
            while (metaObject != null) {
                if (Proxy.isProxyClass(metaObject.getClass())) {
                    metaObject = getFieldValue(metaObject, "h");
                }
                MappedStatement ms = (MappedStatement) getFieldValue(metaObject, "mappedStatement");
                if (ms != null) {
                    return ms;
                }
                metaObject = getFieldValue(metaObject, "target");
            }
        } catch (Exception e) {
            log.trace("Failed to get MappedStatement", e);
        }
        return null;
    }

    /**
     * 获取数据权限注解
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement ms) {
        try {
            // 使用反射获取 namespace
            String namespace = (String) getFieldValue(ms, "namespace");
            if (namespace == null) {
                return null;
            }
            Class<?> mapperClass = Class.forName(namespace);
            Method method = findMethod(mapperClass, ms.getId());
            if (method != null) {
                DataPermission annotation = method.getAnnotation(DataPermission.class);
                if (annotation != null) {
                    return annotation;
                }
                // 查找类上的注解
                annotation = mapperClass.getAnnotation(DataPermission.class);
                if (annotation != null) {
                    return annotation;
                }
            }
        } catch (Exception e) {
            log.trace("Failed to get DataPermission annotation", e);
        }
        return null;
    }

    /**
     * 查找方法
     */
    private Method findMethod(Class<?> clazz, String methodId) {
        String methodName = methodId.contains(".") ? methodId.substring(methodId.lastIndexOf(".") + 1) : methodId;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 获取目标对象
     */
    private Object getTarget(Invocation invocation) {
        Object target = invocation.getTarget();
        if (target instanceof StatementHandler) {
            return target;
        }
        return target;
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置字段值
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            log.warn("Failed to set field value: {}", fieldName, e);
        }
    }

    /**
     * 复制额外参数
     */
    private void copyAdditionalParameters(BoundSql source, BoundSql target) {
        try {
            java.lang.reflect.Field additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
            Object additionalParameters = additionalParametersField.get(source);
            additionalParametersField.set(target, additionalParameters);
        } catch (Exception e) {
            log.warn("Failed to copy additional parameters", e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * 添加权限规则
     */
    public void addRule(DataPermissionRule rule) {
        this.rules.add(rule);
    }

    /**
     * 设置权限规则列表
     */
    public void setRules(List<DataPermissionRule> rules) {
        this.rules = rules;
    }
}