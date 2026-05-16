package com.xiaoye.starter.data.field;

import java.util.function.Function;

/**
 * 字段脱敏策略接口
 * <p>
 * 定义自定义脱敏策略的统一接口
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@FunctionalInterface
public interface FieldMaskingStrategy extends Function<String, String> {

    /**
     * 应用脱敏
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    @Override
    String apply(String value);

    /**
     * 手机号脱敏策略
     */
    FieldMaskingStrategy PHONE = value -> {
        if (value == null || value.length() < 7) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    };

    /**
     * 身份证脱敏策略
     */
    FieldMaskingStrategy ID_CARD = value -> {
        if (value == null || value.length() < 8) {
            return value;
        }
        return value.substring(0, 4) + "********" + value.substring(value.length() - 4);
    };

    /**
     * 银行卡脱敏策略
     */
    FieldMaskingStrategy BANK_CARD = value -> {
        if (value == null || value.length() < 4) {
            return value;
        }
        int visibleLength = 4;
        int maskedLength = value.length() - visibleLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maskedLength; i += 4) {
            sb.append("****");
        }
        sb.append(value.substring(value.length() - visibleLength));
        return sb.toString();
    };

    /**
     * 姓名脱敏策略
     */
    FieldMaskingStrategy NAME = value -> {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int length = value.length();
        if (length == 1) {
            return value;
        }
        if (length == 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + "*".repeat(length - 2) + value.charAt(length - 1);
    };

    /**
     * 邮箱脱敏策略
     */
    FieldMaskingStrategy EMAIL = value -> {
        if (value == null || !value.contains("@")) {
            return value;
        }
        String[] parts = value.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domainPart;
        }
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domainPart;
    };

    /**
     * 地址脱敏策略
     */
    FieldMaskingStrategy ADDRESS = value -> {
        if (value == null || value.length() < 5) {
            return value;
        }
        return value.substring(0, 5) + "***";
    };
}