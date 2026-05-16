package com.xiaoye.starter.data.encryption;

import java.lang.annotation.*;

/**
 * 字段加密注解
 * <p>
 * 标记需要加密的字段，自动加解密
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptField {

    /**
     * 加密算法：AES, SM4（默认使用配置中的算法）
     */
    String algorithm() default "";

    /**
     * 是否启用加密（默认 true）
     */
    boolean enabled() default true;

    /**
     * 加密模式：AES/CBC/PKCS5Padding, AES/ECB/PKCS5Padding
     */
    String mode() default "";

    /**
     * 初始向量（IV），CBC 模式需要
     */
    String iv() default "";
}