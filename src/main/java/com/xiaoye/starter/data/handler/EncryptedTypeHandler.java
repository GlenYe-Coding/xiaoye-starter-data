package com.xiaoye.starter.data.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * 字段加密类型处理器
 * 用于敏感字段的加密存储和解密查询
 */
@Slf4j
public class EncryptedTypeHandler extends BaseTypeHandler<String> {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    // TODO: 实际项目中应从配置文件或密钥管理服务获取
    private static final String SECRET_KEY = "0123456789abcdef0123456789abcdef";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            String encrypted = encrypt(parameter);
            ps.setString(i, encrypted);
        } catch (Exception e) {
            log.error("字段加密失败", e);
            throw new SQLException("字段加密失败", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encrypted = rs.getString(columnName);
        return encrypted != null ? decrypt(encrypted) : null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encrypted = rs.getString(columnIndex);
        return encrypted != null ? decrypt(encrypted) : null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encrypted = cs.getString(columnIndex);
        return encrypted != null ? decrypt(encrypted) : null;
    }

    /**
     * 加密
     */
    private String encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        
        // 生成随机IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
        
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // IV + ciphertext
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 解密
     */
    private String decrypt(String encrypted) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encrypted);
        
        // 提取IV和ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);
        
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
        
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
