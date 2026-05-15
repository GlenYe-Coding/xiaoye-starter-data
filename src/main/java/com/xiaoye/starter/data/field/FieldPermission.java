package com.xiaoye.starter.data.field;

import java.lang.annotation.*;

/**
 * 字段级权限注解
 * <p>
 * 用于标注字段的权限级别，支持角色/部门等维度
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldPermission {

    /**
     * 权限级别
     */
    Level level() default Level.READ_WRITE;

    /**
     * 脱敏策略
     */
    MaskingStrategy masking() default MaskingStrategy.NONE;

    /**
     * 权限角色列表
     * <p>
     * 只有列表中的角色可以访问此字段
     * </p>
     */
    String[] roles() default {};

    /**
     * 权限部门ID列表
     * <p>
     * 只有列表中的部门可以访问此字段
     * </p>
     */
    long[] deptIds() default {};

    /**
     * 是否只读
     */
    boolean readOnly() default false;

    /**
     * 是否写入时脱敏
     */
    boolean maskingOnWrite() default false;

    /**
     * 权限级别枚举
     */
    enum Level {
        /**
         * 完全隐藏（不可见）
         */
        HIDDEN,
        /**
         * 只读
         */
        READ_ONLY,
        /**
         * 读写
         */
        READ_WRITE,
        /**
         * 完全权限
         */
        FULL
    }

    /**
     * 脱敏策略枚举
     */
    enum MaskingStrategy {
        /**
         * 不脱敏
         */
        NONE,
        /**
         * 手机号脱敏（如 138****1234）
         */
        PHONE,
        /**
         * 身份证脱敏（如 110101****1234****）
         */
        ID_CARD,
        /**
         * 银行卡脱敏（如 **** **** **** 1234）
         */
        BANK_CARD,
        /**
         * 姓名脱敏（如 张*）
         */
        NAME,
        /**
         * 邮箱脱敏（如 t***@example.com）
         */
        EMAIL,
        /**
         * 地址脱敏
         */
        ADDRESS,
        /**
         * 自定义脱敏（需要配合 maskingFunction 使用）
         */
        CUSTOM
    }
}