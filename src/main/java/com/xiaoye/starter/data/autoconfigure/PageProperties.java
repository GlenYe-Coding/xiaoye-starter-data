package com.xiaoye.starter.data.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分页配置属性
 */
@Data
@ConfigurationProperties(prefix = "xiaoye.data.page")
public class PageProperties {

    /**
     * 默认每页大小
     */
    private long defaultPageSize = 10L;

    /**
     * 最大每页大小（防止恶意请求）
     */
    private long maxPageSize = 100L;
}
