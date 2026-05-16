package com.xiaoye.starter.data.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 区域级数据权限规则
 * <p>
 * 根据当前用户所属区域及其子区域进行数据权限过滤
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
public class RegionPermissionRule implements DataPermissionRule {

    private static final Logger log = LoggerFactory.getLogger(RegionPermissionRule.class);

    /**
     * 默认区域权限列名
     */
    private static final String DEFAULT_COLUMN = "region_code";

    /**
     * 默认匹配的表名
     */
    private static final String[] DEFAULT_TABLES = {"sys_order", "sys_region"};

    @Override
    public String getName() {
        return "region";
    }

    @Override
    public String[] getTables() {
        return DEFAULT_TABLES;
    }

    @Override
    public String getColumn(String tableName) {
        return DEFAULT_COLUMN;
    }

    @Override
    public String getCondition(String tableName, String alias) {
        String[] regionCodes = DataPermissionContext.getCurrentRegionCodes();
        if (regionCodes == null || regionCodes.length == 0) {
            return "1=0";
        }

        String column = DEFAULT_COLUMN;
        String tableAlias = alias != null ? alias : tableName;
        String fullColumn = tableAlias + "." + column;

        if (regionCodes.length == 1) {
            return fullColumn + " = '" + escapeSql(regionCodes[0]) + "'";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(fullColumn).append(" IN (");
            for (int i = 0; i < regionCodes.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("'").append(escapeSql(regionCodes[i])).append("'");
            }
            sb.append(")");
            return sb.toString();
        }
    }

    @Override
    public Object[] getPermissionValues(String column) {
        return DataPermissionContext.getCurrentRegionCodes();
    }

    @Override
    public boolean isSkipCheck() {
        if (DataPermissionContext.checkAdmin()) {
            return true;
        }
        // 如果用户没有区域信息，默认无权限
        return DataPermissionContext.getCurrentRegionCodes() == null || DataPermissionContext.getCurrentRegionCodes().length == 0;
    }

    /**
     * SQL 注入防护
     */
    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }
}