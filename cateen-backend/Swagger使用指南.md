# Swagger/Knife4j 使用指南

## 📚 访问地址

项目启动后，访问以下地址：

### Knife4j增强文档（推荐）
```
http://localhost:8080/api/doc.html
```

### 原生Swagger UI
```
http://localhost:8080/api/swagger-ui/
或
http://localhost:8080/api/swagger-ui/index.html
```

### Swagger JSON API文档
```
http://localhost:8080/api/v2/api-docs
```

---

## 🚀 快速使用

### 1. 启动项目

```bash
mvn spring-boot:run
```

### 2. 打开文档页面

浏览器访问：**http://localhost:8080/api/doc.html**

### 3. 登录获取Token

#### 步骤1：找到"1. 认证模块" -> "用户登录"接口

#### 步骤2：点击"调试"按钮

#### 步骤3：输入测试数据
```json
{
  "username": "admin",
  "password": "admin123"
}
请求头：Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VySWQiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU5NTYyNTkzLCJleHAiOjE3NTk2NDg5OTN9.bc4DyY0otpZbR7NgE8T5bG-fLdrDOB_KdWknmgjLur8
```

#### 步骤4：点击"发送"，复制返回的Token

响应示例：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJvbGUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNjk2MzI5NjAwLCJleHAiOjE2OTY0MTYwMDB9.xxx",
    "user": {
      "id": "1",
      "username": "admin",
      "role": "admin",
      "status": "active"
    }
  }
}
```

### 4. 配置全局Token

#### 方法一：使用Knife4j的全局参数（推荐）

1. 点击页面右上角的**"Authorize"**按钮（或锁形图标🔒）
2. 在弹出的窗口中输入：`Bearer {你的token}`
   - 注意：**Bearer** 和 **token** 之间有一个空格
   - 例如：`Bearer eyJhbGciOiJIUzI1NiJ9...`
3. 点击"Authorize"确认
4. 之后所有需要认证的接口都会自动带上这个Token

#### 方法二：在每个接口单独设置

在每个接口的请求头中添加：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 5. 测试其他接口

配置好Token后，就可以测试其他需要认证的接口了，比如：
- 获取美食列表
- 创建美食
- 管理评论
- 等等...

---

## 🎯 功能特性

### Knife4j增强功能

1. **更美观的UI**：比原生Swagger更现代化
2. **中文界面**：已配置为中文
3. **离线文档**：支持导出Markdown、HTML等格式
4. **调试增强**：
   - 支持文件上传
   - 支持全局参数
   - 支持请求/响应高亮
5. **个性化设置**：
   - 主题切换
   - 语言切换
   - 请求参数缓存

---

## 📖 接口文档说明

### 模块分类

所有接口按功能分为8个模块：

1. **认证模块**：登录、注册、密码管理
2. **用户管理**：用户CRUD、角色管理
3. **美食管理**：美食CRUD、状态管理
4. **美食分类管理**：分类CRUD
5. **美食标签管理**：标签CRUD
6. **评论管理**：评论CRUD、审核
7. **榜单管理**：榜单CRUD
8. **统计数据**：仪表盘统计、分类统计

### 接口标注说明

- 🔓 **无需认证**：登录、注册接口
- 🔒 **需要认证**：其他所有接口都需要Token
- 👑 **需要管理员权限**：用户管理、审核等接口

---

## 🔧 常见问题

### 1. Token过期怎么办？

Token默认有效期为24小时。过期后：
1. 重新登录获取新Token
2. 更新全局Token配置

### 2. 接口返回401错误

原因：
- 未配置Token
- Token格式错误（检查是否有"Bearer "前缀）
- Token已过期

解决：重新登录获取Token

### 3. 接口返回403错误

原因：权限不足（例如普通用户访问管理员接口）

解决：使用管理员账户登录

### 4. 看不到Swagger文档

检查：
1. 项目是否正常启动
2. 访问地址是否正确
3. 端口是否被占用

### 5. 如何导出API文档？

在Knife4j页面：
1. 点击"文档管理"
2. 选择"离线文档"
3. 选择导出格式（Markdown/HTML/Word）
4. 点击"下载"

---

## 💡 高级用法

### 1. 批量测试接口

使用Postman或其他工具导入Swagger JSON：
```
http://localhost:8080/v2/api-docs
```

### 2. 自定义Swagger配置

修改 `Knife4jConfig.java` 文件：
- API信息
- 联系方式
- 安全配置
- 等等

### 3. 添加自定义注解

在Controller方法上使用：
- `@ApiOperation`：接口说明
- `@ApiImplicitParam`：参数说明
- `@ApiModel`：模型说明
- `@ApiModelProperty`：字段说明

---

## 📝 测试用例

### 完整测试流程

#### 1. 用户登录
```http
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

#### 2. 查询美食列表
```http
GET /api/foods?pageNum=1&pageSize=10
Authorization: Bearer {token}
```

#### 3. 创建美食
```http
POST /api/foods
Authorization: Bearer {token}
{
  "name": "宫保鸡丁",
  "description": "经典川菜",
  "categoryId": "cat_002",
  "price": 32.00,
  "tagIds": ["tag_001", "tag_004"]
}
```

#### 4. 创建评论
```http
POST /api/comments
Authorization: Bearer {token}
{
  "foodId": "food_001",
  "content": "非常好吃！",
  "rating": 5
}
```

#### 5. 审核评论
```http
PUT /api/comments/{id}/status
Authorization: Bearer {token}
{
  "status": "approved"
}
```

---

## 🎓 学习资源

- **Knife4j官方文档**：https://doc.xiaominfo.com/
- **Swagger官方文档**：https://swagger.io/docs/
- **Spring Boot集成Swagger**：https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api

---

## 📞 技术支持

如有问题，请查看：
- 项目README
- 后端接口文档
- 本文档

---

**祝您使用愉快！** 🎉

