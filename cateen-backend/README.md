# 美食评估管理系统后端

## 项目简介

美食评估管理系统后端服务，基于Spring Boot + MyBatis-Plus + MySQL开发，提供完整的RESTful API接口。

## 技术栈

- **Java**: 11
- **Spring Boot**: 2.7.14
- **MyBatis-Plus**: 3.5.3.1
- **MySQL**: 8.0+
- **Redis**: 5.0+
- **JWT**: 0.11.5
- **Druid**: 1.2.16

## 项目结构

```
cateen-backend/
├── src/main/java/com/xawl/cateen/
│   ├── CateenApplication.java          # 启动类
│   ├── common/                          # 通用类
│   │   ├── Result.java                  # 统一响应结果
│   │   └── ResultCode.java              # 响应状态码
│   ├── config/                          # 配置类
│   │   ├── CorsConfig.java              # 跨域配置
│   │   ├── MyBatisPlusConfig.java       # MyBatis-Plus配置
│   │   └── WebMvcConfig.java            # Web MVC配置
│   ├── constant/                        # 常量类
│   │   ├── CommentStatusConstants.java  # 评论状态常量
│   │   ├── RoleConstants.java           # 角色常量
│   │   └── StatusConstants.java         # 状态常量
│   ├── controller/                      # 控制器层
│   ├── dto/                            # 数据传输对象
│   ├── entity/                         # 实体类
│   ├── exception/                      # 异常处理
│   │   ├── BusinessException.java       # 业务异常
│   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   ├── interceptor/                    # 拦截器
│   │   └── AuthInterceptor.java         # 认证拦截器
│   ├── mapper/                         # Mapper接口
│   ├── service/                        # 服务层
│   ├── util/                           # 工具类
│   │   ├── JwtUtil.java                 # JWT工具类
│   │   ├── PasswordUtil.java            # 密码工具类
│   │   └── UserContext.java             # 用户上下文
│   └── vo/                             # 视图对象
├── src/main/resources/
│   ├── mapper/xml/                      # MyBatis XML映射文件
│   ├── sql/                            # SQL脚本
│   │   ├── schema.sql                   # 建表脚本
│   │   └── data.sql                     # 测试数据脚本
│   ├── application.yml                  # 主配置文件
│   ├── application-dev.yml              # 开发环境配置
│   └── application-prod.yml             # 生产环境配置
└── pom.xml                             # Maven配置文件
```

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 2. 数据库初始化

```bash
# 创建数据库并执行初始化脚本
mysql -u root -p < src/main/resources/sql/schema.sql
mysql -u root -p < src/main/resources/sql/data.sql
```

### 3. 修改配置

编辑 `src/main/resources/application-dev.yml`，修改数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cateen?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 4. 启动项目

```bash
# 使用Maven编译并运行
mvn clean install
mvn spring-boot:run

# 或者使用IDE直接运行 CateenApplication.java
```

项目启动后，访问：http://localhost:8080/api

## API文档

详细的API接口文档请参考项目根目录下的 `后端接口文档.md`

### 测试账户

#### 管理员账户
- 用户名：admin
- 密码：admin123

#### 普通用户账户
- 用户名：testuser
- 密码：123456

## 开发规范

### 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 类名使用PascalCase
- 方法名和变量名使用camelCase
- 常量使用UPPER_SNAKE_CASE

### 接口规范

- 使用RESTful API风格
- 统一响应格式（Result类）
- 统一异常处理
- 参数校验使用@Valid注解

### 数据库规范

- 表名使用小写+下划线
- 字段名使用小写+下划线
- 所有表必须有主键
- 重要表必须有created_at和updated_at字段

## 核心功能

### 1. 认证模块
- 用户登录/注册
- JWT Token认证
- 密码加密（BCrypt）

### 2. 用户管理
- 用户列表查询（分页）
- 用户角色管理
- 用户状态管理

### 3. 美食管理
- 美食CRUD操作
- 美食分类管理
- 美食标签管理
- 美食状态管理

### 4. 评论管理
- 评论CRUD操作
- 评论审核
- 评论统计

### 5. 榜单管理
- 榜单CRUD操作
- 榜单美食关联
- 榜单排序

### 6. 统计分析
- 仪表盘统计
- 热门美食统计
- 用户活动统计
- 评分统计

## 部署说明

### 1. 打包项目

```bash
mvn clean package -DskipTests
```

### 2. 运行JAR包

```bash
java -jar target/cateen-backend-1.0.0.jar --spring.profiles.active=prod
```

### 3. 使用Docker部署（可选）

```bash
# 构建Docker镜像
docker build -t cateen-backend:1.0.0 .

# 运行Docker容器
docker run -d -p 8080:8080 --name cateen-backend cateen-backend:1.0.0
```

## 常见问题

### 1. 数据库连接失败

检查MySQL服务是否启动，确认配置文件中的连接信息是否正确。

### 2. Token验证失败

检查请求头中是否包含正确的Authorization字段，格式为：`Bearer {token}`

### 3. 跨域问题

项目已配置CORS，如果仍有问题，检查前端请求地址是否正确。

## 更新日志

### v1.0.0 (2025-10-03)
- ✅ 初始版本发布
- ✅ 完成用户认证模块
- ✅ 完成用户管理模块
- ✅ 完成美食管理模块
- ✅ 完成评论管理模块
- ✅ 完成榜单管理模块
- ✅ 完成统计分析模块

## 许可证

MIT License

## 联系方式

如有问题或建议，请联系开发团队。

