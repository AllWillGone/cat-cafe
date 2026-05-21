# Android 管理员端开发规划

本文档是下一轮 Android 管理员能力开发的执行边界、页面逻辑、接口核对和验收标准。开始写代码前仍必须按仓库真实文件复核：`database/init.sql`、`backend/models.py`、`backend/schemas.py`、`backend/routers/`、`android/CLAUDE.md`。

## 1. 当前结论

- 不新建独立管理员 App，不改包名，继续使用现有 `com.catcafe.app`。
- App 启动默认进入游客模式，底部导航仍为：首页、服务、我的。
- 右上角增加管理员入口，点击后进入单独的管理员登录界面。
- 管理员登录成功后仍使用同一套底部导航：`首页 / 服务 / 我的`。
- 管理员模式下：
  - 首页：复用顾客端首页展示，不做管理功能。
  - 服务：替换为管理员工作台，不再展示顾客端商品/猫咪/购物车/订单 Tab。
  - 我的：只保留个人信息、修改密码、退出登录。
- 管理员不做点赞模块，也不需要“我的点赞”“我的评论”“我的订单”等顾客个人功能。
- 不改数据库结构，不新增后端接口；第一版使用现有管理员接口完成闭环。

## 2. 已完成能力不再作为待办

以下能力已属于当前顾客端范围，本轮不要重复规划、重复实现或重构：

- 游客浏览首页、商品、猫咪、详情、公开评论。
- 顾客注册、登录、退出登录、登录态保存、个人资料、修改密码、忘记密码。
- 商品/猫咪/评论点赞、取消点赞、我的点赞。
- 商品购物车、多商品下单、扫码支付演示。
- 我的订单列表、筛选、详情、待处理订单联系方式修改、终态订单删除。
- 商品/猫咪评论区、发表评论、我的评论、评论删除。
- 图片相对路径拼接、Glide 加载、占位图。
- Debug 环境 `127.0.0.1:8000` + `adb reverse tcp:8000 tcp:8000` 调试方案。

这些能力只作为可复用代码来源，不再作为管理员端需求目标。

## 3. 总体交互边界

### 3.1 游客与顾客

- App 启动默认游客模式，不强制登录。
- 游客点击需要登录的顾客功能时，进入普通登录界面。
- 普通用户登录后保持现有顾客端体验。
- 普通用户不能进入管理员工作台；访问管理员接口仍由后端 `get_current_admin` 拦截。

### 3.2 管理员入口

- 首页右上角放管理员入口。
- 管理员入口对游客和已登录普通用户都可见。
- 点击管理员入口后进入管理员登录界面，要求输入管理员账号密码。
- 管理员登录成功后保存 token 和 `userType=1`，进入主界面。
- 如果管理员登录界面中登录到 `userType=0`，提示“当前账号不是管理员”，不进入管理员模式。

### 3.3 管理员模式

- 管理员模式下仍显示底部导航：首页、服务、我的。
- 首页复用现有首页，但管理员不参与顾客侧购物、点赞、发表评论等行为。
- 如果管理员在首页或详情页触发顾客行为，前端应隐藏入口或提示“管理员账号不参与顾客操作”。
- 服务页改为管理员工作台，以五个方块入口跳转：
  - 评论管理
  - 订单管理
  - 商品管理
  - 猫咪管理
  - 用户管理
- 管理员不做点赞管理。当前后端也没有 `GET /api/admin/likes`，不要为此新增接口。
- 我的页只保留：
  - 个人信息
  - 修改密码
  - 退出登录

## 4. 开发边界

- 不改 `database/init.sql` 表结构，不新增表、不新增字段。
- 不新增后端接口，先使用现有 `/api/admin/...` 和已有订单/评论接口完成闭环。
- 管理员权限必须依赖后端 token 和 `get_current_admin`，Android 只做入口分流和 UI 控制。
- Android 不信任本地 `userType` 执行业务授权；所有真实权限以后端响应为准。
- 不允许客户端传入或伪造 `userId`、`userType` 来越权。
- 商品金额继续用 `BigDecimal` 或字符串展示，不使用 `float/double` 做金额计算。
- 图片第一版只支持填写 URL 或相对路径，不做相册选择和上传。
- 商品删除要尊重后端规则：有历史订单时后端会改为下架，不要在 UI 文案中承诺一定硬删除。
- 订单必须按 `batchNo` 聚合展示和操作；批次操作时 Android 要遍历 `BatchOrderResponse.items[*].orderId` 逐条调用接口。

## 5. 管理员端 MVP 需求

### 5.1 管理员登录

- 复用 `POST /api/login`。
- 登录成功后根据响应中的 `userType` 判断是否进入管理员模式。
- 管理员登录界面不放快速登录按钮。
- 登录失败、非管理员账号、网络失败都用现有错误处理方式提示。

### 5.2 管理员服务页工作台

- 服务页不再使用顾客端商品/猫咪/购物车/订单 Tab。
- 使用五个管理方块入口：评论、订单、商品、猫咪、用户。
- 每个方块进入独立页面或 Activity。
- 移动端不要照搬 Web 表格；列表用卡片，筛选放顶部紧凑控件，操作放卡片按钮或详情页。

### 5.3 用户管理

第一版做：

- 用户列表：分页、关键字搜索用户名/手机号、按 `userType` 筛选。
- 编辑用户：用户名、手机号、头像 URL、性别、生日。
- 删除用户：不能删除自己；有关联订单的用户后端会返回 400。

第一版不做：

- 新增用户。
- 修改用户类型 `userType`。
- 重置任意用户密码。

原因：现有 `AdminUserUpdate` 不包含 `userType` 和密码字段，后端没有管理员新增用户/重置密码接口。

### 5.4 商品管理

第一版做：

- 商品列表：分页、关键字、分类、状态筛选；管理员能看到在售和下架商品。
- 新增商品：名称、分类、价格、库存、图片 URL、描述、状态。
- 编辑商品：同上，全部字段可选。
- 上架/下架：上架可调用 `PUT /api/admin/products/{id}` 传 `status=1`；下架优先调用 `PUT /api/admin/products/{id}/off`。
- 删除商品：调用 `DELETE /api/admin/products/{id}`；如果商品存在历史订单，后端会返回“商品存在历史订单，已改为下架”。

第一版不做：

- 图片上传。
- 批量上下架。
- 库存流水或销售统计。

### 5.5 猫咪管理

第一版做：

- 猫咪列表：分页、关键字、状态筛选；管理员能看到休息中和在岗中。
- 新增猫咪：名字、品种、生日、状态、性格、照片 URL、备注。
- 编辑猫咪：同上，全部字段可选。
- 切换状态：调用 `PUT /api/admin/cats/{id}` 传 `status=0/1`。
- 删除猫咪：调用 `DELETE /api/admin/cats/{id}`。

注意：猫咪评论是软引用，删除猫咪不会自动删除历史评论。UI 删除前要给明确确认。

### 5.6 评论管理

第一版做：

- 评论列表：分页、按审核状态筛选、关键字搜索用户名/评论 ID/评论内容。
- 审核通过：`auditStatus=1`。
- 审核拒绝：`auditStatus=2`。
- 删除评论：复用 `DELETE /api/comments/{id}`，管理员可删除任意评论。

第一版不做：

- 评论内容编辑。
- 评论目标名称补全；现有响应只有 `targetType + targetId`，可显示“商品 #id / 猫咪 #id”。

### 5.7 订单管理

第一版做：

- 订单列表：分页、按订单状态筛选、关键字搜索批次号/联系人用户名。
- 订单卡片按 `batchNo` 展示整批商品明细、联系人、手机号、备注、总金额、状态。
- 修改订单状态：未支付、已支付、待拿取、已完成、已取消。
- 编辑订单联系人、手机号、备注。
- 删除订单：管理员可删任意状态订单；删除非终态订单时后端会恢复库存。

批次操作硬性要求：

- 列表和详情返回的是 `BatchOrderResponse`，但修改/删除接口路径是 `/api/orders/{order_id}`。
- 对一个批次执行状态更新、联系人修改或删除时，必须遍历该批次所有 `items[*].orderId` 逐条调用。
- 不能只拿 `items[0].orderId` 操作，否则同批次其他商品行状态会不一致。
- 状态改为 `4=已取消` 或删除非终态订单会触发库存恢复；前端不要额外修改库存。

第一版不做：

- 真实支付。
- 批量导出。
- 按商品名搜索管理员订单。当前 `GET /api/admin/orders` 的 `keyword` 只匹配 `batchNo` 和 `userName`，不是商品名。

## 6. 最小化改动方案

### 6.1 复用现有代码

建议复用：

- `ApiClient`：自动附加 Bearer token。
- `SessionManager`：保存/清理 token、userId、userName、userType、头像。
- `NetworkHelper` / `ApiCallback` / `ApiError`：统一错误处理。
- 现有模型：`UserDetail`、`ProductDetail`、`CatDetail`、`CommentDetail`、`BatchOrderResponse`、分页模型等。
- 现有列表经验：`RecyclerView` + Adapter + `SwipeRefreshLayout` + `skip/limit`。
- 现有图片处理：`AppConfig.IMAGE_BASE_URL` + Glide。
- 现有表单经验：个人资料、订单编辑、评论弹窗中的输入校验和提交模式。

需要新增或调整：

- 首页右上角管理员入口。
- 管理员登录 Activity。
- 管理员模式识别和服务页内容分流。
- 管理员工作台五宫格入口。
- 管理员各模块 Activity/Adapter：用户、订单、商品、猫咪、评论。
- 管理员请求模型：`ProductCreate/Update`、`CatCreate/Update`、`AdminUserUpdate`、`AdminOrderUpdate`、`CommentAudit`。
- `ApiService` 中补齐 `/api/admin/...` 方法。

### 6.2 推荐页面结构

- `AuthActivity`：继续承担普通登录/注册/忘记密码。
- `AdminAuthActivity`：管理员登录，只允许 `userType=1` 进入管理员模式。
- `MainActivity`：继续承载底部导航，根据当前 session 的 `userType` 控制服务页和我的页内容。
- `HomeFragment`：复用现有首页，管理员模式隐藏或禁用顾客动作。
- `ServiceFragment`：
  - 普通/游客模式：保留现有顾客服务页。
  - 管理员模式：显示五个管理方块。
- `MineFragment`：
  - 普通/游客模式：保留现有顾客我的页。
  - 管理员模式：只显示个人信息、修改密码、退出登录。
- `AdminUsersActivity`
- `AdminProductsActivity`
- `AdminCatsActivity`
- `AdminCommentsActivity`
- `AdminOrdersActivity`

## 7. 开发顺序

1. 管理员入口、管理员登录、登录后 `userType` 分流。
2. 服务页和我的页按管理员模式切换内容。
3. 补齐 `ApiService` 管理员接口和请求模型。
4. 商品管理和猫咪管理：字段直接、闭环最清晰。
5. 评论管理：列表 + 审核 + 删除。
6. 订单管理：重点处理 `batchNo` 批次逐行更新/删除。
7. 用户管理：最后做，删除用户有订单约束，错误提示要准确。

## 8. 管理员接口核对

以下结论来自当前实际代码：`backend/routers/`、`backend/schemas.py`、`backend/models.py`、`database/init.sql`。

### 已完备，可直接接入

- 登录：`POST /api/login`
- 当前账号：`GET /api/user/me`
- 修改自己信息：`PUT /api/user/me`
- 修改自己密码：`PUT /api/user/me/password`
- 用户列表：`GET /api/admin/users`
- 用户编辑：`PUT /api/admin/users/{id}`
- 用户删除：`DELETE /api/admin/users/{id}`
- 商品列表：`GET /api/admin/products`
- 商品新增：`POST /api/admin/products`
- 商品编辑：`PUT /api/admin/products/{id}`
- 商品删除：`DELETE /api/admin/products/{id}`
- 商品下架：`PUT /api/admin/products/{id}/off`
- 猫咪列表：`GET /api/admin/cats`
- 猫咪新增：`POST /api/admin/cats`
- 猫咪编辑：`PUT /api/admin/cats/{id}`
- 猫咪删除：`DELETE /api/admin/cats/{id}`
- 评论列表：`GET /api/admin/comments`
- 评论审核：`PUT /api/admin/comments/{id}/audit`
- 评论删除：`DELETE /api/comments/{id}`
- 订单列表：`GET /api/admin/orders`
- 订单详情：`GET /api/orders/{id}`
- 订单修改：`PUT /api/orders/{id}`
- 订单删除：`DELETE /api/orders/{id}`

### 不完备或受限，第一版规避

- 管理员没有点赞模块；后端没有 `GET /api/admin/likes`。
- 管理员不能新增用户、不能修改用户 `userType`、不能重置任意用户密码。
- 管理员订单搜索不支持商品名，只支持批次号和联系人用户名。
- 订单修改/删除没有批次级接口，只能对每个 `orderId` 逐条调用。
- 评论列表不直接返回商品名/猫咪名，只返回 `targetType + targetId`。
- 没有文件上传接口，商品/猫咪/头像图片只能填 URL。
- 没有统计报表接口，不做营业额、热销、库存流水看板。

## 9. 管理员端接口摘要

### 用户

- `GET /api/admin/users?keyword=&userType=&sortBy=userId&sortOrder=desc&skip=0&limit=20`
- `PUT /api/admin/users/{user_id}`
- `DELETE /api/admin/users/{user_id}`

`AdminUserUpdate` 字段：`userName`、`gender`、`birthday`、`userPhone`、`userAvatar`。

### 商品

- `GET /api/admin/products?category=&status=&keyword=&skip=0&limit=20`
- `POST /api/admin/products`
- `PUT /api/admin/products/{product_id}`
- `DELETE /api/admin/products/{product_id}`
- `PUT /api/admin/products/{product_id}/off`

商品字段：`productName`、`category`、`price`、`stockQuantity`、`imageUrl`、`description`、`status`。

### 猫咪

- `GET /api/admin/cats?status=&keyword=&skip=0&limit=20`
- `POST /api/admin/cats`
- `PUT /api/admin/cats/{cat_id}`
- `DELETE /api/admin/cats/{cat_id}`

猫咪字段：`catName`、`breed`、`birthday`、`status`、`personality`、`photoUrl`、`notes`。

### 评论

- `GET /api/admin/comments?auditStatus=&keyword=&skip=0&limit=20`
- `PUT /api/admin/comments/{comment_id}/audit`
- `DELETE /api/comments/{comment_id}`

审核字段：`auditStatus=1` 通过，`auditStatus=2` 拒绝。

### 订单

- `GET /api/admin/orders?orderStatus=&keyword=&skip=0&limit=20`
- `GET /api/orders/{order_id}`
- `PUT /api/orders/{order_id}`
- `DELETE /api/orders/{order_id}`

管理员订单更新字段：`orderStatus`、`userPhone`、`userName`、`orderNote`。

订单状态：`0=未支付`、`1=已支付`、`2=待拿取`、`3=已完成`、`4=已取消`。

## 10. 验收标准

实现完成后至少验证：

- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat lintDebug`
- App 默认游客模式可进入。
- 右上角管理员入口可进入管理员登录页。
- 管理员账号登录后，底部导航仍为首页、服务、我的。
- 管理员模式下首页复用展示，顾客下单/点赞/评论等操作被隐藏或阻止。
- 管理员模式下服务页显示评论、订单、商品、猫咪、用户五个管理方块。
- 管理员模式下我的页只显示个人信息、修改密码、退出登录。
- 普通用户不能进入管理员工作台。
- 每个管理员模块的列表、筛选、分页可用。
- 商品可新增、编辑、上架、下架、删除或下架保留。
- 猫咪可新增、编辑、切换状态、删除。
- 评论可通过、拒绝、删除。
- 订单批次展示正确；批次状态更新、联系人编辑、删除会遍历批次内所有 `orderId`。
- 用户可编辑、可删除；删除自己和删除有关联订单用户时提示后端返回原因。

## 11. 暂不做的后续方向

- 管理员点赞模块。
- 管理员新增账号、修改用户类型、重置用户密码。
- 批次级订单后端接口。
- 管理员订单按商品名搜索。
- 评论目标名称后端补全。
- 图片上传。
- 数据统计报表。
