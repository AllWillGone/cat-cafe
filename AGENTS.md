# Agent Rules

本项目是猫咖管理系统：后端 FastAPI + SQLAlchemy + MySQL，Android 端尚未正式创建。修改代码前必须以仓库真实文件为准，不凭记忆补全。

## 权威信息来源
- 数据库结构以 `database/init.sql` 为准；`models.py` 只能映射结构，不负责建表，禁止新增 `create_all()`。
- 后端现状以 `backend/CLAUDE.md`、`docs/开发日志.md` 和实际代码共同确认；根目录 `CLAUDE.md` 可能包含旧进度。
- 接口实现以 `backend/routers/`、`backend/schemas.py`、`backend/models.py` 为准。发现文档和代码不一致时，先指出差异，再决定是否同步。

## 修改前必做
- 先读相关模块代码、Schema、Model、路由和开发日志，不确定的字段/状态码/权限规则不得猜。
- 任何涉及数据库字段、表关系、状态枚举、接口路径、响应结构的改动，必须同时检查 `init.sql`、`models.py`、`schemas.py`、对应 router 和文档。
- 不要修改或提交 `.env`、`.venv/`、本地 IDE 配置、数据库密码等敏感/本机文件。

## 改动边界
- 每次任务只修改与需求直接相关的文件，禁止顺手重构、统一风格、批量改名或移动目录。
- 禁止为了“看起来更完整”新增未被需求要求的接口、字段、表、依赖或架构层。
- 不确定需求时先提问；不确定代码行为时先查代码或运行验证，不得自行脑补。
- 修改完成后必须说明：改动文件、影响接口、验证命令、未验证风险。

## 后端约束
- 保持现有命名：数据库字段使用 camelCase，表名沿用 `user/catinformation/product/orders/comment/likes`，接口路径沿用现有 `/api/...` 风格。
- 认证和权限必须复用 `auth.py` 的 `get_current_user` / `get_current_admin`，不要相信客户端传入的 `userId` 或 `userType`。
- 订单必须尊重 `batchNo` 聚合、库存扣减/恢复、订单状态 `0-4`；商品下架使用 `status=0`，不要为历史订单随意硬删商品。
- 评论 `targetType + targetId`、点赞 `likeType + objectId` 是软引用；修改时必须保留目标存在性校验。
- 金额字段使用 `Decimal` / `Numeric(10, 2)`，不要改成 float。

## 验证与收尾
- 后端改动后至少运行语法检查：`python -m compileall backend`；能启动时再用 `uvicorn main:app --reload` 和 `/docs` 手测相关接口。
- 修改接口、字段、状态码、权限或已知行为后，同步更新 `docs/开发日志.md` 和必要的 `CLAUDE.md`。
- 不能验证的内容必须明确写出原因，不要把未测内容说成已完成。
