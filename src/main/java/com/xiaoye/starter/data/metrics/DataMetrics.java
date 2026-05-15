package com.xiaoye.starter.data.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Data 数据层指标统计
 * <p>
 * 提供CRUD操作、慢查询、租户拦截等环节的监控指标
 * </p>
 *
 * <h2>暴露的指标</h2>
 * <pre>
 * xiaoye.data.crud.total        - CRUD操作
 * xiaoye.data.slow.query       - 慢查询
 * xiaoye.data.tenant.intercept - 租户拦截
 * </pre>
 */
@Slf4j
@Component
public class DataMetrics {

    private final MeterRegistry meterRegistry;

    // 计数器缓存
    private final ConcurrentHashMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    // 计时器缓存
    private final ConcurrentHashMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public DataMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录CRUD操作
     *
     * @param tableName  表名
     * @param operation  操作类型 (insert, update, delete, select)
     */
    public void recordCrud(String tableName, String operation) {
        Counter counter = getOrCreateCounter("xiaoye.data", "crud.total", "table", tableName, "operation", operation);
        counter.increment();
        log.debug("CRUD operation recorded: table={}, operation={}", tableName, operation);
    }

    /**
     * 记录CRUD耗时
     *
     * @param tableName   表名
     * @param operation   操作类型
     * @param durationMs  耗时（毫秒）
     */
    public void recordCrudDuration(String tableName, String operation, long durationMs) {
        Timer timer = getOrCreateTimer("xiaoye.data", "crud.duration", tableName, operation);
        timer.record(durationMs, TimeUnit.MILLISECONDS);
        log.debug("CRUD duration recorded: table={}, operation={}, duration={}ms", tableName, operation, durationMs);
    }

    /**
     * 记录慢查询
     *
     * @param sql         SQL语句（截断后）
     * @param tableName   表名
     * @param durationMs  耗时（毫秒）
     */
    public void recordSlowQuery(String sql, String tableName, long durationMs) {
        // 截断SQL防止过长
        String truncatedSql = sql.length() > 100 ? sql.substring(0, 100) + "..." : sql;
        Counter counter = getOrCreateCounter("xiaoye.data", "slow.query", "table", tableName);
        counter.increment();
        log.warn("Slow query recorded: table={}, duration={}ms, sql={}", tableName, durationMs, truncatedSql);
    }

    /**
     * 记录查询命中（缓存命中）
     *
     * @param tableName  表名
     */
    public void recordQueryHit(String tableName) {
        Counter counter = getOrCreateCounter("xiaoye.data", "query.hit", "table", tableName);
        counter.increment();
    }

    /**
     * 记录查询未命中（缓存未命中）
     *
     * @param tableName  表名
     */
    public void recordQueryMiss(String tableName) {
        Counter counter = getOrCreateCounter("xiaoye.data", "query.miss", "table", tableName);
        counter.increment();
    }

    /**
     * 记录租户拦截
     *
     * @param tenantId    租户ID
     * @param tableName   表名
     * @param interceptType 拦截类型 (no_tenant, no_permission, cross_tenant)
     */
    public void recordTenantIntercept(String tenantId, String tableName, String interceptType) {
        Counter counter = getOrCreateCounter("xiaoye.data", "tenant.intercept", "table", tableName, "type", interceptType);
        counter.increment();
        log.debug("Tenant intercept recorded: tenantId={}, table={}, type={}", tenantId, tableName, interceptType);
    }

    /**
     * 记录乐观锁冲突
     *
     * @param tableName  表名
     * @param recordId   记录ID
     */
    public void recordOptimisticLock(String tableName, String recordId) {
        Counter counter = getOrCreateCounter("xiaoye.data", "optimistic.lock", "table", tableName);
        counter.increment();
        log.debug("Optimistic lock conflict recorded: table={}, recordId={}", tableName, recordId);
    }

    /**
     * 记录批量操作
     *
     * @param tableName  表名
     * @param operation  操作类型
     * @param batchSize  批量大小
     */
    public void recordBatchOperation(String tableName, String operation, int batchSize) {
        Counter counter = getOrCreateCounter("xiaoye.data", "batch." + operation, "table", tableName);
        counter.increment(batchSize);
        log.debug("Batch operation recorded: table={}, operation={}, batchSize={}", tableName, operation, batchSize);
    }

    /**
     * 获取或创建计数器
     */
    private Counter getOrCreateCounter(String prefix, String name, String... tags) {
        String key = prefix + "." + name + "." + String.join(".", tags);
        return counterCache.computeIfAbsent(key, k -> {
            Counter.Builder builder = Counter.builder(prefix + "." + name)
                    .description("Data metrics counter");
            for (int i = 0; i < tags.length; i += 2) {
                if (i + 1 < tags.length) {
                    builder.tag(tags[i], tags[i + 1]);
                }
            }
            return builder.register(meterRegistry);
        });
    }

    /**
     * 获取或创建计时器
     */
    private Timer getOrCreateTimer(String prefix, String name, String... tags) {
        String key = prefix + "." + name + "." + String.join(".", tags);
        return timerCache.computeIfAbsent(key, k -> {
            Timer.Builder builder = Timer.builder(prefix + "." + name)
                    .description("Data metrics timer");
            for (int i = 0; i < tags.length; i += 2) {
                if (i + 1 < tags.length) {
                    builder.tag(tags[i], tags[i + 1]);
                }
            }
            return builder.register(meterRegistry);
        });
    }
}
