package com.xiaoye.starter.data.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoye.common.core.response.PageResult;
import com.xiaoye.starter.data.autoconfigure.PageProperties;

import java.util.List;

/**
 * 分页助手工具类
 * <p>
 * 封装 MyBatis Plus 分页与统一分页响应格式之间的转换
 * </p>
 */
public class PageHelper {

    private static volatile PageProperties properties;

    /**
     * 默认每页大小（毫秒）
     * 可通过 PageProperties 配置覆盖
     */
    private static final long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 最大每页大小（防止恶意请求）
     * 可通过 PageProperties 配置覆盖
     */
    private static final long MAX_PAGE_SIZE = 100L;

    private PageHelper() {
    }

    /**
     * 设置分页配置（由自动配置调用）
     */
    public static void setProperties(PageProperties pageProperties) {
        properties = pageProperties;
    }

    /**
     * 获取默认每页大小
     */
    private static long getDefaultPageSize() {
        return properties != null ? properties.getDefaultPageSize() : DEFAULT_PAGE_SIZE;
    }

    /**
     * 获取最大每页大小
     */
    private static long getMaxPageSize() {
        return properties != null ? properties.getMaxPageSize() : MAX_PAGE_SIZE;
    }

    /**
     * 创建分页请求
     */
    public static <T> Page<T> of(long pageNum, long pageSize) {
        long size = Math.min(Math.max(pageSize, 1), getMaxPageSize());
        long current = Math.max(pageNum, 1);
        return new Page<>(current, size);
    }

    /**
     * 创建分页请求（使用默认页大小）
     */
    public static <T> Page<T> of(long pageNum) {
        return of(pageNum, getDefaultPageSize());
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
        return (Math.max(pageNum, 1) - 1) * Math.min(pageSize, getMaxPageSize());
    }
}
