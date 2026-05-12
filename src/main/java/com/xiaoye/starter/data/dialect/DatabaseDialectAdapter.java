package com.xiaoye.starter.data.dialect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据库方言适配服务
 */
@Slf4j
@Component
public class DatabaseDialectAdapter {

    /**
     * 获取分页SQL
     */
    public String getPaginationSql(String originalSql, int offset, int limit, String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return originalSql + " LIMIT " + offset + ", " + limit;
            case "oracle":
                return "SELECT * FROM (" + originalSql + ") WHERE ROWNUM <= " + (offset + limit);
            case "postgresql":
                return originalSql + " LIMIT " + limit + " OFFSET " + offset;
            default:
                return originalSql;
        }
    }

    /**
     * 获取当前日期函数
     */
    public String getCurrentDateFunction(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return "NOW()";
            case "oracle":
                return "SYSDATE";
            case "postgresql":
                return "CURRENT_TIMESTAMP";
            default:
                return "NOW()";
        }
    }
}
