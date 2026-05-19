# Android 开发说明

## 当前阶段状态

后端 FastAPI、MySQL 数据库和 Web 前端已完成整合，Web 目录为 `web/cat-cafe-ui`。
Android 端尚未创建，写 Android 前以 `database/init.sql`、`backend/models.py`、`backend/schemas.py`、`backend/routers/` 为准。

## 本地启动依赖

1. 管理员 CMD 启动 MySQL：

```cmd
net start MySQL80
```

2. 启动后端：

```cmd
cd G:\cat-cafe\backend
.venv\Scripts\activate
uvicorn main:app --reload
```

3. 后端文档：

```text
http://127.0.0.1:8000/docs
```

## Android 访问地址

Android 模拟器不能直接用 `127.0.0.1` 访问电脑后端：

| 运行位置 | Base URL |
|------|------|
| Web/电脑浏览器 | `http://127.0.0.1:8000` |
| Android Emulator | `http://10.0.2.2:8000` |
| 真机同 Wi-Fi | `http://电脑局域网IP:8000` |

真机调试时后端需要改为监听局域网：

```cmd
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Android 项目需要允许明文 HTTP。后续创建 Android 工程时，在 `AndroidManifest.xml` 加：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

并根据项目 SDK 配置 `android:usesCleartextTraffic="true"` 或网络安全配置。

## 测试账号

| 角色 | 用户名 | 密码 | 手机号 |
|------|------|------|------|
| 管理员 | `admin` | `123456` | 可为空 |
| 普通用户 | `user` | `123456` | `13800000000` |

登录成功后保存返回的 `token`，后续需登录接口统一加请求头：

```text
Authorization: Bearer <token>
```

## Android 优先实现页面

建议先做顾客端闭环，再做管理员端：

1. 登录/注册：`POST /api/login`、`POST /api/register`
2. 首页/猫咪：`GET /api/cats`、`GET /api/cats/{id}`
3. 商品：`GET /api/products`、`GET /api/products/{id}`
4. 下单：`POST /api/orders`
5. 我的订单：`GET /api/orders`
6. 点赞/我的点赞：`POST /api/likes`、`GET /api/likes`、`DELETE /api/likes/{id}`
7. 评论：`POST /api/comments`、`GET /api/comments`
8. 个人资料：`GET /api/user/me`、`PUT /api/user/me`

## 接口注意事项

- 金额字段是 `Decimal/Numeric(10,2)`，Android 端用字符串或 BigDecimal 处理，不要用 float 做金额计算。
- 订单使用 `batchNo` 聚合同一批商品，列表和详情返回 `items` 数组。
- 商品下架是 `status=0`，不要依赖硬删除。
- 评论对象使用 `targetType + targetId`：`0=商品`，`1=猫咪`。
- 点赞对象使用 `likeType + objectId`：`0=商品`，`1=评论`，`2=猫咪`。
- 公开图片路径来自 Web 静态资源，如 `/cats/mimi.jpg`。Android 如需展示图片，先拼接后端或 Web 静态资源地址；当前后端没有托管这些图片。
- `/api/send-sms-code` 是演示模式，会直接返回验证码，正式环境不能这样做。

## 文档位置

- 后端/API 总说明：`docs/开发日志.md`
- Web 整合和扩展记录：`docs/开发日志2.md`
- 权威数据库结构：`database/init.sql`
