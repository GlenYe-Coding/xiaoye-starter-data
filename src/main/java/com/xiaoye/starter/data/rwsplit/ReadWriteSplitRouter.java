package com.xiaoye.starter.data.rwsplit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 读写分离数据源路由
 */
@Slf4j
@Component
public class ReadWriteSplitRouter {

    private static final ThreadLocal<Boolean> readOnlyHolder = new ThreadLocal<>();

    /**
     * 设置为读库
     */
    public void useReadDataSource() {
        readOnlyHolder.set(true);
        log.debug("使用读库");
    }

    /**
     * 设置为写库
     */
    public void useWriteDataSource() {
        readOnlyHolder.set(false);
        log.debug("使用写库");
    }

    /**
     * 获取当前数据源类型
     */
    public boolean isReadOnly() {
        return Boolean.TRUE.equals(readOnlyHolder.get());
    }

    /**
     * 清除设置
     */
    public void clear() {
        readOnlyHolder.remove();
    }
}
