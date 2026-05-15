package com.xiaoye.starter.data.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Data 数据模块配置属性
 * <p>
 * 配置数据处理相关的参数，包括分页、慢SQL、多租户等
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Data
@Validated
@ConfigurationProperties(prefix = "xiaoye.data")
public class DataProperties {

    /**
     * 是否启用数据模块
     */
    private boolean enabled = true;

    // ==================== 分页配置 ====================

    /**
     * 分页配置
     */
    private PageProperties page = new PageProperties();

    @Data
    public static class PageProperties {
        /**
         * 默认每页条数
         */
        @Min(value = 1, message = "Default page size must be at least 1")
        private int defaultPageSize = 20;

        /**
         * 最大每页条数
         */
        @Min(value = 1, message = "Max page size must be at least 1")
        private int maxPageSize = 100;

        /**
         * 请求参数名：pageNum
         */
        private String pageNumParam = "pageNum";

        /**
         * 请求参数名：pageSize
         */
        private String pageSizeParam = "pageSize";

        /**
         * 响应数据字段：total
         */
        private String totalField = "total";

        /**
         * 响应数据字段：records
         */
        private String recordsField = "records";

        /**
         * 响应数据字段：pages
         */
        private String pagesField = "pages";

        /**
         * 响应数据字段：pageNum
         */
        private String pageNumField = "pageNum";

        /**
         * 响应数据字段：pageSize
         */
        private String pageSizeField = "pageSize";
    }

    // ==================== 慢SQL配置 ====================

    /**
     * 慢SQL配置
     */
    private SlowSqlProperties slowSql = new SlowSqlProperties();

    @Data
    public static class SlowSqlProperties {
        /**
         * 是否启用慢SQL监控
         */
        private boolean enabled = true;

        /**
         * 慢SQL阈值（毫秒）
         */
        @Min(value = 1, message = "Slow SQL threshold must be at least 1ms")
        private long slowSqlThreshold = 500;

        /**
         * 告警阈值（毫秒）
         */
        @Min(value = 1, message = "Alert threshold must be at least 1ms")
        private long alertThreshold = 3000;

        /**
         * 是否记录 SQL 语句
         */
        private boolean recordSql = true;

        /**
         * 是否记录执行堆栈
         */
        private boolean recordStackTrace = true;

        /**
         * 最大记录参数长度
         */
        @Min(value = 1, message = "Max params length must be at least 1")
        private int maxParamsLength = 2000;

        /**
         * 日志保留天数
         */
        @Min(value = 1, message = "Retention days must be at least 1")
        private int retentionDays = 7;
    }

    // ==================== 多租户配置 ====================

    /**
     * 多租户配置
     */
    private TenantProperties tenant = new TenantProperties();

    @Data
    public static class TenantProperties {
        /**
         * 是否启用多租户
         */
        private boolean enabled = false;

        /**
         * 租户字段名
         */
        @NotBlank(message = "Tenant field cannot be blank")
        private String tenantField = "tenant_id";

        /**
         * 默认租户 ID
         */
        private Long defaultTenantId = 0L;

        /**
         * 排除租户检查的表（逗号分隔）
         */
        private String excludeTables = "sys_user,sys_role,sys_permission,sys_dict";

        /**
         * 排除租户检查的路径（逗号分隔）
         */
        private String excludePaths = "/health,/actuator/**";

        /**
         * 是否启用租户隔离
         */
        private boolean isolationEnabled = true;

        /**
         * 租户字段是否可为空（默认不可为空）
         */
        private boolean nullable = false;
    }

    // ==================== 自动填充配置 ====================

    /**
     * 自动填充配置
     */
    private AutoFillProperties autoFill = new AutoFillProperties();

    @Data
    public static class AutoFillProperties {
        /**
         * 是否启用自动填充
         */
        private boolean enabled = true;

        /**
         * 创建时间字段
         */
        private String createTimeField = "create_time";

        /**
         * 更新时间字段
         */
        private String updateTimeField = "update_time";

        /**
         * 创建人字段
         */
        private String createByField = "create_by";

        /**
         * 更新人字段
         */
        private String updateByField = "update_by";

        /**
         * 版本字段
         */
        private String versionField = "version";

        /**
         * 删除标记字段
         */
        private String deletedField = "deleted";

        /**
         * 删除标记默认值
         */
        private Integer deletedDefaultValue = 0;

        /**
         * 是否自动填充创建时间
         */
        private boolean fillCreateTime = true;

        /**
         * 是否自动填充更新时间
         */
        private boolean fillUpdateTime = true;

        /**
         * 是否自动填充创建人
         */
        private boolean fillCreateBy = true;

        /**
         * 是否自动填充更新人
         */
        private boolean fillUpdateBy = true;

        /**
         * 是否自动填充版本号
         */
        private boolean fillVersion = true;
    }

    // ==================== 数据权限配置 ====================

    /**
     * 数据权限配置
     */
    private DataPermissionProperties dataPermission = new DataPermissionProperties();

    @Data
    public static class DataPermissionProperties {
        /**
         * 是否启用数据权限
         */
        private boolean enabled = false;

        /**
         * 数据权限字段
         */
        @NotBlank(message = "Data permission field cannot be blank")
        private String dataPermissionField = "org_id";

        /**
         * 权限过滤方式：AUTO, MANUAL
         */
        private String filterMode = "AUTO";

        /**
         * 是否启用行级权限
         */
        private boolean rowPermissionEnabled = false;

        /**
         * 行级权限字段
         */
        private String rowPermissionField = "";
    }

    // ==================== 数据加密配置 ====================

    /**
     * 数据加密配置
     */
    private EncryptionProperties encryption = new EncryptionProperties();

    @Data
    public static class EncryptionProperties {
        /**
         * 是否启用数据加密
         */
        private boolean enabled = false;

        /**
         * 加密算法：AES, RSA, SM4
         */
        private String algorithm = "AES";

        /**
         * 密钥（Base64编码）
         */
        @NotBlank(message = "Encryption key cannot be blank")
        private String key = "";

        /**
         * 加密字段列表（逗号分隔）
         */
        private String encryptFields = "";
    }

    // ==================== CRUD 配置 ====================

    /**
     * CRUD 配置
     */
    private CrudProperties crud = new CrudProperties();

    @Data
    public static class CrudProperties {
        /**
         * 是否启用逻辑删除
         */
        private boolean logicalDeleteEnabled = true;

        /**
         * 逻辑删除值
         */
        private Integer deleteValue = 1;

        /**
         * 逻辑未删除值
         */
        private Integer undeleteValue = 0;

        /**
         * 是否启用租户字段填充
         */
        private boolean tenantFillEnabled = true;

        /**
         * 是否启用乐观锁
         */
        private boolean optimisticLockEnabled = true;

        /**
         * 是否自动处理空字符串
         */
        private boolean trimStringEnabled = true;

        /**
         * 是否启用字段验证
         */
        private boolean validationEnabled = true;
    }
}