package com.xiaoye.starter.data.shadow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 影子库支持服务
 */
@Slf4j
@Component
public class ShadowDatabaseSupport {

    private static final ThreadLocal<Boolean> shadowMode = new ThreadLocal<>();

    /**
     * 启用影子模式
     */
    public void enableShadowMode() {
        shadowMode.set(true);
        log.info("启用影子库模式");
    }

    /**
     * 禁用影子模式
     */
    public void disableShadowMode() {
        shadowMode.set(false);
        log.info("禁用影子库模式");
    }

    /**
     * 检查是否为影子模式
     */
    public boolean isShadowMode() {
        return Boolean.TRUE.equals(shadowMode.get());
    }

    /**
     * 清除设置
     */
    public void clear() {
        shadowMode.remove();
    }
}
