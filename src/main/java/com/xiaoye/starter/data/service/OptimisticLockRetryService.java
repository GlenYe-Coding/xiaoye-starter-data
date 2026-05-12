package com.xiaoye.starter.data.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * 乐观锁重试服务
 * 当发生乐观锁冲突时自动重试
 */
@Slf4j
@Service
public class OptimisticLockRetryService {

    /**
     * 带重试的更新操作
     * 最多重试3次，每次间隔1秒
     */
    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void updateWithRetry(Runnable updateOperation) {
        try {
            updateOperation.run();
            log.debug("更新操作成功");
        } catch (OptimisticLockingFailureException e) {
            log.warn("乐观锁冲突，准备重试");
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 带重试的更新操作（自定义重试次数）
     */
    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 500, multiplier = 2) // 指数退避
    )
    public void updateWithExponentialRetry(Runnable updateOperation) {
        updateOperation.run();
    }
}
