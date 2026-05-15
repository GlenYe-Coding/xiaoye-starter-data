package com.xiaoye.starter.data.autoconfigure;

import com.xiaoye.starter.data.permission.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限自动配置
 * <p>
 * P1 功能：
 * - 数据权限注解支持
 * - 行级数据权限切面
 * - 部门和区域权限规则
 * - 字段级权限控制
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "com.xiaoye.starter.data.permission.DataPermission")
@EnableConfigurationProperties(DataProperties.class)
@RequiredArgsConstructor
public class DataPermissionAutoConfiguration {

    private final DataProperties dataProperties;

    /**
     * 初始化数据权限模块
     */
    @PostConstruct
    public void init() {
        if (dataProperties.getDataPermission().isEnabled()) {
            log.info("Data permission module enabled");
        }
    }

    /**
     * 部门权限规则
     */
    @Bean
    @ConditionalOnMissingBean(name = "departmentPermissionRule")
    @ConditionalOnProperty(prefix = "xiaoye.data.data-permission", name = "enabled", havingValue = "true", matchIfMissing = false)
    public DepartmentPermissionRule departmentPermissionRule() {
        return new DepartmentPermissionRule();
    }

    /**
     * 区域权限规则
     */
    @Bean
    @ConditionalOnMissingBean(name = "regionPermissionRule")
    @ConditionalOnProperty(prefix = "xiaoye.data.data-permission", name = "enabled", havingValue = "true", matchIfMissing = false)
    public RegionPermissionRule regionPermissionRule() {
        return new RegionPermissionRule();
    }

    /**
     * 数据权限切面
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "xiaoye.data.data-permission", name = "enabled", havingValue = "true", matchIfMissing = false)
    public DataPermissionAspect dataPermissionAspect(
            List<DataPermissionRule> dataPermissionRules) {
        DataPermissionAspect aspect = new DataPermissionAspect(new ArrayList<>(dataPermissionRules));
        return aspect;
    }

    /**
     * 数据权限上下文清理监听器
     */
    @Bean
    @ConditionalOnProperty(prefix = "xiaoye.data.data-permission", name = "enabled", havingValue = "true", matchIfMissing = false)
    public DataPermissionContextCleaner dataPermissionContextCleaner() {
        return new DataPermissionContextCleaner();
    }

    /**
     * 数据权限上下文清理器
     */
    @Slf4j
    public static class DataPermissionContextCleaner {
        // 使用 @PreDestroy 或请求结束监听器清理 ThreadLocal
        // 具体实现可配合 Spring 的 RequestContextListener
    }
}