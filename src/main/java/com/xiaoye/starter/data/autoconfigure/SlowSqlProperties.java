package com.xiaoye.starter.data.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

/**
 * 慢SQL监控配置属性
 */
@Validated
@ConfigurationProperties(prefix = "xiaoye.data.slow-sql")
public class SlowSqlProperties {

    /**
     * 是否启用慢SQL监控
     */
    private boolean enabled = true;

    /**
     * 慢SQL阈值（毫秒），默认500ms
     */
    @Min(value = 0, message = "慢SQL阈值不能为负数")
    private long slowSqlThreshold = 500;

    /**
     * 告警阈值（毫秒），默认3000ms
     */
    @Min(value = 0, message = "告警阈值不能为负数")
    private long alertThreshold = 3000;

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getSlowSqlThreshold() { return slowSqlThreshold; }
    public void setSlowSqlThreshold(long slowSqlThreshold) { this.slowSqlThreshold = slowSqlThreshold; }
    public long getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(long alertThreshold) { this.alertThreshold = alertThreshold; }
}
