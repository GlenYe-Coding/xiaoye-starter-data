package com.xiaoye.starter.data.interceptor;

import com.xiaoye.starter.data.interceptor.TenantContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * 多租户数据隔离拦截器
 * <p>
 * 自动在SQL中添加 tenant_id 条件，实现数据隔离
 * </p>
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class TenantInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private static final String TENANT_COLUMN = "tenant_id";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object param = args[1];

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return invocation.proceed();
        }

        BoundSql boundSql = ms.getBoundSql(param);
        String originalSql = boundSql.getSql();
        String modifiedSql = addTenantCondition(originalSql);

        if (!originalSql.equals(modifiedSql)) {
            BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), modifiedSql, boundSql.getParameterMappings(), param);
            copyAdditionalParameters(boundSql, newBoundSql);

            MappedStatement newMs = newMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
            args[0] = newMs;
            log.debug("SQL added tenant condition: tenantId={}", tenantId);
        }

        return invocation.proceed();
    }

    /**
     * 添加租户条件
     */
    private String addTenantCondition(String sql) {
        String upperSql = sql.toUpperCase().trim();

        if (upperSql.startsWith("INSERT")) {
            return sql;
        }

        if (upperSql.startsWith("SELECT")) {
            if (!upperSql.contains(TENANT_COLUMN.toUpperCase())) {
                if (upperSql.contains("WHERE")) {
                    return sql + " AND " + TENANT_COLUMN + " = " + TenantContext.getTenantId();
                } else if (upperSql.contains("GROUP BY")) {
                    int groupByIndex = upperSql.indexOf("GROUP BY");
                    return sql.substring(0, groupByIndex) + " WHERE " + TENANT_COLUMN + " = " + TenantContext.getTenantId() + " " + sql.substring(groupByIndex);
                }
            }
        }

        return sql;
    }

    private void copyAdditionalParameters(BoundSql source, BoundSql target) {
        try {
            Field additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
            Object additionalParameters = additionalParametersField.get(source);
            additionalParametersField.set(target, additionalParameters);
        } catch (Exception e) {
            log.warn("Failed to copy additional parameters", e);
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