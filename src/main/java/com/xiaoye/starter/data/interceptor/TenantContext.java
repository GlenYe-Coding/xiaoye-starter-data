package com.xiaoye.starter.data.interceptor;

/**
 * 租户上下文（data模块副本，避免循环依赖）
 * <p>
 * 用于在当前线程/请求中获取当前租户ID
 * </p>
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 清除租户上下文
     * <p>
     * 必须在请求结束时调用，避免ThreadLocal内存泄漏
     * </p>
     */
    public static void clear() {
        TENANT_ID.remove();
    }

    /**
     * 是否启用多租户
     */
    public static boolean isEnabled() {
        return TENANT_ID.get() != null;
    }

    /**
     * 获取租户ID，如果未设置则返回默认值
     *
     * @param defaultValue 默认值
     * @return 租户ID或默认值
     */
    public static Long getOrDefault(Long defaultValue) {
        Long tenantId = TENANT_ID.get();
        return tenantId != null ? tenantId : defaultValue;
    }
}