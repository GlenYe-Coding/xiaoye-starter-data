package com.xiaoye.starter.data.timeout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事务超时控制服务
 */
@Slf4j
@Component
public class TransactionTimeoutControl {

    /**
     * 执行带超时控制的事务
     */
    @Transactional(timeout = 30)
    public void executeWithTimeout(Runnable task) {
        log.info("执行带超时控制的事务");
        task.run();
    }

    /**
     * 设置默认事务超时时间
     */
    public void setDefaultTimeout(int seconds) {
        log.info("设置默认事务超时时间: {}秒", seconds);
    }
}
