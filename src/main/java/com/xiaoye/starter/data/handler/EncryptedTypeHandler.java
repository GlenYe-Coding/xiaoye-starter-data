package com.xiaoye.starter.data.handler;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.xiaoye.common.utils.CryptoUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字段加密类型处理器
 * <p>
 * P1 功能：
 * - 自动对敏感字段（如手机号、身份证）进行 AES/SM4 加解密
 * - 支持国密 SM4 算法
 * - 支持多种加密模式
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "xiaoye.data.encryption")
public class EncryptedTypeHandler extends BaseTypeHandler<String> implements Constants {

    /**
     * 默认密钥（生产环境应从配置读取）
     */
    private static final String DEFAULT_KEY = "XiaoYeSecretKey12";

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 加密算法：AES, SM4
     */
    private String algorithm = "AES";

    /**
     * 密钥
     */
    private String key = DEFAULT_KEY;

    /**
     * 密钥来源：config（配置）/ env（环境变量）
     */
    private String keySource = "config";

    /**
     * 环境变量名称
     */
    private String keyEnvVariable = "XIAOYE_DATA_ENCRYPT_KEY";

    @PostConstruct
    public void init() {
        // 从环境变量获取密钥
        if ("env".equals(keySource)) {
            String envKey = System.getenv(keyEnvVariable);
            if (StringUtils.hasText(envKey)) {
                this.key = envKey;
            }
        }

        if (!StringUtils.hasText(key)) {
            log.warn(">>> XiaoYe Data Encryption: No encryption key configured, using default key");
            this.key = DEFAULT_KEY;
        }

        log.info(">>> XiaoYe Data Encryption: Enabled with algorithm={}", algorithm);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            String encrypted = encrypt(parameter);
            ps.setString(i, encrypted);
        } catch (Exception e) {
            throw new SQLException("Encryption failed", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encrypted = rs.getString(columnName);
        return decrypt(encrypted);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encrypted = rs.getString(columnIndex);
        return decrypt(encrypted);
    }

    @Override
    public String getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String encrypted = cs.getString(columnIndex);
        return decrypt(encrypted);
    }

    private String encrypt(String content) {
        if (content == null) {
            return null;
        }

        if (!enabled) {
            return content;
        }

        try {
            if ("SM4".equalsIgnoreCase(algorithm)) {
                return CryptoUtils.sm4Encrypt(content, key);
            } else {
                return CryptoUtils.aesEncrypt(content, key);
            }
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    private String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }

        if (!enabled) {
            return encrypted;
        }

        try {
            if ("SM4".equalsIgnoreCase(algorithm)) {
                return CryptoUtils.sm4Decrypt(encrypted, key);
            } else {
                return CryptoUtils.aesDecrypt(encrypted, key);
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}