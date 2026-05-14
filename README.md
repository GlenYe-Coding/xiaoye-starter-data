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

### 自动填充

```java
@Data
@TableName("user")
public class User extends AuditEntity {
    private String username;
    private String email;
    // createTime, createBy, updateTime, updateBy 自动填充
}
```

### 分页查询

```java
Page<User> page = new Page<>(1, 20);
userService.page(page);
```

### 租户隔离

```yaml
xiaoye:
  data:
    tenant:
      enabled: true
      column: tenant_id
```

## License

MIT License
