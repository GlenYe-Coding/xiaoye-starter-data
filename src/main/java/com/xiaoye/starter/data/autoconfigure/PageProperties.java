package com.xiaoye.starter.data.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

/**
 * 分页配置属性
 */
@Data
@Validated
@ConfigurationProperties(prefix = "xiaoye.data.page")
public class PageProperties {

    /**
     * 默认每页大小
     */
    @Min(value = 1, message = "默认每页大小不能小于1")
    private long defaultPageSize = 10L;

    /**
     * 最大每页大小（防止恶意请求）
     */
    @Min(value = 1, message = "最大每页大小不能小于1")
    private long maxPageSize = 100L;
}
