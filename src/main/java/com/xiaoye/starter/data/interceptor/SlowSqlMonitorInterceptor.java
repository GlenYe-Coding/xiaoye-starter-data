package com.xiaoye.starter.data.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

/**
 * 慢SQL监控拦截器
 * 记录执行时间超过阈值的SQL语句
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class SlowSqlMonitorInterceptor implements Interceptor {

    @Value("${mybatis.slow-sql-threshold:1000}")
    private Long slowSqlThreshold; // 慢SQL阈值（毫秒）

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return invocation.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > slowSqlThreshold) {
                StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
                String sql = statementHandler.getBoundSql().getSql();
                
                log.warn("慢SQL检测 - 执行时间: {}ms, SQL: {}", executionTime, formatSql(sql));
                
                // TODO: 可将慢SQL记录到数据库或监控系统
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可通过配置文件设置属性
    }

    /**
     * 格式化SQL（去除多余空格和换行）
     */
    private String formatSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }
}
