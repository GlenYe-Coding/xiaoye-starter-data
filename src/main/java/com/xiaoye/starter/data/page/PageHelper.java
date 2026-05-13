package com.xiaoye.starter.data.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 分页助手工具类
 * <p>
 * 封装 MyBatis Plus 分页与统一分页响应格式之间的转换
 * </p>
 */
public class PageHelper {

    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;

    private PageHelper() {
    }

    /**
     * 创建分页请求
     */
    public static <T> Page<T> of(long pageNum, long pageSize) {
        long size = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        long current = Math.max(pageNum, 1);
        return new Page<>(current, size);
    }

    /**
     * 创建分页请求（使用默认页大小）
     */
    public static <T> Page<T> of(long pageNum) {
        return of(pageNum, DEFAULT_PAGE_SIZE);
    }

    /**
     * 转换为统一分页响应
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 转换为统一分页响应
     */
    public static <T, R> PageResult<R> toPageResult(IPage<T> page, List<R> records) {
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 计算分页偏移量
     */
    public static long getOffset(long pageNum, long pageSize) {
        return (Math.max(pageNum, 1) - 1) * Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
