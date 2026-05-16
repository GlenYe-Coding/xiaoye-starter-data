# XiaoYe Starter Data - 数据持久化模块

## 概述

`xiaoye-starter-data` 基于 MyBatis Plus 提供增强的数据持久化功能，包括自动填充、分页、租户隔离等。

## 特性

- ✅ **MyBatis Plus 增强**: 简化 CRUD 操作
- ✅ **自动填充**: 自动填充创建时间、更新时间等字段
- ✅ **分页支持**: 内置分页插件
- ✅ **租户隔离**: 多租户数据隔离
- ✅ **慢 SQL 监控**: 自动检测慢查询

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.xiaoye</groupId>
    <artifactId>xiaoye-starter-data</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xiaoye?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.xiaoye.**.entity
  configuration:
    map-underscore-to-camel-case: true
```

### 3. 使用 BaseServiceImpl

```java
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {
    // 继承基础 CRUD 方法
}
```

## 核心功能

### 1. BaseServiceImpl - 简化 CRUD

继承 `BaseServiceImpl` 即可获得完整的 CRUD 能力：

```java
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {
    // 自动拥有以下方法：
    // - save(), saveBatch() - 保存
    // - removeById(), removeBatchByIds() - 删除
    // - updateById(), updateBatchById() - 更新
    // - getById(), listByIds() - 查询
    // - page(), list() - 分页和列表查询
}
```

**常用方法：**

```java
// 保存
User user = new User();
user.setUsername("张三");
userService.save(user);

// 批量保存
List<User> users = Arrays.asList(user1, user2, user3);
userService.saveBatch(users, 100); // 每批100条

// 条件查询
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, "张三")
       .like(User::getEmail, "@gmail.com")
       .orderByDesc(User::getCreateTime);
List<User> users = userService.list(wrapper);

// 分页查询
Page<User> page = new Page<>(1, 20);
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(User::getUsername, keyword);
Page<User> result = userService.page(page, wrapper);

// 条件更新
LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(User::getId, userId)
       .set(User::getStatus, 1);
userService.update(wrapper);

// 条件删除
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, 0)
       .lt(User::getCreateTime, LocalDateTime.now().minusDays(30));
userService.remove(wrapper);
```

### 2. 自动填充 (AutoFillMetaObjectHandler)

自动填充审计字段，无需手动设置：

```java
@Data
@TableName("user")
public class User extends AuditEntity {
    private String username;
    private String email;
    
    // 以下字段会自动填充：
    // - createTime: 创建时间
    // - createBy: 创建人
    // - updateTime: 更新时间
    // - updateBy: 更新人
    // - deleted: 逻辑删除标记
}
```

**配置自动填充：**

```yaml
mybatis-plus:
  global-config:
    db-config:
      # 逻辑删除配置
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      # ID 生成策略
      id-type: auto  # AUTO/INPUT/ASSIGN_ID/ASSIGN_UUID
```

**自定义填充规则：**

```java
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUserId());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUserId());
    }
    
    private String getCurrentUserId() {
        // 从上下文获取当前用户ID
        return AuthContextHolder.getUserId();
    }
}
```

### 3. 分页查询

内置分页插件，支持多种数据库：

```java
@Service
public class ArticleService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    /**
     * 分页查询文章
     */
    public Page<ArticleVO> pageArticles(int pageNum, int pageSize, String keyword) {
        // 创建分页对象
        Page<Article> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Article::getTitle, keyword)
                   .or()
                   .like(Article::getContent, keyword);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        
        // 执行分页查询
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<ArticleVO> voPage = new Page<>();
        BeanUtils.copyProperties(result, voPage);
        voPage.setRecords(convertToVO(result.getRecords()));
        
        return voPage;
    }
}
```

**分页响应格式：**

```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

### 4. 租户隔离 (TenantInterceptor)

多租户数据隔离，自动添加租户条件：

```yaml
xiaoye:
  data:
    tenant:
      enabled: true
      column: tenant_id  # 租户字段名
      # 忽略租户条件的表
      ignore-tables:
        - sys_dict
        - sys_config
```

**使用示例：**

```java
// 自动添加 WHERE tenant_id = ?
List<User> users = userService.list();

// 在请求拦截器中设置租户ID
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 从 Token 或 Session 中获取租户ID
        Long tenantId = TenantContext.getTenantId();
        TenantInterceptor.setTenantId(tenantId);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        // 清理租户上下文
        TenantInterceptor.clear();
    }
}
```

### 5. 慢 SQL 监控

自动检测并记录慢查询：

```yaml
xiaoye:
  data:
    monitor:
      enabled: true
      slow-sql-threshold: 1000  # 慢SQL阈值（毫秒）
      log-slow-sql: true        # 是否记录慢SQL
```

**监控输出：**

```
[WARN] Slow SQL detected: SELECT * FROM user WHERE status = 1
Execution time: 1523ms
Threshold: 1000ms
```

### 6. 加密字段 (EncryptedTypeHandler)

敏感字段自动加密存储：

```java
@Data
@TableName("user")
public class User {
    private Long id;
    private String username;
    
    // 手机号自动加密存储
    @TableField(typeHandler = EncryptedTypeHandler.class)
    private String phone;
    
    // 身份证自动加密存储
    @TableField(typeHandler = EncryptedTypeHandler.class)
    private String idCard;
}
```

**配置加密密钥：**

```yaml
xiaoye:
  data:
    encryption:
      key: ${ENCRYPTION_KEY:your-secret-key}
      algorithm: AES
```

## 高级用法

### 自定义 Mapper 方法

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名模糊查询
     */
    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    List<User> searchByUsername(@Param("keyword") String keyword);
    
    /**
     * 批量更新状态
     */
    @Update("UPDATE user SET status = #{status} WHERE id IN (${ids})")
    int batchUpdateStatus(@Param("ids") String ids, @Param("status") Integer status);
}
```

### XML Mapper

```xml
<!-- UserMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.xiaoye.mapper.UserMapper">
    
    <select id="selectUserWithRole" resultType="com.xiaoye.entity.User">
        SELECT u.*, r.role_name
        FROM user u
        LEFT JOIN user_role ur ON u.id = ur.user_id
        LEFT JOIN role r ON ur.role_id = r.id
        WHERE u.id = #{userId}
    </select>
    
</mapper>
```

### 事务管理

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    /**
     * 创建订单（事务）
     */
    public void createOrder(Order order, List<OrderItem> items) {
        // 保存订单
        orderMapper.insert(order);
        
        // 保存订单项
        items.forEach(item -> {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        });
        
        // 如果任何一步失败，整个事务回滚
    }
}

## License

MIT License
