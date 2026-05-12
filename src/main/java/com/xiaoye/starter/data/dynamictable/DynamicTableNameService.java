package com.xiaoye.starter.data.dynamictable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 动态表名支持服务
 */
@Slf4j
@Component
public class DynamicTableNameService {

    private static final ThreadLocal<String> tableNameHolder = new ThreadLocal<>();

    /**
     * 设置当前线程的表名
     */
    public void setTableName(String tableName) {
        tableNameHolder.set(tableName);
        log.debug("设置动态表名: {}", tableName);
    }

    /**
     * 获取当前线程的表名
     */
    public String getTableName() {
        return tableNameHolder.get();
    }

    /**
     * 清除表名
     */
    public void clear() {
        tableNameHolder.remove();
    }
}
