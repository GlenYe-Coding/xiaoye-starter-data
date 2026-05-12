package com.xiaoye.starter.data.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 批量数据处理服务
 */
@Slf4j
@Service
public class BatchProcessService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 1000;

    /**
     * 批量插入
     */
    @Transactional
    public void batchInsert(String sql, List<Object[]> batchArgs) {
        if (batchArgs == null || batchArgs.isEmpty()) {
            return;
        }

        int total = batchArgs.size();
        int processed = 0;

        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<Object[]> subList = batchArgs.subList(i, end);
            
            int[] results = jdbcTemplate.batchUpdate(sql, subList);
            processed += results.length;
            
            log.info("批量插入进度: {}/{}", processed, total);
        }

        log.info("批量插入完成，总数: {}", total);
    }

    /**
     * 批量更新
     */
    @Transactional
    public void batchUpdate(String sql, List<Object[]> batchArgs) {
        batchInsert(sql, batchArgs); // 复用批量插入逻辑
    }

    /**
     * 批量删除
     */
    @Transactional
    public void batchDelete(String sql, List<Object> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        int total = ids.size();
        int processed = 0;

        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<Object> subList = ids.subList(i, end);
            
            String inClause = String.join(",", subList.stream()
                    .map(id -> "?")
                    .toArray(String[]::new));
            
            String deleteSql = sql + " IN (" + inClause + ")";
            jdbcTemplate.update(deleteSql, subList.toArray());
            
            processed += subList.size();
            log.info("批量删除进度: {}/{}", processed, total);
        }

        log.info("批量删除完成，总数: {}", total);
    }
}
