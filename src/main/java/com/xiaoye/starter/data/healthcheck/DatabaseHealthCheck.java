package com.xiaoye.starter.data.healthcheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库健康检查服务
 */
@Slf4j
@Component
public class DatabaseHealthCheck {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 检查数据库连接
     */
    public boolean checkConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("数据库连接正常");
            return true;
        } catch (Exception e) {
            log.error("数据库连接异常", e);
            return false;
        }
    }

    /**
     * 获取数据库版本
     */
    public String getDatabaseVersion() {
        try {
            return jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
