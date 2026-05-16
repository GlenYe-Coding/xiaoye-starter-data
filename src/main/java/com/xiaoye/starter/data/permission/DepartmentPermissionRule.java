package com.xiaoye.starter.data.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 部门级数据权限规则
 * <p>
 * 根据当前用户所属部门及其子部门进行数据权限过滤
 * </p>
 *
 * @author XiaoYe
 * @since 1.0.0
 */
public class DepartmentPermissionRule implements DataPermissionRule {

    private static final Logger log = LoggerFactory.getLogger(DepartmentPermissionRule.class);

    /**
     * 默认部门权限列名
     */
    private static final String DEFAULT_COLUMN = "dept_id";

    /**
     * 默认匹配的表名
     */
    private static final String[] DEFAULT_TABLES = {"sys_user", "sys_role", "sys_post"};

    @Override
    public String getName() {
        return "department";
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
        Long[] deptIds = DataPermissionContext.getCurrentDeptIds();
        if (deptIds == null || deptIds.length == 0) {
            return "1=0";
        }

        String column = DEFAULT_COLUMN;
        String tableAlias = alias != null ? alias : tableName;
        String fullColumn = tableAlias + "." + column;

        if (deptIds.length == 1) {
            return fullColumn + " = " + deptIds[0];
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(fullColumn).append(" IN (");
            for (int i = 0; i < deptIds.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(deptIds[i]);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    @Override
    public Object[] getPermissionValues(String column) {
        return DataPermissionContext.getCurrentDeptIds();
    }

    @Override
    public boolean isSkipCheck() {
        if (DataPermissionContext.checkAdmin()) {
            return true;
        }
        // 如果用户没有部门信息，默认无权限
        return DataPermissionContext.getCurrentDeptIds() == null || DataPermissionContext.getCurrentDeptIds().length == 0;
    }
}