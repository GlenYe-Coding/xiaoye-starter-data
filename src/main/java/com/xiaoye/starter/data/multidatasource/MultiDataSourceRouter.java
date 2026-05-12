package com.xiaoye.starter.data.multidatasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 多数据源路由服务
 */
@Slf4j
@Component
public class MultiDataSourceRouter {

    private static final ThreadLocal<String> dataSourceHolder = new ThreadLocal<>();

    /**
     * 切换数据源
     */
    public void switchDataSource(String dataSourceName) {
        dataSourceHolder.set(dataSourceName);
        log.info("切换数据源: {}", dataSourceName);
    }

    /**
     * 获取当前数据源
     */
    public String getCurrentDataSource() {
        return dataSourceHolder.get();
    }

    /**
     * 清除数据源设置
     */
    public void clear() {
        dataSourceHolder.remove();
    }
}
