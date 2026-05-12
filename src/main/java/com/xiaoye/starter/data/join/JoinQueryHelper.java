package com.xiaoye.starter.data.join;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 联表查询封装服务
 */
@Slf4j
@Component
public class JoinQueryHelper {

    /**
     * 执行LEFT JOIN查询
     */
    public <T> List<T> leftJoinQuery(String mainTable, String joinTable, String joinCondition, Class<T> resultType) {
        log.info("执行LEFT JOIN查询: {} LEFT JOIN {} ON {}", mainTable, joinTable, joinCondition);
        // TODO: 构建JOIN查询SQL并执行
        return null;
    }

    /**
     * 执行INNER JOIN查询
     */
    public <T> List<T> innerJoinQuery(String mainTable, String joinTable, String joinCondition, Class<T> resultType) {
        log.info("执行INNER JOIN查询: {} INNER JOIN {} ON {}", mainTable, joinTable, joinCondition);
        // TODO: 构建JOIN查询SQL并执行
        return null;
    }

    /**
     * 批量关联查询结果
     */
    public void batchAssociate(List<?> mainList, List<?> joinList, String foreignKey) {
        log.info("批量关联查询结果，外键: {}", foreignKey);
        // TODO: 根据外键关联两个列表的数据
    }
}
