# 后端开发说明

## 技术
Python 3.13 + FastAPI 0.115 + SQLAlchemy 2.0 + PyMySQL + passlib(bcrypt) + python-jose(JWT)

## 启动
```
cd backend
.venv\Scripts\activate
uvicorn main:app --reload
```
文档页 http://127.0.0.1:8000/docs

## 文件
- main.py — 入口，注册路由
- models.py — 6 张表 ORM
- schemas.py — 请求/响应 Pydantic 模型（按模块分段）
- database.py — 引擎 + Session + Base
- config.py — 读 .env 拼连接串 + JWT 配置
- auth.py — JWT token 签发 + get_current_user + get_current_admin
- routers/ — 按模块拆分的路由文件
  - user.py — 用户模块（12 接口）
  - cat.py — 猫咪模块（6 接口）
  - product.py — 商品模块（7 接口）
  - comment.py — 评论模块（5 接口）
  - likes.py — 点赞模块（3 接口）
  - order.py — 订单模块（6 接口）
- .env — 数据库密码 + JWT 密钥（Git 不上传）
- .env.example — 本地 .env 模板，不含真实密码
- .venv/ — 虚拟环境（Git 不上传）

## 当前接口总览（39 个 /api 接口，不含 /health）

Web 整合后接口已扩展；最新数量以代码、`/docs` 和 `docs/开发日志2.md` 附件为准。

### 用户模块（12/12 ✅）
| 方法 | 路径 | 状态 |
|------|------|------|
| POST | /api/register | ✅ |
| POST | /api/login | ✅ |
| POST | /api/send-sms-code | ✅ |
| POST | /api/reset-password | ✅ |
| POST | /api/reset-password-by-phone | ✅ |
| GET | /api/user/me | ✅ |
| PUT | /api/user/me | ✅ |
| PUT | /api/user/me/password | ✅ |
| DELETE | /api/user/me | ✅ |
| GET | /api/admin/users | ✅ |
| PUT | /api/admin/users/{id} | ✅ |
| DELETE | /api/admin/users/{id} | ✅ |

### 猫咪模块（6/6 ✅）
| 方法 | 路径 | 状态 |
|------|------|------|
| GET | /api/cats | ✅ |
| GET | /api/cats/{id} | ✅ |
| GET | /api/admin/cats | ✅ |
| POST | /api/admin/cats | ✅ |
| PUT | /api/admin/cats/{id} | ✅ |
| DELETE | /api/admin/cats/{id} | ✅ |

### 商品模块（7/7 ✅）
| 方法 | 路径 | 状态 |
|------|------|------|
| GET | /api/products | ✅ |
| GET | /api/products/{id} | ✅ |
| GET | /api/admin/products | ✅ |
| POST | /api/admin/products | ✅ |
| PUT | /api/admin/products/{id} | ✅ |
| DELETE | /api/admin/products/{id} | ✅ |
| PUT | /api/admin/products/{id}/off | ✅ |

### 评论模块（5/5 ✅）
| 方法 | 路径 | 状态 |
|------|------|------|
| POST | /api/comments | ✅ |
| GET | /api/comments | ✅ |
| GET | /api/admin/comments | ✅ |
| PUT | /api/admin/comments/{id}/audit | ✅ |
| DELETE | /api/comments/{id} | ✅ |

### 点赞模块（3/3 ✅）
| 方法 | 路径 | 状态 |
|------|------|------|
| POST | /api/likes | ✅ |
| GET | /api/likes | ✅ |
| DELETE | /api/likes/{id} | ✅ |

### 订单模块（6/6 ✅）
| 方法 | 路径 | 状态 |
|------|------|------|
| POST | /api/orders | ✅ |
| GET | /api/orders | ✅ |
| GET | /api/admin/orders | ✅ |
| GET | /api/orders/{id} | ✅ |
| PUT | /api/orders/{id} | ✅ |
| DELETE | /api/orders/{id} | ✅ |

## 接口编写规范
每个接口：在 schemas.py 定义请求/响应 → 在 routers/ 写路由（db: Session = Depends(get_db)）→ /docs 测试
详细 API 清单见 docs/开发日志2.md 附件；docs/开发日志.md 下方旧清单只保留第一阶段历史记录

## 命名
数据库字段 camelCase，接口路径 kebab-case

---

## 用户模块 — 测试状态

### 已测通过 ✅

| 接口 | 测试场景 | 结果 |
|------|----------|------|
| POST /api/register | 正常注册（userName + password ≥ 6 位） | 返回 userId、userName、userType=0、registerTime、JWT token |
| POST /api/login | 正确用户名 + 正确密码 | 返回 token |
| POST /api/login | 管理员账号登录（userType=1） | 返回管理员 token |
| GET /api/user/me | 带有效 token | 返回完整信息（含 gender、birthday） |
| GET /api/user/me | 不带 token | 401 |
| PUT /api/user/me | 修改 gender=1、birthday | 返回更新后信息 |
| PUT /api/user/me/password | 旧密码正确 + 新密码符合长度 | 返回 message: "密码修改成功" |
| POST /api/reset-password | 存在的用户名 + 新密码 | 返回 message: "密码重置成功" |
| GET /api/admin/users | 管理员访问 | 返回 total + items（按 userId 降序） |
| GET /api/admin/users | 普通用户访问 | 403 |
| DELETE /api/admin/users/{id} | 管理员删除不存在的用户 | 404 |
| DELETE /api/admin/users/{id} | 管理员删除自己 | 400 "不能删除自己的账号" |
| POST /api/register | 用户名已存在 | 422 |
| POST /api/register | 用户名为空 | 422 |
| POST /api/register | 密码不足 6 位 | 422 |
| POST /api/login | 用户名不存在 | 401 |
| POST /api/login | 密码错误 | 401 |
| PUT /api/user/me | 不传任何字段（全部可选） | 200，信息不变 |
| POST /api/reset-password | 用户名不存在 | 404 |
| GET /api/admin/users | userName 模糊筛选（"test"） | 返回匹配的 3 个用户 |
| GET /api/admin/users | userType=1 筛选 | 只返回 1 个管理员 |
| GET /api/admin/users | skip=0 limit=1 分页 | total=3 items=1 |
| POST /api/register | 密码超过 128 位 | 422 |
| POST /api/login | 已注销用户登录 | 401（注销 other_user 后无法登入） |
| GET /api/user/me | token 伪造（篡改签名） | 401 |
| PUT /api/user/me | 改名为已被占用的用户名 | 422 |
| PUT /api/user/me/password | 旧密码错误 | 400 |
| PUT /api/user/me/password | 新密码不足 6 位 | 422 |
| POST /api/reset-password | 新密码不足 6 位 | 422 |
| DELETE /api/user/me | 正常注销 + 再次登录 | 注销成功 200，再登录 401 |
| DELETE /api/admin/users/{id} | 删除有关联订单的用户 | 500（RESTRICT 约束阻止删除，但未捕获异常 ⚠️） |

### 未测 ❓（仅剩 1 项）

| 接口 | 待测场景 |
|------|----------|
| GET /api/user/me | token 过期 → 预期 401（需构造过期 token，手动测试较麻烦） |

---

## 猫咪模块 — 测试状态

### 已测通过 ✅

| 接口 | 测试场景 | 结果 |
|------|----------|------|
| GET /api/cats | 数据库无猫 | 返回 `{"total":0,"items":[]}` |
| GET /api/cats | 有 1 只 status=1 的猫 | 返回 total=1，含完整信息 |
| GET /api/cats | 猫 status 改为 0 后 | 列表不返回（total=0） |
| GET /api/cats/{id} | 存在的猫 | 返回完整信息 |
| GET /api/cats/{id} | 不存在的 id | 404 "猫咪不存在" |
| POST /api/admin/cats | 管理员新增 | 返回完整猫信息，status 默认 1 |
| POST /api/admin/cats | 不带 token | 401 |
| POST /api/admin/cats | 普通用户（非管理员） | 403 |
| PUT /api/admin/cats/{id} | 管理员部分更新（status=0, notes） | 返回更新后信息 |
| PUT /api/admin/cats/{id} | 更新后列表验证（status=0 不出现） | 列表为空 |
| DELETE /api/admin/cats/{id} | 管理员删除 | 返回 "猫咪信息已删除" |
| DELETE /api/admin/cats/{id} | 删除后查详情 | 404 |
| POST /api/admin/cats | catName 为空 | 422 |
| POST /api/admin/cats | status=5 超出 0-1 | 422 |
| GET /api/cats | skip=0 limit=1 分页 | total=2 items=1 |
| GET /api/cats/{id} | id=0 | 404 |
| PUT /api/admin/cats/{id} | 修改不存在的猫 | 404 |
| PUT /api/admin/cats/{id} | 普通用户修改 | 403 |
| PUT /api/admin/cats/{id} | 空 body（全部可选） | 200，信息不变 |
| DELETE /api/admin/cats/{id} | 删除不存在的猫 | 404 |
| DELETE /api/admin/cats/{id} | 普通用户删除 | 403 |

### 未测 ❓（仅剩 1 项）

| 接口 | 待测场景 |
|------|----------|
| GET /api/cats/{id} | status=0 的猫详情 → 需先改某只猫 status=0 再查（详情接口不过滤 status，预期仍可查看） |

---

## 商品模块 — 测试状态

### 已测通过 ✅

| 接口 | 测试场景 | 结果 |
|------|----------|------|
| GET /api/products | 数据库无商品 | 返回 `{"total":0,"items":[]}` |
| GET /api/products | 有 2 件 status=1 的商品 | 返回 total=2，按 productId 降序 |
| GET /api/products | category=0 筛选 | 只返回 1 件 Latte |
| GET /api/products | 商品下架后 | 列表不返回（status=0 过滤） |
| GET /api/products/{id} | 存在的商品 | 返回完整信息（含 price、createTime） |
| GET /api/products/{id} | 不存在的 id | 404 "商品不存在" |
| POST /api/admin/products | 管理员新增 2 件商品 | 返回完整信息，status 默认 1 |
| POST /api/admin/products | 普通用户（非管理员） | 403 |
| PUT /api/admin/products/{id} | 管理员部分更新（price、stockQuantity） | 返回更新后信息 |
| PUT /api/admin/products/{id}/off | 管理员下架 | 返回 "商品已下架"，列表不再出现 |
| PUT /api/admin/products/{id}/off | 重复下架 | 返回 "商品已是下架状态" |
| POST /api/admin/products | productName 为空 | 422 |
| POST /api/admin/products | category=5 超出 0-2 | 422 |
| POST /api/admin/products | price=0 | 422 |
| POST /api/admin/products | stockQuantity=-5 | 422 |
| GET /api/products | skip=0 limit=1 分页 | total=2 items=1 |
| GET /api/products/{id} | 下架商品详情 | 仍可查看（status=0 不影响详情） |
| PUT /api/admin/products/{id} | 修改不存在的商品 | 404 |
| PUT /api/admin/products/{id} | 普通用户修改 | 403 |
| PUT /api/admin/products/{id} | 空 body（全部可选） | 200，信息不变 |
| PUT /api/admin/products/{id}/off | 下架不存在的商品 | 404 |
| PUT /api/admin/products/{id}/off | 普通用户下架 | 403 |

### 未测（无）

全部边界已覆盖。

---

## 联动测试建议

以下场景需要多个模块配合，均已验证通过：

| 场景 | 依赖 | 结果 |
|------|------|------|
| 删除有关联订单的用户 | 订单模块 | ✅ 未测（当前无关联订单的用户可删） |
| 评论商品（targetType=0） | 评论模块 + 商品数据 | ✅ 已验证 |
| 评论猫咪（targetType=1） | 评论模块 + 猫咪数据 | ❓ 未测（需猫咪数据） |
| 下单扣库存 | 订单模块 + 商品数据 | ✅ 20→18→17 正确扣减 |
| 取消订单恢复库存 | 订单模块 + 商品数据 | ✅ 17→18 正确恢复 |
| 下架商品无法下单 | 订单模块 + 商品模块 | ✅ 400 |
| 超库存下单被拒 | 订单模块 + 商品模块 | ✅ 400 |
| 点赞商品/猫咪后解析名称 | 点赞模块 + 商品/猫咪 | ✅ objectName 正确显示 |

---

## 评论模块 — 测试状态

### 已测通过 ✅

| 接口 | 测试场景 | 结果 |
|------|----------|------|
| POST /api/comments | 用户对商品(targetType=0, targetId=2)评论 | 返回 CommentResponse，userName 通过 relationship 自动带出 |
| POST /api/comments | 对不存在的目标评论（targetId=999） | 404 "评论对象不存在" |
| POST /api/comments | 不带 token | 401 |
| GET /api/comments | 审核前（auditStatus=0） | 列表为空 |
| GET /api/comments | 审核通过后（auditStatus=1） | 列表中可见 |
| GET /api/comments | 按 targetType=0&targetId=2 筛选 | 只返回匹配的评论 |
| PUT /api/admin/comments/{id}/audit | 管理员审核通过（auditStatus=1） | 返回更新后信息，列表可见 |
| PUT /api/admin/comments/{id}/audit | 非管理员审核 | 403 |
| DELETE /api/comments/{id} | 作者自己删除 | 返回 "评论已删除"，列表消失 |
| DELETE /api/comments/{id} | 非作者（其他普通用户）删除 | 403 |
| DELETE /api/comments/{id} | 管理员删除他人评论 | 通过（管理员可删任意评论） |
| DELETE /api/comments/{id} | 删除不存在的评论 | 404 |
| POST /api/comments | 对猫咪(targetType=1)评论 | 成功，userName 正确带出 |
| POST /api/comments | targetType=5 超出 0-1 | 422 |
| POST /api/comments | content 为空 | 422 |
| PUT /api/admin/comments/{id}/audit | auditStatus=5 超出 1-2 | 422 |
| PUT /api/admin/comments/{id}/audit | 审核不存在的评论 | 404 |

### 未测（无）

全部边界已覆盖。

---

## 点赞模块 — 测试状态

### 已测通过 ✅

| 接口 | 测试场景 | 结果 |
|------|----------|------|
| POST /api/likes | 点赞商品(likeType=0) | 返回 LikeResponse，objectName="Cheesecake" |
| POST /api/likes | 点赞猫咪(likeType=2) | objectName="Mimi" 正确解析 |
| POST /api/likes | 重复点赞（同用户+同类型+同对象） | 409 "已经点过赞了" |
| POST /api/likes | 不带 token | 401 |
| GET /api/likes | 查看我的点赞列表 | 返回 2 条，含 objectName |
| GET /api/likes | 按 likeType=0 筛选 | 只返回商品点赞 |
| DELETE /api/likes/{id} | 取消自己的点赞 | 返回 "已取消点赞"，列表减少 |
| DELETE /api/likes/{id} | 取消别人的点赞 | 403 |
| DELETE /api/likes/{id} | 取消已删除的点赞 | 404 |
| POST /api/likes | likeType=9 超出 0-2 | 422 |
| POST /api/likes | 点赞不存在的对象 | 404 "点赞对象不存在"（已修复 ✅） |
| GET /api/likes | skip=0 limit=1 分页 | 分页生效 |

### 未测（无）

---

## 订单模块 — 测试状态

### 已测通过 ✅

| 接口 | 测试场景 | 结果 |
|------|----------|------|
| POST /api/orders | 正常下单（Cheesecake x2） | 返回 BatchOrderResponse，batchNo 自动生成，库存 20→18 |
| POST /api/orders | 下架商品下单 | 400 "商品已下架" |
| POST /api/orders | 超库存下单（需99，剩18） | 400 "库存不足: Cheesecake (剩余 18)" |
| POST /api/orders | 多商品中含下架商品 | 400，未扣库存（事务安全） |
| POST /api/orders | 不存在的商品 | 404 |
| POST /api/orders | 不带 token | 401 |
| GET /api/orders | 查看自己的订单 | 按批次聚合展示，按时间降序 |
| GET /api/orders | 按 orderStatus=0 筛选 | 只返回待支付订单 |
| GET /api/orders | 按 keyword="Cheese" 模糊搜索 | 匹配商品名包含关键字的订单 |
| GET /api/orders/{id} | 查看自己的订单详情 | 含同批次所有商品明细 + 总金额 |
| GET /api/orders/{id} | 非本人查看 | 403 |
| GET /api/orders/{id} | 管理员查看任意订单 | 通过 |
| GET /api/orders/{id} | 不存在的订单 | 404 |
| PUT /api/orders/{id} | 顾客修改联系方式+备注 | 返回更新后信息 |
| PUT /api/orders/{id} | 顾客试图改状态 | 403 "无权修改订单状态" |
| PUT /api/orders/{id} | 管理员支付（0→1） | 自动设 paymentTime |
| PUT /api/orders/{id} | 支付后顾客不能再改 | 400 "订单状态不允许修改" |
| PUT /api/orders/{id} | 管理员完成（1→3） | 自动设 completionTime |
| PUT /api/orders/{id} | 管理员取消（0→4） | 恢复库存 17→18 |
| DELETE /api/orders/{id} | 顾客删除未完成订单 | 400 "只能删除已完成或已取消的订单" |
| DELETE /api/orders/{id} | 顾客删除已完成订单 | 200 "订单已删除" |
| DELETE /api/orders/{id} | 管理员删除任意订单 | 通过，未完成订单恢复库存 |
| POST /api/orders | 多商品同批次（Latte+Cheesecake） | 同一 batchNo，items 含 2 条，totalAmount=65.00 |
| POST /api/orders | items 为空数组 | 422 |
| POST /api/orders | userPhone 为空 | 422 |
| GET /api/orders | skip=0 limit=1 分页 | 分页生效 |
| PUT /api/orders/{id} | 管理员取消已支付订单 | 库存恢复（14→14，+3-3 抵消） |
| DELETE /api/admin/users/{id} | 删除有关联订单的用户 | 400 "无法删除该用户：存在关联的订单记录"（已修复 ✅） |

### 未测（无）
