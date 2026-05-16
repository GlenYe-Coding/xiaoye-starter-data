package com.xiaoye.starter.data.autoconfigure;

import com.xiaoye.starter.data.permission.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.PostConstruct;
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
@AutoConfiguration
@ConditionalOnClass(name = "com.xiaoye.starter.data.permission.DataPermission")
@EnableConfigurationProperties(DataProperties.class)
public class DataPermissionAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DataPermissionAutoConfiguration.class);

    private final DataProperties dataProperties;

    public DataPermissionAutoConfiguration(DataProperties dataProperties) {
        this.dataProperties = dataProperties;
    }

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
     * 数据权限上下文清理拦截器
     * <p>
     * 在请求完成后清理 ThreadLocal，避免内存泄漏
     * </p>
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnProperty(prefix = "xiaoye.data.data-permission", name = "enabled", havingValue = "true", matchIfMissing = false)
    public HandlerInterceptor dataPermissionContextCleanerInterceptor() {
        return new DataPermissionContextCleanerInterceptor();
    }

    /**
     * 数据权限上下文清理拦截器实现
     * <p>
     * 在请求完成时清理 ThreadLocal，防止内存泄漏和权限泄露
     * </p>
     */
    public static class DataPermissionContextCleanerInterceptor implements HandlerInterceptor {

        private static final Logger log = LoggerFactory.getLogger(DataPermissionContextCleanerInterceptor.class);

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, Exception ex) {
            try {
                DataPermissionContext.clear();
                log.debug("DataPermissionContext cleared for request: {}",
                    request.getRequestURI());
            } catch (Exception e) {
                log.error("Failed to clear DataPermissionContext", e);
            }
        }
    }
}