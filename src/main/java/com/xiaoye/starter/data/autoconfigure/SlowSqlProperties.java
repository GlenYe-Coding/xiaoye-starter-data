package com.xiaoye.starter.data.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 慢SQL监控配置属性
 */
@Data
@ConfigurationProperties(prefix = "xiaoye.data.slow-sql")
public class SlowSqlProperties {

    /**
     * 是否启用慢SQL监控
     */
    private boolean enabled = true;

    /**
     * 慢SQL阈值（毫秒），默认500ms
     */
    private long slowSqlThreshold = 500;

    /**
     * 告警阈值（毫秒），默认3000ms
     */
    private long alertThreshold = 3000;
}
