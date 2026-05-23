# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 猫咖点单系统

四人课设，Android 顾客端/轻量管理端 + Vue Web 管理端 + Python FastAPI + MySQL 8.0。

### 常用命令

**后端：**

```bash
cd backend
.venv\Scripts\activate          # Windows 激活虚拟环境
uvicorn main:app --reload       # 启动，文档页 http://127.0.0.1:8000/docs
```

**Android：**

```bash
cd android
.\gradlew.bat assembleDebug     # 构建
.\gradlew.bat lintDebug         # Lint 检查
.\gradlew.bat testDebugUnitTest # 单元测试
```

模拟器需先执行 `adb reverse tcp:8000 tcp:8000` 转发后端端口。API 地址集中在 `app/build.gradle` 的 `BuildConfig`。

**Android 构建类型：**

| 构建 | 命令 | API 地址 | 用途 |
|------|------|---------|------|
| Debug | `assembleDebug` | 可切换 | 默认用服务器 IP，模拟器开发改回 `127.0.0.1:8000` |
| Release | `assembleRelease` | `47.86.228.41` | 真机 / 发版 |

在 `app/build.gradle` 的 `buildTypes` 中切换 debug 的 `buildConfigField`。

**Web 管理端：**

```bash
cd web\cat-cafe-ui
npm install                     # 首次
npm run dev                     # 启动开发服务器
```

**数据库：**

```bash
mysql -u root -pyang < database/init.sql                          # 初始化
mysql -u root -pyang cat_cafe < database/seed_demo_media.sql      # 演示数据
```

**Dev Container（推荐）：**

在 VS Code 中安装 Dev Containers 扩展，`Ctrl+Shift+P` → "Dev Containers: Reopen in Container"。容器内自动配置好 Python、Node.js 和 MySQL 客户端，无需手动安装依赖。

```bash
# 容器内启动后端（DB_HOST 自动设为 mysql）
cd backend && uvicorn main:app --reload --host 0.0.0.0

# 容器内启动 Web 前端
cd web/cat-cafe-ui && npm run dev
```

MySQL 数据库和数据表在容器首次启动时自动创建（init.sql 和 seed_demo_media.sql 自动导入）。详见 `.devcontainer/` 和 `docker-compose.yml`。

容器端口映射：`8000`（后端）、`5173`（前端 dev server）、`3306`（MySQL）。

### 生产服务器

Docker 配置文件已在当前分支，包括 `docker-compose.prod.yml`（生产）、`docker-compose.yml`（本地开发）、`backend/Dockerfile.prod`、`nginx/`。

阿里云 ECS Ubuntu 24.04，2核2G，IP: `47.86.228.41`，域名: `pixelcat.tech`。

**SSH 登录：**

| 账号 | 密码 | 用途 |
|------|------|------|
| `root` | `123456Cat` | 管理员（仅作者使用） |
| `teammate1` | `catcafe2026` | 队员 B |
| `teammate2` | `catcafe2026` | 队员 D |
| `teammate3` | `catcafe2026` | 队员 C |

所有账号已加入 `docker` 组，可直接操作容器。

```bash
ssh root@47.86.228.41    # 作者用
ssh teammate1@47.86.228.41  # 队员用
```

**项目路径：** `/opt/cat-cafe`

**服务器常用操作：**

```bash
cd /opt/cat-cafe
docker compose -f docker-compose.prod.yml ps              # 查看服务状态
docker compose -f docker-compose.prod.yml logs -f --tail=50  # 实时日志
docker compose -f docker-compose.prod.yml restart backend    # 重启后端
docker compose -f docker-compose.prod.yml up -d --build      # 重新构建并启动
docker compose -f docker-compose.prod.yml down               # 停止所有服务
```

**测试账号（Web 登录）：**

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `admin123` | 管理员 |
| `test` | `123456` | 顾客 |

**Adminer（数据库管理，老师用）：**

| URL | 用户名 | 密码 | 系统 | 服务器 | 数据库 | 权限 |
|-----|--------|------|------|--------|--------|------|
| `http://47.86.228.41/adminer` | `teacher` | `catcafe2026` | MySQL | `mysql` | `cat_cafe` | SELECT 只读 |

**⚠️ 注意：**
- 2核2G 内存有限，切勿同时构建所有镜像。必须分步：先构建后端，再构建前端（`docker run --rm -v ... node:22-alpine sh -c "npm run build"`）
- 加了 2GB swap 防止 OOM，但 npm build 仍建议单独在临时容器中运行
- 数据在 Docker volume `cat-cafe_mysql_data` 中，`docker compose down -v` 会清空

### 项目结构

```
android/         Android Studio 顾客端/轻量管理端（作者负责）
backend/         Python FastAPI，详见 backend/CLAUDE.md
web/cat-cafe-ui/ Vue 3 + Element Plus + Vite 管理端（组员 C）
database/        init.sql（建表）+ seed_demo_media.sql（演示数据）
docs/            开发日志、需求分析
```

**Web 前端路由：**

| 路由 | 页面 | 说明 |
|------|------|------|
| `/login` | login.vue | 登录 |
| `/signup` | signupOrdinaryUser.vue | 注册 |
| `/home/index` | UserIndex.vue | 顾客首页 |
| `/home/cats` | UserCats.vue | 猫咪列表 |
| `/home/cats/:id` | CatDetail.vue | 猫咪详情 |
| `/home/products` | UserProducts.vue | 商品列表 |
| `/home/products/:id` | ProductDetail.vue | 商品详情 |
| `/home/orders` | UserOrders.vue | 我的订单 |
| `/home/likes` | UserLikes.vue | 我的点赞 |
| `/home/profile` | UserProfile.vue | 个人信息 |
| `/admin/users` | adminUsers.vue | 用户管理 |
| `/admin/comments` | adminComments.vue | 评论审核 |
| `/admin/orders` | adminOrders.vue | 订单管理 |
| `/admin/cats` | adminCats.vue | 猫咪管理 |
| `/admin/products` | adminProducts.vue | 商品管理 |

**Android 代码分层：**

| 包 | 说明 |
|---|---|
| `core/` | AppConfig（API 地址）、SessionManager（token 存储）、CartManager（购物车） |
| `model/` | 请求/响应数据类，与后端 schemas 对应 |
| `network/` | Retrofit ApiService 接口定义 + ApiClient 单例 |
| `ui/` | Activity + Fragment + Adapter，按页面拆分 |
| `util/` | UiText 文本工具 |

### 技术栈

- 后端：Python 3.13 + FastAPI 0.115 + SQLAlchemy 2.0 + PyMySQL + passlib(bcrypt) + python-jose(JWT)
- Android：Java + Retrofit + OkHttp + Glide + RecyclerView
- Web：Vue 3 + Element Plus + Vite + Axios + Vue Router
- 数据库：MySQL 8.0.45，root 密码 yang，库名 cat_cafe

### 关键设计决策

- 6 张表：user / catinformation / product / orders / comment / likes
- orders 通过 batchNo 支持多商品下单（同一批多条记录共享 batchNo）
- comment 通过 targetType(0=商品,1=猫咪) + targetId 统一评商品和猫咪
- likes 通过 likeType(0=商品,1=评论,2=猫咪) + objectId 软引用多表
- 密码 bcrypt 哈希存储，passlib 库
- 建表用 init.sql 手动执行，不用 SQLAlchemy create_all
- 数据库字段 camelCase，接口路径 kebab-case
- 后端直接托管 `web/cat-cafe-ui/public` 下的 `/cats`、`/products`、`/avatars` 静态资源
- 管理员可通过 `POST /api/upload` 上传图片（Web 端有文件选择器，无需手动输路径）
- Docker 生产环境：backend 和 nginx 通过 `./uploads/` 共享目录传递上传文件

### 接口总览（41 个 /api 接口，全部完成）

| 模块 | 数量 | 文件 |
| --- | --- | --- |
| 用户 | 12 | routers/user.py |
| 猫咪 | 6 | routers/cat.py |
| 商品 | 7 | routers/product.py |
| 评论 | 6 | routers/comment.py |
| 点赞 | 3 | routers/likes.py |
| 订单 | 6 | routers/order.py |
| 上传 | 1 | routers/upload.py |

完整接口清单和测试状态见 backend/CLAUDE.md。

### 开发边界

- Android 已包含轻量管理端；复杂后台仍优先由 Web 管理端承担。
- 猫咪本体只用于信息展示/评论/点赞，不作为购买对象；可购买的是服务券、餐饮、猫咪用品
- 不做真实支付、离线缓存
- 金额用字符串或 BigDecimal，不用 float/double
- 认证以服务端 token 为准，客户端不伪造 userId 或 userType
- 不随意改数据库结构，不新增未被需求要求的后端接口

### 组员分工

- 作者：订单模块后端（6 接口）+ Android 全部
- B：用户模块剩余 + 猫咪模块
- D：商品模块 + 评论模块 + 点赞模块
- C：Web 前端
