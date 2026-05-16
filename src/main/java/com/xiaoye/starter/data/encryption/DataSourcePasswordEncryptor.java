package com.xiaoye.starter.data.encryption;

import com.xiaoye.common.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源密码加密器
 * <p>
 * 支持多数据源密码加密，支持配置加密和运行时解密
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "xiaoye.data.datasource-crypto")
public class DataSourcePasswordEncryptor {

    private static final Logger log = LoggerFactory.getLogger(DataSourcePasswordEncryptor.class);

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 默认加密算法：AES, SM4
     */
    private String algorithm = "SM4";

    /**
     * 加密密钥
     */
    private String key;

    /**
     * 密钥来源：config（配置）/ env（环境变量）
     */
    private String keySource = "config";

    /**
     * 环境变量名称
     */
    private String keyEnvVariable = "XIAOYE_DATASOURCE_KEY";

    /**
     * 数据源配置映射（key: 数据源名称, value: 自定义密钥）
     */
    private Map<String, String> datasourceKeys = new ConcurrentHashMap<>();

    /**
     * 数据源密码属性名映射
     */
    private Map<String, String> passwordProperties = Map.of(
            "primary", "spring.datasource.password",
            "slave1", "spring.datasource.slave1.password",
            "slave2", "spring.datasource.slave2.password",
            "hikari", "spring.datasource.hikari.password"
    );

    /**
     * 加密结果缓存
     */
    private final Map<String, String> encryptedCache = new ConcurrentHashMap<>();

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getKeySource() { return keySource; }
    public void setKeySource(String keySource) { this.keySource = keySource; }
    public String getKeyEnvVariable() { return keyEnvVariable; }
    public void setKeyEnvVariable(String keyEnvVariable) { this.keyEnvVariable = keyEnvVariable; }
    public Map<String, String> getDatasourceKeys() { return datasourceKeys; }
    public void setDatasourceKeys(Map<String, String> datasourceKeys) { this.datasourceKeys = datasourceKeys; }
    public Map<String, String> getPasswordProperties() { return passwordProperties; }
    public void setPasswordProperties(Map<String, String> passwordProperties) { this.passwordProperties = passwordProperties; }

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info(">>> XiaoYe DataSource Encryption: Disabled");
            return;
        }

        // 从环境变量获取密钥
        if ("env".equals(keySource) && !StringUtils.hasText(key)) {
            String envKey = System.getenv(keyEnvVariable);
            if (StringUtils.hasText(envKey)) {
                this.key = envKey;
            }
        }

        if (!StringUtils.hasText(key)) {
            log.warn(">>> XiaoYe DataSource Encryption: No key configured, please set xiaoye.data.datasource-crypto.key");
        } else {
            log.info(">>> XiaoYe DataSource Encryption: Enabled with algorithm={}, keySource={}",
                    algorithm, keySource);
        }
    }

    /**
     * 加密密码
     *
     * @param plainPassword 明文密码
     * @param datasourceName 数据源名称（可选）
     * @return 加密后的密码（格式：ENC(密文)）
     */
    public String encrypt(String plainPassword, String datasourceName) {
        if (!enabled || !StringUtils.hasText(plainPassword)) {
            return plainPassword;
        }

        // 检查缓存
        String cacheKey = datasourceName + ":" + plainPassword;
        if (encryptedCache.containsKey(cacheKey)) {
            return encryptedCache.get(cacheKey);
        }

        String effectiveKey = getEffectiveKey(datasourceName);
        if (!StringUtils.hasText(effectiveKey)) {
            log.warn("No encryption key available for datasource: {}", datasourceName);
            return plainPassword;
        }

        try {
            String encrypted;
            if ("AES".equalsIgnoreCase(algorithm)) {
                encrypted = CryptoUtils.aesEncrypt(plainPassword, effectiveKey);
            } else {
                encrypted = CryptoUtils.sm4Encrypt(plainPassword, effectiveKey);
            }

            String result = "ENC(" + encrypted + ")";
            encryptedCache.put(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to encrypt password for datasource {}: {}", datasourceName, e.getMessage());
            throw new RuntimeException("Password encryption failed", e);
        }
    }

    /**
     * 加密密码（使用默认数据源）
     */
    public String encrypt(String plainPassword) {
        return encrypt(plainPassword, "primary");
    }

    /**
     * 解密密码
     *
     * @param encryptedPassword 加密密码（格式：ENC(密文)）
     * @param datasourceName     数据源名称（可选）
     * @return 解密后的明文密码
     */
    public String decrypt(String encryptedPassword, String datasourceName) {
        if (!enabled || !StringUtils.hasText(encryptedPassword)) {
            return encryptedPassword;
        }

        // 不是加密格式，直接返回
        if (!encryptedPassword.startsWith("ENC(") || !encryptedPassword.endsWith(")")) {
            return encryptedPassword;
        }

        // 提取密文
        String encrypted = encryptedPassword.substring(4, encryptedPassword.length() - 1);
        String effectiveKey = getEffectiveKey(datasourceName);

        if (!StringUtils.hasText(effectiveKey)) {
            log.warn("No encryption key available for datasource: {}", datasourceName);
            return encryptedPassword;
        }

        try {
            String decrypted;
            if ("AES".equalsIgnoreCase(algorithm)) {
                decrypted = CryptoUtils.aesDecrypt(encrypted, effectiveKey);
            } else {
                decrypted = CryptoUtils.sm4Decrypt(encrypted, effectiveKey);
            }
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt password for datasource {}: {}", datasourceName, e.getMessage());
            throw new RuntimeException("Password decryption failed", e);
        }
    }

    /**
     * 解密密码（使用默认数据源）
     */
    public String decrypt(String encryptedPassword) {
        return decrypt(encryptedPassword, "primary");
    }

    /**
     * 获取有效的密钥
     */
    private String getEffectiveKey(String datasourceName) {
        if (StringUtils.hasText(datasourceName) && datasourceKeys.containsKey(datasourceName)) {
            return datasourceKeys.get(datasourceName);
        }
        return key;
    }

    /**
     * 注册数据源密钥
     *
     * @param datasourceName 数据源名称
     * @param key             密钥
     */
    public void registerDatasourceKey(String datasourceName, String key) {
        datasourceKeys.put(datasourceName, key);
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        encryptedCache.clear();
    }

    /**
     * 生成命令行加密工具
     * <p>
     * 用于在部署前加密密码
     * </p>
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String generateEncryptedPassword(String plainPassword) {
        DataSourcePasswordEncryptor encryptor = new DataSourcePasswordEncryptor();
        // 使用默认密钥初始化
        encryptor.setEnabled(true);
        encryptor.setAlgorithm("SM4");
        // 从环境变量获取
        String key = System.getenv("XIAOYE_DATASOURCE_KEY");
        if (StringUtils.hasText(key)) {
            encryptor.setKey(key);
        } else {
            // 使用编译时默认密钥（仅用于演示）
            encryptor.setKey("0123456789abcdef");
        }
        return encryptor.encrypt(plainPassword);
    }
}