# 🍽️ 智慧食堂美食评估系统

> 一个集美食浏览、评分评论、社区分享、AI 推荐、幸运抽签于一体的食堂美食评估与管理平台。

## 📖 项目简介

智慧食堂美食评估系统是一套完整的食堂美食数字化管理解决方案，包含三个核心子系统：

- **微信小程序**（用户端）：面向学生/食客，提供美食浏览、评分评论、社区分享、AI 智能推荐、幸运抽签等功能
- **Spring Boot 后端**：提供 RESTful API 服务，处理业务逻辑、数据存储、AI 集成
- **React 管理后台**（管理端）：面向管理员，提供美食管理、评论审核、用户管理、数据统计等功能

## ✨ 核心功能

### 🍜 美食浏览与搜索
- 按分类（主食、菜肴、汤羹、小吃、饮品）浏览美食
- 关键词搜索，支持搜索历史记录
- 无限滚动加载 + 下拉刷新
- 热门美食推荐

### ⭐ 评分与评论
- 1-5 星评分系统
- 图文评论，支持图片上传
- 评论点赞互动
- 评分实时更新美食均分

### 👥 社区分享（论坛）
- 发布图文/视频帖子
- 帖子点赞、评论互动
- 内容审核管理

### 🤖 AI 智能助手
- 基于通义千问（Qwen-Plus）大模型
- 根据用户偏好智能推荐美食
- 支持管理端 AI 数据分析与报表生成
- LangChain4j 函数调用（Tool Calling）集成

### 🎰 幸运抽签
- 随机抽取美食，解决"今天吃什么"的选择困难
- 支持按类别/评分筛选

### 📊 数据统计（管理端）
- 美食、用户、评论、评分多维度统计
- 可视化图表展示（Recharts）
- Excel / PPT 报表导出（Apache POI）

### 🔐 安全与权限
- 微信 OAuth 一键登录
- JWT 双拦截器认证（强制认证 + 可选认证）
- RBAC 角色权限控制（管理员 / 普通用户）
- 评论内容审核机制

## 🛠️ 技术栈

### 后端（cateen-backend）

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 2.7.14 | 核心框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | - | 缓存 / AI 会话存储 |
| MinIO | - | 对象存储（图片/视频） |
| JWT (jjwt) | 0.11.5 | 身份认证 |
| LangChain4j | 0.29.1 | AI 集成框架 |
| 通义千问 (Qwen-Plus) | - | 大语言模型 |
| Knife4j | - | API 文档（Swagger） |
| Druid | - | 数据库连接池 |
| Apache POI | - | Excel/PPT 报表生成 |

### 小程序（cateen-mini）

| 技术 | 说明 |
|------|------|
| 微信小程序原生框架 | WXML + WXSS + JS |
| Vant Weapp | UI 组件库 |
| glass-easel | 组件框架 |

### 管理后台（cateen-fronted）

| 技术 | 说明 |
|------|------|
| React 18 + TypeScript | 前端框架 |
| Vite 5 | 构建工具 |
| Tailwind CSS 3 | 原子化 CSS |
| shadcn/ui (Radix UI) | UI 组件库 |
| React Router v6 | 路由管理 |
| Recharts | 图表可视化 |
| React Hook Form + Zod | 表单与校验 |

## 📁 项目结构

```
xawl_cateen/
├── cateen-backend/                # Spring Boot 后端服务
│   └── src/main/java/com/xawl/cateen/
│       ├── config/                # 配置类（CORS、拦截器、MinIO、AI 等）
│       ├── common/                # 通用响应封装（Result、ResultCode）
│       ├── controller/            # 控制器
│       │   ├── admin/             #   管理端 API（/api/admin/*）
│       │   └── mini/              #   小程序 API（/api/mini/*）
│       ├── service/               # 业务服务层
│       │   ├── impl/              #   核心服务实现
│       │   ├── mini/              #   小程序专用服务
│       │   ├── tools/             #   LangChain4j @Tool 函数
│       │   └── storage/           #   文件上传服务（MinIO/本地）
│       ├── entity/                # 数据库实体
│       ├── mapper/                # MyBatis-Plus 映射器
│       ├── dto/                   # 数据传输对象
│       ├── vo/                    # 视图对象
│       ├── interceptor/           # JWT 认证拦截器
│       ├── util/                  # 工具类
│       └── exception/             # 异常处理
├── cateen-mini/                   # 微信小程序
│   ├── pages/                     # 页面
│   │   ├── index/                 #   首页（美食列表）
│   │   ├── detail/                #   美食详情
│   │   ├── ranking/               #   社区分享
│   │   ├── publish/               #   发布帖子
│   │   ├── ai-assistant/          #   AI 助手
│   │   ├── lucky/                 #   幸运抽签
│   │   ├── profile/               #   个人中心
│   │   ├── collections/           #   我的收藏
│   │   └── comments/              #   我的评论
│   ├── components/                # 自定义组件
│   │   ├── navigation-bar/        #   自定义导航栏
│   │   ├── food-card/             #   美食卡片
│   │   ├── rating-star/           #   星级评分
│   │   ├── ai-assistant-button/   #   AI 悬浮按钮
│   │   ├── ai-chat-modal/         #   AI 聊天弹窗
│   │   └── custom-tab-bar/        #   自定义底部导航
│   └── utils/                     # 工具函数
│       ├── api.js                 #   API 请求封装
│       ├── config.js              #   环境配置
│       ├── auth.js                #   认证工具
│       └── util.js                #   通用工具
├── cateen-fronted/                # React 管理后台
│   └── app-6gulfhglmz29/
│       └── src/
│           ├── pages/             # 管理页面
│           ├── components/        # UI 组件
│           ├── services/          # API 服务
│           └── types/             # TypeScript 类型
├── uploads/                       # 本地文件上传目录
├── 开发环境配置说明.md             # 开发环境配置指南
└── 评分功能问题排查指南.md         # 评分功能调试指南
```

## 🚀 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| MinIO | （可选，本地存储可替代） |
| Node.js | 20+（管理后台） |
| 微信开发者工具 | 最新稳定版 |

### 1. 启动后端服务

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS cateen DEFAULT CHARSET utf8mb4;"

# 2. 修改配置文件
#    编辑 cateen-backend/src/main/resources/application-dev.yml
#    配置 MySQL 连接、Redis 连接、MinIO 地址
#    配置微信小程序 AppID 和 Secret

# 3. 启动服务
cd cateen-backend
mvn spring-boot:run

# 服务运行在 http://localhost:8080
# API 文档：http://localhost:8080/doc.html
```

### 2. 配置小程序

```bash
# 1. 用微信开发者工具打开 cateen-mini 目录

# 2. 修改 utils/config.js 中的 API 地址
#    本地开发：http://localhost:8080/api/mini
#    局域网调试：http://<你的IP>:8080/api/mini（需同时修改后端 base-url）

# 3. 在开发者工具中勾选「不校验合法域名」

# 4. 编译运行
```

> 详细配置说明请参考 [开发环境配置说明.md](./开发环境配置说明.md)

### 3. 启动管理后台

```bash
cd cateen-fronted/app-6gulfhglmz29
npm install
npm run dev
```

## 📡 API 概览

### 小程序端 API（`/api/mini/*`）

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `POST /api/mini/auth/login` | 微信登录 |
| 美食 | `GET /api/mini/foods` | 美食列表（分页/排序/筛选） |
| 美食 | `GET /api/mini/foods/{id}` | 美食详情 |
| 美食 | `POST /api/mini/foods/{id}/comments` | 发表评论 |
| 评论 | `POST /api/mini/comments/{id}/like` | 评论点赞 |
| 排行 | `GET /api/mini/ranking` | 美食排行榜 |
| 抽签 | `GET /api/mini/lucky` | 幸运抽签 |
| 收藏 | `POST /api/mini/user/collections` | 收藏美食 |
| 论坛 | `GET/POST /api/mini/forum/posts` | 论坛帖子 |
| 上传 | `POST /api/mini/upload` | 图片/视频上传 |
| AI | `POST /api/ai/chat` | AI 对话（流式） |

### 管理端 API（`/api/admin/*`）

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `POST /api/admin/auth/login` | 管理员登录 |
| 美食 | `CRUD /api/admin/foods` | 美食管理 |
| 分类 | `CRUD /api/admin/categories` | 分类管理 |
| 标签 | `CRUD /api/admin/tags` | 标签管理 |
| 评论 | `PUT /api/admin/comments/{id}` | 评论审核 |
| 用户 | `CRUD /api/admin/users` | 用户管理 |
| 排行 | `CRUD /api/admin/rankings` | 排行榜管理 |
| 统计 | `GET /api/admin/stats` | 仪表盘统计 |
| AI | `POST /api/admin/ai` | 管理端 AI 助手 |

## 🗄️ 数据库设计

系统包含 15 张数据表，核心表关系如下：

```
food_categories ──┐
food_tags ──┐     │
             ├── foods ──┬── comments ──┬── comment_like
             │           │              └── comment_preferences
             │           ├── collection (用户收藏)
             │           └── ranking_foods
             │
profiles ────┼── forum_posts ──┬── forum_comments
             │                 └── forum_likes
             └── search_history

rankings ──── ranking_foods
```

## 🤖 AI 功能说明

系统基于 **LangChain4j** 集成了**阿里云通义千问（Qwen-Plus）**大模型，通过 Function Calling 机制让 AI 能够实时查询数据库：

- **小程序 AI 助手**：根据用户需求从数据库中检索美食并智能推荐
- **管理端 AI 助手**：提供数据统计分析、报表生成等智能查询

配置位于 `application.yml` 的 `langchain4j` 和 `custom.ai` 节点。

## ⚙️ 配置说明

| 配置文件 | 用途 |
|----------|------|
| `application.yml` | 主配置（JWT、MyBatis-Plus、MinIO、AI） |
| `application-dev.yml` | 开发环境（数据库连接、上传路径） |
| `application-prod.yml` | 生产环境 |
| `cateen-mini/utils/config.js` | 小程序 API 地址（local/lan/prod） |

> ⚠️ **安全提醒**：请勿将 `application.yml` 中的密钥（微信 AppSecret、AI API Key、MinIO 密钥等）提交到公开仓库。生产环境建议使用环境变量或配置中心管理敏感信息。

## 🐛 常见问题

### 小程序图片无法显示？
1. 检查 `application-dev.yml` 中 `upload.base-url` 是否为局域网 IP
2. 微信开发者工具需勾选「不校验合法域名」
3. 防火墙需放行 8080 端口

### 评分提交成功但首页没更新？
下拉刷新首页或重新进入，确保 `onShow` 生命周期正确触发。详见 [评分功能问题排查指南](./评分功能问题排查指南.md)。

### 换了网络环境连不上？
修改两处配置：`application-dev.yml` 的 `base-url` 和 `config.js` 的 `lan` 地址，改为新的局域网 IP。

## 📄 许可证

本项目为内部使用项目，未开源。

---

**🏫 西安文理学院 · 智慧食堂美食评估系统**
