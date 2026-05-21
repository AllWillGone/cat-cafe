# Android 开发日志3

## 一、文档定位

本文件是 Android 顾客端当前开发日志与接手备忘，已整合原 `docs/开发日志.md` 中 2026-05-20 分散记录。

权威接口、字段、数据库结构仍以 `backend/schemas.py`、`backend/routers/`、`backend/models.py` 和 `database/init.sql` 为准；本文件只记录 Android 接入状态、业务结论、验证结果和下次开发注意事项。

## 二、当前完成状态

Android 端已完成第一批可运行顾客端闭环骨架，并在 2026-05-20 扩展到详情、点赞、评论入口、购物车和个人资料相关流程。

### 已完成能力

- 包名：`com.catcafe.app`
- 底部三入口：`首页 / 服务 / 我的`
- 游客首页、商品列表、猫咪列表
- 登录、注册、退出登录、登录态保存与 token 校验
- 商品详情页、猫咪详情页
- 商品/猫咪评论入口；未登录发表评论时直接跳转登录页
- 商品/猫咪点赞状态展示、点赞、取消点赞
- 购物车、加购、数量增减、下单前登录拦截
- 我的页信息展示、个人资料页、用户资料刷新
- Retrofit / OkHttp / Glide / RecyclerView 基础结构

### 当前实现文件

- 入口：`app/src/main/java/com/catcafe/app/MainActivity.java`
- 登录页：`app/src/main/java/com/catcafe/app/ui/AuthActivity.java`
- 首页：`app/src/main/java/com/catcafe/app/ui/HomeFragment.java`
- 服务页：`app/src/main/java/com/catcafe/app/ui/ServiceFragment.java`
- 我的页：`app/src/main/java/com/catcafe/app/ui/MineFragment.java`
- 商品详情：`app/src/main/java/com/catcafe/app/ui/ProductDetailActivity.java`
- 猫咪详情：`app/src/main/java/com/catcafe/app/ui/CatDetailActivity.java`
- 点赞控制：`app/src/main/java/com/catcafe/app/ui/LikeController.java`
- 商品规则：`app/src/main/java/com/catcafe/app/ui/ProductRules.java`
- 网络层：`app/src/main/java/com/catcafe/app/network/`
- 会话保存：`app/src/main/java/com/catcafe/app/core/SessionManager.java`
- 布局：`app/src/main/res/layout/`

## 三、2026-05-20 整合开发记录

### 业务与首页/服务页调整

- 明确猫咖核心业务不是“购买猫咪”，而是购买“一小时撸猫券”；猫咪页只展示资料、状态和搜索，不作为购买对象。
- 首页“推荐商品”优先展示服务类“撸猫券”，再补充其他在售商品。
- 首页推荐等待服务类商品和全部商品两路数据返回后再渲染，避免短暂缺少置顶撸猫券。
- 服务页“猫咖”标签调整为：顶部展示“撸猫券”商品入口，下方展示“猫咪信息”搜索和列表。
- 服务类商品卡片文案调整为“一小时体验券”和“购买券”，避免用户误解为购买猫咪。
- `/api/cats` 后端增加可选参数 `includeAll`：默认只返回在岗猫咪，传 `true` 时返回全部猫咪，供服务页展示完整猫咪信息。
- `database/seed_demo_media.sql` 新增本地演示数据“一小时撸猫券”。

涉及文件：

- `backend/routers/cat.py`
- `database/seed_demo_media.sql`
- `app/src/main/java/com/catcafe/app/network/ApiService.java`
- `app/src/main/java/com/catcafe/app/ui/ProductRules.java`
- `app/src/main/java/com/catcafe/app/ui/HomeFragment.java`
- `app/src/main/java/com/catcafe/app/ui/ServiceFragment.java`
- `app/src/main/java/com/catcafe/app/ui/adapter/ProductAdapter.java`
- `app/src/main/java/com/catcafe/app/util/UiText.java`
- `app/src/main/res/layout/fragment_service.xml`

### 点赞交互

- 商品详情页、猫咪详情页、首页列表、服务页列表的点赞展示统一为“点赞数 + 小爱心图标”。
- 未点赞显示空心爱心；点赞成功后显示红色实心爱心并增加计数；再次点击取消点赞并减少计数。
- 未登录点击爱心时直接跳转登录页。
- 新增 `LikeController` 统一处理点赞状态查询、创建、取消、数字更新和图标切换。
- Android 端通过 `GET /api/likes?likeType=...` 查询当前用户是否已点赞并获取 `likeId`，再决定调用 `POST /api/likes` 或 `DELETE /api/likes/{likeId}`。
- 后端当前支持评论点赞 `likeType=1`，但 Android 暂无评论列表展示页，因此本次未放置评论点赞入口；后续评论列表可复用 `LikeController`。

涉及 Android 文件：

- `app/src/main/java/com/catcafe/app/ui/LikeController.java`
- `app/src/main/res/drawable/ic_heart_outline_24.xml`
- `app/src/main/res/drawable/ic_heart_filled_24.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/layout/activity_product_detail.xml`
- `app/src/main/res/layout/activity_cat_detail.xml`
- `app/src/main/res/layout/item_product.xml`
- `app/src/main/res/layout/item_cat.xml`
- `ProductDetailActivity.java`
- `CatDetailActivity.java`
- `ProductAdapter.java`
- `CatAdapter.java`
- `HomeFragment.java`
- `ServiceFragment.java`

### 评论、购买与购物车

- 未登录点击商品评论、猫咪评论时直接跳转登录页，不再等接口返回未授权错误。
- 未登录加购或提交订单时直接跳转登录页。
- 购物车行改为单行展示商品名、减号、数量、加号，不展示图片和多余说明。
- 购物车加减数量同步更新 `CartManager`；数量减到 0 时从真实购物车移除。
- 购物车下方收货人和手机号默认填充当前账号信息，并调用 `GET /api/user/me` 刷新一次。
- 购物车底部表单和按钮做了轻量样式整理。

影响接口：

- 继续使用现有 `GET /api/user/me`、`POST /api/orders`、`POST /api/comments`，无接口字段或路径变化。

### 我的页与个人资料

- 登录后的“我的”页直接展示头像、用户名、账号类型、用户 ID、手机号。
- 进入或返回“我的”页时调用 `GET /api/user/me` 刷新用户资料，并同步到本地会话缓存。
- 修改信息、我的订单、我的点赞、修改密码、退出登录等操作按钮集中放到底部。
- 未登录状态仍展示登录/注册入口。
- 个人资料页改为顶部资料卡 + 基础信息表单：展示头像、用户名、账号类型、注册时间，并保留用户名、手机号、头像路径编辑。
- 头像通过 `userAvatar` 加载，支持 `/avatars/...` 相对路径；测试用户 `user` 使用 `/avatars/luoridaqiao.jpg`。

影响接口：

- 使用现有 `GET /api/user/me`。
- 无接口路径、字段、状态码变化。
- 涉及字段仍为 `user.userAvatar`。

### 演示图片资源

- 演示商品图片整理为：只有 Cola、Cola Zero、可乐、无糖可乐使用 `/products/cola.jpg`。
- 其它商品 `imageUrl` 为空，由 Android 占位图显示。
- 更新 `database/seed_demo_media.sql`，并已同步当前本地 `cat_cafe` 开发库。

影响接口：

- 无接口路径、字段、状态码变化。
- 涉及字段为 `product.imageUrl`。

## 四、接口与环境

- 模拟器后端地址：`http://127.0.0.1:8000`，通过 `adb reverse` 转发到电脑后端。
- 当前转发命令：

```cmd
"D:\Users\yang\AppData\Local\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000
```

- 真机同 Wi-Fi：改成电脑局域网 IP，后端需用 `--host 0.0.0.0`。
- 需登录接口统一带 `Authorization: Bearer <token>`。
- 公开图片走 `IMAGE_BASE_URL + 相对路径`；后端已直接托管 `web/cat-cafe-ui/public` 下的 `/cats`、`/products`、`/avatars`。

## 五、开发边界

- 只做顾客端，不做管理员端。
- 不随意改数据库结构，不新增未被需求要求的后端接口。
- 不做真实支付、文件上传、离线缓存、Room。
- 金额使用 `BigDecimal` 或字符串，不用 `float/double`。
- 认证和权限以后端 token 为准，Android 不伪造 `userId` 或 `userType`。

## 六、已验证结论

### Android 构建

```cmd
.\gradlew.bat assembleDebug
.\gradlew.bat :app:assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

已记录结果：

- `assembleDebug` 构建成功。
- `:app:assembleDebug` 构建成功。
- `testDebugUnitTest` 曾通过。
- `lintDebug` 曾通过。

### 后端与网络

- `POST /api/login` 在后端可正常返回 200。
- 模拟器内访问 `http://10.0.2.2:8000` 一度超时。
- 模拟器内通过 `adb reverse` 访问 `http://127.0.0.1:8000/api/cats`、`/api/products` 可正常返回数据。
- `adb` 可连接模拟器，App 包名已是 `com.catcafe.app`。
- 后端核心 Python 文件 AST 语法检查通过。
- `python -m compileall backend` 在当前环境会尝试写入 `backend/.venv` 和 `__pycache__`，遇到权限拒绝，因此未作为最终验证命令。

## 七、已记录问题

- 旧运行配置残留过 `com.example.catcafe`，导致 Android Studio 试图用旧包名启动；需检查 Run Configuration 是否仍指向旧 Activity。
- 模拟器环境下 `10.0.2.2:8000` 曾无法连通，表现为服务页一直转圈、登录无响应、请求 10 秒超时。
- 已确认当时真实原因是网络连不上，不是数据库空，也不是后端接口错误。
- 当前 Debug 配置改用 `127.0.0.1` + Gradle 自动 `adb reverse`，`installDebug` 前只会转发 `tcp:8000`。
- 当前代码已把地址集中到 `app/build.gradle` 的 `BuildConfig`，后续切真机或换网络时只改这里。
- 已删除默认测试模板文件：
  - `app/src/test/java/com/example/catcafe/ExampleUnitTest.java`
  - `app/src/androidTest/java/com/example/catcafe/ExampleInstrumentedTest.java`

## 八、下阶段优先级

1. 评论列表展示和评论点赞。
2. 我的订单。
3. 我的点赞。
4. 修改个人资料完整流程。
5. 修改密码。
6. 下单后的订单状态展示与跳转。

## 九、下次开工前先读

1. `CLAUDE.md`
2. `ANDROID_FRONTEND_PLAN.md`
3. `backend/schemas.py`
4. `backend/routers/`
5. `database/init.sql`

## 十、2026-05-20 需求审查记录

本节根据当前目录 `需求.md`、Android 实际代码、后端 `schemas.py` / `routers/` / `models.py`、`database/init.sql` 以及 Web 管理端目录静态审查整理。这里只记录风险、问题和建议，不代表已经完成修改。

### 1. 总体判断

- `需求.md` 是完整系统需求，覆盖用户端和管理员端；当前 Android 实现仍应按既定边界定位为顾客端。
- 后端已经提供大部分用户端和管理员端接口；Web 目录已有管理员端页面，包括用户、商品、订单、评论、猫咪管理。
- 不建议短期内把管理员端塞进 Android。管理员功能优先继续由 Web 后台承担，Android 先补顾客端闭环。
- 业务口径需要统一：猫咪本体只用于信息展示、评论和点赞，不作为购买对象；可购买对象是猫咖服务券、餐饮和猫咪用品。

### 2. 当前已覆盖能力

- Android 已覆盖游客浏览猫咪/商品、商品/猫详情、登录、注册、退出登录、个人资料、修改资料、修改密码、商品/猫点赞与取消、购物车、提交订单、我的订单基础列表、我的点赞基础列表、发表评论入口。
- 后端已覆盖公开查询、登录注册、用户资料、订单、评论、点赞，以及 `/api/admin/...` 管理员接口。
- Web 管理端已有用户管理、商品管理、猫咪管理、订单管理、评论审核等页面。

### 3. 主要缺口

- Android 暂无评论点赞入口；后端支持 `likeType=1`。
- Android 我的订单只有基础列表，没有详情、状态筛选、商品名搜索、待处理订单修改、终态订单删除。
- Android 忘记密码接口已声明，但登录页没有入口和完整流程。
- Android 注销账号接口后端存在 `/api/user/me` DELETE，但 App 未接。
- “查看自己发布过的评论”没有用户端闭环；公开评论接口只展示审核通过评论，用户无法查看自己的待审核评论。
- “售后服务”在需求中只有一句描述，当前没有明确数据模型、接口或页面，后续不能凭空扩展，需要先定义业务范围。

### 4. 风险与逻辑问题

- Web 管理端订单页使用 `row.items[0].orderId` 更新/删除订单，表面是整批订单，实际可能只改第一条订单，和 `batchNo` 聚合展示不一致。
- 下单库存扣减是先查库存再扣减后提交，缺少行锁或乐观并发控制；高并发下存在超卖风险。
- Android 购物车使用加入时的商品库存快照，提交前没有刷新商品状态和库存；后端会兜底拒绝，但用户体验会变成提交后才报错。
- Android 当前允许 `userType=1` 管理员账号登录后进入顾客端状态，和“用户端/管理员端分离”口径不一致。
- `NetworkHelper` 收到 401 会清除 session，但当前页面多数只 Toast 错误，不一定刷新为游客态或引导重新登录。
- `需求.md` 写“猫及其用品的下单与付款功能”，需要避免误解为“购买猫咪本体”。

### 5. 分层优先级建议

P0：先修业务一致性，不扩大量新功能。

1. 明确购买口径：猫咪本体不可购买，只购买服务券、餐饮、猫咪用品。
3. Android 管理员登录边界：如果 `userType=1`，提示使用 Web 后台，不进入顾客端购物/订单流程。

P1：补顾客端核心闭环。


P2：补账号与互动闭环。

1. Android 增加忘记密码流程，使用 `send-sms-code` 和 `reset-password-by-phone` 演示接口。
2. Android 增加注销账号入口，调用后端 `/api/user/me` DELETE，注意用户有历史订单时可能受外键限制。
3. 我的点赞补类型筛选、关键字搜索、点击跳转到商品/猫详情；评论点赞跳转到对应目标详情。
4. 评论点赞/取消复用现有 `LikeController` 思路。

P3：管理端完善。

1. 管理员功能优先放 Web，不建议当前阶段新增 Android 管理端。
2. Web 管理端优先修订单批次状态更新/删除的一致性。
3. Web 管理端加强路由权限守卫，避免普通用户直接访问管理页面。
4. 管理员账号创建、用户类型调整、删除用户时的历史订单限制要按后端真实规则处理。

P4：售后服务。

1. 先定义售后服务范围：订单问题反馈、取消/退款申请、客服留言、还是仅订单备注。
2. 未定义前不要新增表、接口或复杂页面。
3. 若课程验收只要求“服务结束后的评价”，可先把“评论/评价 + 管理员审核”作为最小闭环，不急于扩展售后模块。
## 十一、2026-05-20 Android 订单批次操作修复

- 只修改 Android 顾客端，未修改后端和 Web。
- 我的订单列表改为展示批次内商品明细、联系人和可用操作。
- 待处理订单编辑联系人、手机号、备注时，Android 会遍历当前 `BatchOrderResponse.items` 中的所有 `orderId`，逐条调用 `PUT /api/orders/{id}`，避免只修改批次第一行。
- 已完成或已取消订单删除时，Android 会遍历当前批次所有 `orderId`，逐条调用 `DELETE /api/orders/{id}`，避免只删除批次第一行。
- 验证：`.\gradlew.bat assembleDebug` 通过。

## 十二、2026-05-20 Android P1 顾客端闭环修复

- 我的订单页新增状态筛选、商品名搜索和订单详情弹窗。
- 保留并完善待处理订单编辑联系人/手机号/备注、终态订单整批删除能力。
- 购物车下单成功后清空购物车并跳转订单列表。
- 商品详情页、猫咪详情页新增审核通过评论列表；发表评论后仍提示等待审核，不本地插入公开评论列表。
- 验证：`.\gradlew.bat assembleDebug` 通过。

## 十三、2026-05-20 Android 个人资料编辑补齐
- 对照 `database/init.sql`、`backend/models.py`、`backend/schemas.py` 和 `backend/routers/user.py` 确认用户资料字段为 `userName`、`gender`、`birthday`、`userPhone`、`userAvatar`。
- Android 个人资料页补齐 `gender` 与 `birthday` 编辑：性别按后端约定提交 `1=男`、`2=女`，生日通过日期选择器提交 `yyyy-MM-dd`，继续复用现有 `PUT /api/user/me`。
- 本次未修改数据库结构、后端路由或接口响应结构。
- 验证：`.\gradlew.bat assembleDebug` 通过；`.\gradlew.bat lintDebug` 通过，保留既有硬编码文案警告。

## 十四、2026-05-20 我的评论与点赞跳转
- 后端新增 `GET /api/comments/my`：使用当前 token 查询自己的评论，包含待审核、已通过、已拒绝，返回结构复用 `PaginatedComments`。
- Android 新增“我的评论”入口和页面：点击评论跳转到对应商品或猫咪详情，删除调用现有 `DELETE /api/comments/{id}`。
- Android “我的点赞”列表支持点击跳转：商品点赞跳商品详情，猫咪点赞跳猫咪详情，评论点赞通过 `targetType + targetId` 跳到原商品或猫咪详情。
- 评论增删逻辑复核：发表评论已校验目标存在并用 token 写 `userId`；删除评论允许作者本人或管理员；公开评论列表仍只展示审核通过内容。
- 验证：`.\gradlew.bat assembleDebug` 通过；`.\gradlew.bat lintDebug` 通过；`backend/routers/comment.py` AST 检查通过。`compileall` 因 `__pycache__` 写权限失败未完成。
