package com.xiaoye.starter.data.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Properties;

/**
 * 慢SQL监控拦截器
 * <p>
 * P1 功能：
 * - 记录执行超过阈值的SQL语句
 * - 输出警告日志供运维分析
 * </p>
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SlowSqlInterceptor implements Interceptor {

    /**
     * 默认慢SQL阈值（毫秒）
     */
    private static final long DEFAULT_SLOW_SQL_THRESHOLD = 500;

    /**
     * 默认告警阈值（毫秒）
     */
    private static final long DEFAULT_ALERT_THRESHOLD = 3000;

    private long slowSqlThreshold = DEFAULT_SLOW_SQL_THRESHOLD;
    private long alertThreshold = DEFAULT_ALERT_THRESHOLD;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;

        try {
            result = invocation.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            logSlowSql(invocation, executionTime, exception);
        }
    }

    /**
     * 记录慢SQL
     */
    private void logSlowSql(Invocation invocation, long executionTime, Throwable exception) {
        if (executionTime < slowSqlThreshold) {
            return;
        }

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object param = args[1];
        BoundSql boundSql = ms.getBoundSql(param);

        String sql = boundSql.getSql();
        String methodName = ms.getId();

        if (executionTime >= alertThreshold) {
            log.error("[SLOW_SQL_ALERT] executionTime: {}ms, method: {}, SQL: {}, params: {}",
                    executionTime, methodName, sql, formatParams(param));
        } else {
            log.warn("[SLOW_SQL_LOG] executionTime: {}ms, method: {}, SQL: {}, params: {}",
                    executionTime, methodName, sql, formatParams(param));
        }

        if (exception != null) {
            log.error("Slow SQL execution exception: method={}, time={}ms, exception={}",
                    methodName, executionTime, exception.getMessage());
        }
    }

    /**
     * 格式化参数
     */
    private String formatParams(Object param) {
        if (param == null) {
            return "null";
        }
        if (param instanceof String || param instanceof Number) {
            return param.toString();
        }
        return param.getClass().getSimpleName();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String slowThreshold = properties.getProperty("slowSqlThreshold");
        if (slowThreshold != null) {
            this.slowSqlThreshold = Long.parseLong(slowThreshold);
        }

        String alertThreshold = properties.getProperty("alertThreshold");
        if (alertThreshold != null) {
            this.alertThreshold = Long.parseLong(alertThreshold);
        }

        log.info("Slow SQL monitoring enabled: slowSqlThreshold={}ms, alertThreshold={}ms", slowSqlThreshold, alertThreshold);
    }

    public void setSlowSqlThreshold(long slowSqlThreshold) {
        this.slowSqlThreshold = slowSqlThreshold;
    }

    public void setAlertThreshold(long alertThreshold) {
        this.alertThreshold = alertThreshold;
    }
}