package com.xiaoye.starter.data.field;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段权限过滤器
 * <p>
 * 根据当前用户权限动态过滤/脱敏响应字段
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Component
public class FieldPermissionFilter {

    private static final Logger log = LoggerFactory.getLogger(FieldPermissionFilter.class);

    /**
     * 缓存：类 -> 字段权限映射
     */
    private static final Map<Class<?>, Map<String, FieldPermissionInfo>> PERMISSION_CACHE = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public FieldPermissionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        registerSerializerModule();
    }

    /**
     * 注册自定义序列化器
     */
    private void registerSerializerModule() {
        SimpleModule module = new SimpleModule("FieldPermissionModule");
        module.addSerializer(new FieldPermissionJsonSerializer(this));
        objectMapper.registerModule(module);
    }

    /**
     * 过滤对象字段
     *
     * @param obj        目标对象
     * @param <T>       对象类型
     * @return          过滤后的对象（深拷贝）
     */
    public <T> T filter(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            // 使用 JSON 序列化/反序列化实现深拷贝和过滤
            String json = objectMapper.writeValueAsString(obj);
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            // 获取对象的权限信息
            Class<?> clazz = obj.getClass();
            Map<String, FieldPermissionInfo> fieldPermissions = getFieldPermissions(clazz);

            // 过滤字段
            filterMap(map, fieldPermissions);

            // 反序列化回目标类型
            @SuppressWarnings("unchecked")
            Class<T> targetClass = (Class<T>) obj.getClass();
            return objectMapper.convertValue(map, targetClass);
        } catch (Exception e) {
            log.warn("Failed to filter fields for object: {}", obj.getClass().getName(), e);
            return obj;
        }
    }

    /**
     * 过滤 Map
     */
    private void filterMap(Map<String, Object> map, Map<String, FieldPermissionInfo> fieldPermissions) {
        if (map == null || fieldPermissions == null || fieldPermissions.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : new ArrayList<>(map.entrySet())) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            FieldPermissionInfo info = fieldPermissions.get(fieldName);
            if (info == null) {
                // 无注解字段，保持原样
                continue;
            }

            switch (info.getLevel()) {
                case HIDDEN:
                    // 完全隐藏
                    map.remove(fieldName);
                    break;
                case READ_ONLY:
                    // 只读字段，检查写入场景（这里只处理读取，所以不删除）
                    // 如果是写入场景过滤，需要另外处理
                    break;
                case READ_WRITE:
                case FULL:
                    // 读写或完全权限，需要检查脱敏
                    if (info.getMasking() != FieldPermission.MaskingStrategy.NONE) {
                        Object maskedValue = applyMasking(value, info.getMasking());
                        map.put(fieldName, maskedValue);
                    }
                    break;
            }
        }
    }

    /**
     * 过滤集合
     */
    public <T> List<T> filter(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(list.size());
        for (T item : list) {
            result.add(filter(item));
        }
        return result;
    }

    /**
     * 获取类字段权限信息
     */
    private Map<String, FieldPermissionInfo> getFieldPermissions(Class<?> clazz) {
        return PERMISSION_CACHE.computeIfAbsent(clazz, this::extractFieldPermissions);
    }

    /**
     * 提取类字段权限信息
     */
    private Map<String, FieldPermissionInfo> extractFieldPermissions(Class<?> clazz) {
        Map<String, FieldPermissionInfo> permissions = new HashMap<>();

        // 遍历父类字段
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                FieldPermission annotation = field.getAnnotation(FieldPermission.class);
                if (annotation != null) {
                    FieldPermissionInfo info = new FieldPermissionInfo();
                    info.setLevel(annotation.level());
                    info.setMasking(annotation.masking());
                    info.setRoles(Arrays.asList(annotation.roles()));
                    info.setDeptIds(Arrays.stream(annotation.deptIds()).boxed().toArray(Long[]::new));
                    info.setReadOnly(annotation.readOnly());
                    info.setMaskingOnWrite(annotation.maskingOnWrite());
                    permissions.put(field.getName(), info);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return permissions;
    }

    /**
     * 应用脱敏策略
     */
    private Object applyMasking(Object value, FieldPermission.MaskingStrategy strategy) {
        if (value == null) {
            return null;
        }

        String strValue = value.toString();

        switch (strategy) {
            case PHONE:
                return maskPhone(strValue);
            case ID_CARD:
                return maskIdCard(strValue);
            case BANK_CARD:
                return maskBankCard(strValue);
            case NAME:
                return maskName(strValue);
            case EMAIL:
                return maskEmail(strValue);
            case ADDRESS:
                return maskAddress(strValue);
            case NONE:
            default:
                return value;
        }
    }

    /**
     * 手机号脱敏
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 身份证脱敏
     */
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 银行卡脱敏
     */
    public String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 4) {
            return bankCard;
        }
        int visibleLength = 4;
        int maskedLength = bankCard.length() - visibleLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maskedLength; i += 4) {
            sb.append("****");
        }
        sb.append(bankCard.substring(bankCard.length() - visibleLength));
        return sb.toString();
    }

    /**
     * 姓名脱敏
     */
    public String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        int length = name.length();
        if (length == 1) {
            return name;
        }
        if (length == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(length - 2) + name.charAt(length - 1);
    }

    /**
     * 邮箱脱敏
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domainPart;
        }
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domainPart;
    }

    /**
     * 地址脱敏
     */
    public String maskAddress(String address) {
        if (address == null || address.length() < 5) {
            return address;
        }
        // 只保留前5位 + 脱敏
        return address.substring(0, 5) + "***";
    }

    /**
     * 字段权限信息
     */
    public static class FieldPermissionInfo {
        private FieldPermission.Level level;
        private FieldPermission.MaskingStrategy masking;
        private List<String> roles;
        private Long[] deptIds;
        private boolean readOnly;
        private boolean maskingOnWrite;

        // Getters and Setters
        public FieldPermission.Level getLevel() { return level; }
        public void setLevel(FieldPermission.Level level) { this.level = level; }
        public FieldPermission.MaskingStrategy getMasking() { return masking; }
        public void setMasking(FieldPermission.MaskingStrategy masking) { this.masking = masking; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        public Long[] getDeptIds() { return deptIds; }
        public void setDeptIds(Long[] deptIds) { this.deptIds = deptIds; }
        public boolean isReadOnly() { return readOnly; }
        public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
        public boolean isMaskingOnWrite() { return maskingOnWrite; }
        public void setMaskingOnWrite(boolean maskingOnWrite) { this.maskingOnWrite = maskingOnWrite; }
    }

    /**
     * 字段权限 JSON 序列化器
     */
    public static class FieldPermissionJsonSerializer extends JsonSerializer<Object> {
        private final FieldPermissionFilter filter;

        public FieldPermissionJsonSerializer(FieldPermissionFilter filter) {
            this.filter = filter;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            // 直接序列化，字段过滤在 filter 方法中处理
            serializers.defaultSerializeValue(value, gen);
        }
    }
}