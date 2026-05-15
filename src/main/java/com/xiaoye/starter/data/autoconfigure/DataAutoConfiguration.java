package com.xiaoye.starter.data.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xiaoye.starter.data.metrics.DataMetrics;
import com.xiaoye.starter.data.monitor.SlowSqlInterceptor;
import com.xiaoye.starter.data.page.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

/**
 * Data 模块自动配置
 * <p>
 * P0 功能：
 * - MyBatis Plus 分页插件
 * - 乐观锁插件
 * - 慢SQL监控拦截器（可选）
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties({SlowSqlProperties.class, PageProperties.class})
@RequiredArgsConstructor
public class DataAutoConfiguration {

    private final PageProperties pageProperties;

    /**
     * MyBatis Plus 插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 初始化分页配置
     */
    @PostConstruct
    public void initPageHelper() {
        PageHelper.setProperties(pageProperties);
    }

    /**
     * 慢SQL监控拦截器
     * <p>
     * 可通过配置自定义阈值：
     * xiaoye.data.slow-sql.enabled=true
     * xiaoye.data.slow-sql.slow-sql-threshold=500
     * xiaoye.data.slow-sql.alert-threshold=3000
     * </p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "xiaoye.data.slow-sql", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public SlowSqlInterceptor slowSqlInterceptor(SlowSqlProperties properties) {
        SlowSqlInterceptor interceptor = new SlowSqlInterceptor();
        interceptor.setSlowSqlThreshold(properties.getSlowSqlThreshold());
        interceptor.setAlertThreshold(properties.getAlertThreshold());
        return interceptor;
    }

    // ==================== Metrics ====================

    @Bean
    @ConditionalOnMissingBean
    public DataMetrics dataMetrics() {
        return new DataMetrics(null);
    }
}
