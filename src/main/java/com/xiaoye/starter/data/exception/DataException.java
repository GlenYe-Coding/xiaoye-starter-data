package com.xiaoye.starter.data.exception;

import com.xiaoye.common.core.exception.BusinessException;
import com.xiaoye.starter.data.core.DataErrorCode;

/**
 * 数据模块业务异常
 * <p>
 * 用于处理数据库操作、分页、CRUD 等数据相关异常
 *
 * @author xiaoye
 * @since 1.0.0
 */
public class DataException extends BusinessException {

    /**
     * 使用错误码构造异常
     *
     * @param errorCode 错误码
     */
    public DataException(DataErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 使用错误码和自定义消息构造异常
     *
     * @param errorCode 错误码
     * @param message 自定义消息
     */
    public DataException(DataErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }

    /**
     * 使用自定义错误码和消息构造异常
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public DataException(String code, String message) {
        super(code, message);
    }

    /**
     * 创建 SQL 执行失败异常
     */
    public static DataException sqlExecutionFailed(String sql, String reason) {
        return new DataException(
                DataErrorCode.SQL_EXECUTION_FAILED.getCode(),
                "SQL执行失败: " + sql + ", 原因: " + reason
        );
    }

    /**
     * 创建数据重复异常
     */
    public static DataException duplicateData(String field, Object value) {
        return new DataException(
                DataErrorCode.DUPLICATE_DATA.getCode(),
                "数据重复: " + field + " = " + value
        );
    }

    /**
     * 创建数据不存在异常
     */
    public static DataException dataNotFound(String table, Object id) {
        return new DataException(
                DataErrorCode.DATA_NOT_FOUND.getCode(),
                table + " 数据不存在: " + id
        );
    }

    /**
     * 创建分页参数错误异常
     */
    public static DataException paginationParamError(int pageNum, int pageSize) {
        return new DataException(
                DataErrorCode.PAGINATION_PARAM_ERROR.getCode(),
                "分页参数错误: pageNum=" + pageNum + ", pageSize=" + pageSize
        );
    }

    /**
     * 创建乐观锁冲突异常
     */
    public static DataException optimisticLockConflict(String table, Object id) {
        return new DataException(
                DataErrorCode.OPTIMISTIC_LOCK_CONFLICT.getCode(),
                "乐观锁冲突: " + table + " id=" + id + ", 数据已被修改"
        );
    }

    /**
     * 创建事务操作失败异常
     */
    public static DataException transactionFailed(String reason) {
        return new DataException(
                DataErrorCode.TRANSACTION_FAILED.getCode(),
                "事务操作失败: " + reason
        );
    }

    /**
     * 创建连接池耗尽异常
     */
    public static DataException connectionPoolExhausted() {
        return new DataException(
                DataErrorCode.CONNECTION_POOL_EXHAUSTED.getCode(),
                "数据库连接池耗尽"
        );
    }

    /**
     * 创建慢查询异常
     */
    public static DataException slowQuery(String sql, long costTime) {
        return new DataException(
                DataErrorCode.SLOW_QUERY.getCode(),
                "慢查询: " + sql + ", 耗时: " + costTime + "ms"
        );
    }
}