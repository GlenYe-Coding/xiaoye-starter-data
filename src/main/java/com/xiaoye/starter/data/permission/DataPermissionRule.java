package com.xiaoye.starter.data.permission;

import java.sql.Connection;

/**
 * 数据权限规则接口
 * <p>
 * 定义数据权限过滤的规则接口，各个业务可实现此接口自定义权限规则
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
public interface DataPermissionRule {

    /**
     * 获取规则名称
     *
     * @return 规则名称
     */
    String getName();

    /**
     * 获取匹配的表名
     *
     * @return 表名列表，支持通配符
     */
    String[] getTables();

    /**
     * 获取权限列名
     *
     * @param tableName 表名
     * @return 权限列名
     */
    String getColumn(String tableName);

    /**
     * 获取SQL权限条件
     *
     * @param tableName 表名
     * @param alias     表别名
     * @return SQL条件片段，如 "dept_id IN (1,2,3)"
     */
    String getCondition(String tableName, String alias);

    /**
     * 是否匹配指定表
     *
     * @param tableName 表名
     * @return 是否匹配
     */
    default boolean match(String tableName) {
        for (String pattern : getTables()) {
            if (pattern.equals("*") || pattern.equalsIgnoreCase(tableName)) {
                return true;
            }
            // 支持通配符匹配
            if (pattern.contains("*")) {
                String regex = pattern.replace("*", ".*").toLowerCase();
                if (tableName.toLowerCase().matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    default Long getUserId() {
        return DataPermissionContext.getCurrentUserId();
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    default Long getTenantId() {
        return DataPermissionContext.getCurrentTenantId();
    }

    /**
     * 获取当前用户拥有的权限值列表
     *
     * @param column 权限列名
     * @return 权限值列表
     */
    Object[] getPermissionValues(String column);

    /**
     * 是否跳过权限检查
     *
     * @return 是否跳过
     */
    default boolean isSkipCheck() {
        // 管理员跳过权限检查
        return DataPermissionContext.checkAdmin();
    }
}