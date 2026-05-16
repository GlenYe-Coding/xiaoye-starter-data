package com.xiaoye.starter.data.core;

import com.xiaoye.common.core.enums.ErrorCode;

/**
 * Data 模块错误码枚举
 * <p>
 * 3xxx: 数据相关错误
 * 31xx: SQL 相关
 * 32xx: 分页相关
 * 33xx: 事务相关
 * </p>
 *
 * @author xiaoye
 * @since 1.0.0
 * @deprecated 请使用 {@link ErrorCode}，该类仅用于向后兼容，将在后续版本中移除
 */
@Deprecated
public enum DataErrorCode {

    // ========== SQL 相关 31xx ==========
    SQL_EXECUTION_FAILED("3100", "SQL执行失败"),
    SQL_SYNTAX_ERROR("3101", "SQL语法错误"),
    DUPLICATE_DATA("3102", "数据重复"),
    DATA_NOT_FOUND("3103", "数据不存在"),
    DATA_CONSTRAINT_VIOLATION("3104", "数据约束违反"),

    // ========== 分页相关 32xx ==========
    PAGINATION_PARAM_ERROR("3200", "分页参数错误"),
    TOTAL_COUNT_FAILED("3201", "获取总数失败"),
    QUERY_TIME_OUT("3202", "查询超时"),

    // ========== 事务相关 33xx ==========
    TRANSACTION_FAILED("3300", "事务操作失败"),
    TRANSACTION_TIMEOUT("3301", "事务超时"),
    TRANSACTION_ROLLBACK("3302", "事务回滚"),
    OPTIMISTIC_LOCK_CONFLICT("3303", "乐观锁冲突"),

    // ========== 连接相关 34xx ==========
    CONNECTION_POOL_EXHAUSTED("3400", "数据库连接池耗尽"),
    CONNECTION_TIMEOUT("3401", "数据库连接超时"),
    DATABASE_UNAVAILABLE("3402", "数据库不可用"),

    // ========== 性能相关 35xx ==========
    SLOW_QUERY("3500", "慢查询"),
    LARGE_RESULT_SET("3501", "结果集过大");

    private final String code;
    private final String message;

    DataErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return 对应的枚举值，未找到返回 null
     */
    public static DataErrorCode fromCode(String code) {
        for (DataErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 转换为统一的 ErrorCode
     * <p>
     * 用于向后兼容，将 DataErrorCode 映射到 ErrorCode
     * </p>
     *
     * @return 对应的 ErrorCode，如果无映射则返回 null
     */
    public ErrorCode toErrorCode() {
        switch (this) {
            case SQL_EXECUTION_FAILED:
            case SQL_SYNTAX_ERROR:
                return ErrorCode.DATABASE_ERROR;
            case DUPLICATE_DATA:
                return ErrorCode.DUPLICATE_DATA;
            case DATA_NOT_FOUND:
                return ErrorCode.DATA_NOT_FOUND;
            case DATA_CONSTRAINT_VIOLATION:
                return ErrorCode.DATABASE_ERROR;
            case PAGINATION_PARAM_ERROR:
                return ErrorCode.PARAM_ERROR;
            case TOTAL_COUNT_FAILED:
            case QUERY_TIME_OUT:
                return ErrorCode.TIMEOUT_ERROR;
            case TRANSACTION_FAILED:
            case TRANSACTION_TIMEOUT:
            case TRANSACTION_ROLLBACK:
                return ErrorCode.SYSTEM_ERROR;
            case OPTIMISTIC_LOCK_CONFLICT:
                return ErrorCode.CONFLICT;
            case CONNECTION_POOL_EXHAUSTED:
            case CONNECTION_TIMEOUT:
            case DATABASE_UNAVAILABLE:
                return ErrorCode.DATABASE_ERROR;
            case SLOW_QUERY:
            case LARGE_RESULT_SET:
                return ErrorCode.SYSTEM_BUSY;
            default:
                return null;
        }
    }
}