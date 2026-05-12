package com.xiaoye.starter.data.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * SQL注入防御拦截器
 * 检测并阻止常见的SQL注入攻击
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class SqlInjectionInterceptor implements Interceptor {

    // SQL注入常见危险关键字
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|CREATE|EXEC)\\b.*(--|;|/\\*|\\*/))|" +
            "(?i)(\\b(OR|AND)\\b\\s+\\d+\\s*=\\s*\\d+)"
    );

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        String sql = statementHandler.getBoundSql().getSql();

        // 检查SQL是否包含注入特征
        if (SQL_INJECTION_PATTERN.matcher(sql).matches()) {
            log.error("检测到潜在的SQL注入攻击: {}", sql);
            throw new SecurityException("SQL注入检测：非法SQL语句");
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可通过配置文件设置属性
    }
}
