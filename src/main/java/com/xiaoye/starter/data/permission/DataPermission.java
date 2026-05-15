package com.xiaoye.starter.data.permission;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 用于标注Mapper方法需要应用数据权限过滤
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 表名
     */
    String table() default "";

    /**
     * 权限类型
     */
    Type type() default Type.DEPARTMENT;

    /**
     * 权限字段
     */
    String column() default "dept_id";

    /**
     * 是否启用
     */
    boolean enabled() default true;

    /**
     * 权限类型枚举
     */
    enum Type {
        /**
         * 部门级权限
         */
        DEPARTMENT,
        /**
         * 区域级权限
         */
        REGION,
        /**
         * 自定义权限
         */
        CUSTOM,
        /**
         * 仅本人数据
         */
        SELF,
        /**
         * 所有数据（管理员）
         */
        ALL
    }
}