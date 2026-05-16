package com.xiaoye.starter.data.permission;

import lombok.Data;
import java.io.Serializable;

/**
 * 数据权限上下文
 * <p>
 * 存储当前请求的数据权限上下文信息，包括当前用户ID、租户ID、权限信息等
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Data
public class DataPermissionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<DataPermissionContext> CONTEXT = new ThreadLocal<>();

    /**
     * 当前用户ID
     */
    private Long userId;

    /**
     * 当前租户ID
     */
    private Long tenantId;

    /**
     * 用户所属部门ID
     */
    private Long deptId;

    /**
     * 用户所属区域编码
     */
    private String regionCode;

    /**
     * 部门ID列表（包含子部门）
     */
    private Long[] deptIds;

    /**
     * 区域编码列表（包含子区域）
     */
    private String[] regionCodes;

    /**
     * 是否管理员
     */
    private boolean admin;

    /**
     * 额外权限字段
     */
    private String customColumn;

    /**
     * 额外权限值
     */
    private Object[] customValues;

    /**
     * 获取当前上下文
     */
    public static DataPermissionContext get() {
        DataPermissionContext context = CONTEXT.get();
        if (context == null) {
            context = new DataPermissionContext();
            CONTEXT.set(context);
        }
        return context;
    }

    /**
     * 设置上下文
     */
    public static void set(DataPermissionContext context) {
        CONTEXT.set(context);
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return get().getUserId();
    }

    /**
     * 获取当前租户ID
     */
    public static Long getCurrentTenantId() {
        return get().getTenantId();
    }

    /**
     * 设置用户ID
     */
    public static void setCurrentUserId(Long userId) {
        get().setUserId(userId);
    }

    /**
     * 设置租户ID
     */
    public static void setCurrentTenantId(Long tenantId) {
        get().setTenantId(tenantId);
    }

    /**
     * 设置部门ID
     */
    public static void setCurrentDeptId(Long deptId) {
        get().setDeptId(deptId);
    }

    /**
     * 设置区域编码
     */
    public static void setCurrentRegionCode(String regionCode) {
        get().setRegionCode(regionCode);
    }

    /**
     * 是否管理员
     */
    public static boolean checkAdmin() {
        return get().isAdmin();
    }

    /**
     * 设置是否管理员
     */
    public static void setAdminFlag(boolean admin) {
        get().setAdmin(admin);
    }

    /**
     * 获取部门ID列表
     */
    public static Long[] getCurrentDeptIds() {
        DataPermissionContext context = get();
        if (context.getDeptIds() == null && context.getDeptId() != null) {
            return new Long[]{context.getDeptId()};
        }
        return context.getDeptIds();
    }

    /**
     * 获取区域编码列表
     */
    public static String[] getCurrentRegionCodes() {
        DataPermissionContext context = get();
        if (context.getRegionCodes() == null && context.getRegionCode() != null) {
            return new String[]{context.getRegionCode()};
        }
        return context.getRegionCodes();
    }
}