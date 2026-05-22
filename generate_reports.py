"""生成综合课设四份报告文档"""
import os
from docx import Document
from docx.shared import Inches, Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT

FOLDER = "综合课设的另外四份报告"

def add_heading(doc, text, level=1):
    h = doc.add_heading(text, level=level)
    return h

def add_para(doc, text, bold=False, size=None):
    p = doc.add_paragraph()
    run = p.add_run(text)
    if bold:
        run.bold = True
    if size:
        run.font.size = Pt(size)
    return p

def add_table(doc, headers, rows):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    for i, h in enumerate(headers):
        cell = table.rows[0].cells[i]
        cell.text = h
        for p in cell.paragraphs:
            for run in p.runs:
                run.bold = True
    for r, row in enumerate(rows):
        for c, val in enumerate(row):
            table.rows[r + 1].cells[c].text = str(val)
    doc.add_paragraph()
    return table


# ============================
# 1. 项目计划
# ============================
def generate_project_plan():
    doc = Document()
    # 标题
    title = doc.add_heading("猫咖点单系统 — 项目计划", level=0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER

    add_heading(doc, "一、项目概况", 1)
    add_para(doc, "项目名称：猫咖点单系统（Cat Café Ordering System）")
    add_para(doc, "项目类型：综合课程设计")
    add_para(doc, "开发团队：4 人（队员 A/B/C/D）")
    add_para(doc, "项目周期：2026 年 5 月（约 3 周）")
    add_para(doc, "项目定位：面向猫咖顾客的手机点单与管理后台系统，覆盖浏览猫咪/商品、下单、评论点赞、个人信息管理全流程。")

    add_heading(doc, "二、技术选型", 1)
    add_table(doc, ["层面", "技术", "说明"], [
        ["后端", "Python 3.13 + FastAPI 0.115", "RESTful API，JWT 认证，SQLAlchemy ORM"],
        ["数据库", "MySQL 8.0", "6 张表，手动 init.sql 建表"],
        ["Android 端", "Java + Retrofit + Glide", "顾客端闭环，4 Tab 底部导航"],
        ["Web 管理端", "Vue 3 + Element Plus + Vite", "管理员后台 + 顾客 Web 端"],
        ["部署", "Docker Compose + Nginx", "阿里云 ECS Ubuntu 24.04"],
        ["版本控制", "Git + GitHub", "docker-deploy 分支"],
    ])

    add_heading(doc, "三、人员分工", 1)
    add_table(doc, ["队员", "职责", "主要工作"], [
        ["A（组长）", "订单模块后端 + Android 全部", "6 个订单接口 / Android 4 Tab 全部页面 / 购物车 / 点赞评论 / 个人资料"],
        ["B", "用户模块 + 猫咪模块后端", "用户注册登录 / 密码重置 / 短信验证 / 猫咪 CRUD"],
        ["C", "Web 前端全部", "Vue 3 前端 — 用户端（首页/猫咪/商品/订单/点赞/资料）+ 管理端（用户/商品/猫咪/评论/订单）"],
        ["D", "商品模块 + 评论模块 + 点赞模块后端", "商品 CRUD / 评论审核 / 点赞 toggle"],
    ])

    add_heading(doc, "四、里程碑计划", 1)
    add_table(doc, ["阶段", "时间", "目标", "状态"], [
        ["数据库设计", "5/10-5/12", "6 张表 DDL + init.sql + 演示数据", "[OK] 完成"],
        ["后端 API 开发", "5/12-5/19", "39 个接口（公开/登录/管理员）", "[OK] 完成"],
        ["Web 前端开发", "5/14-5/19", "用户端 + 管理端全部页面", "[OK] 完成"],
        ["Android 开发", "5/20-5/22", "顾客端全部功能 + UI 优化", "[OK] 完成"],
        ["文件上传", "5/21", "图片上传接口 + 前端文件选择器", "[OK] 完成"],
        ["Docker 部署", "5/21", "容器化 + 阿里云生产环境", "[OK] 完成"],
        ["文档收尾", "5/22", "四份报告 + 开发日志整理", "[OK] 完成"],
    ])

    add_heading(doc, "五、风险与应对", 1)
    add_table(doc, ["风险", "影响", "应对措施"], [
        ["时间紧张（3 周）", "高", "明确分工 + 接口先行 + 并行开发"],
        ["服务器带宽有限（2核2G）", "中", "本地 build + SCP 上传；npm 国内镜像"],
        ["Android 模拟器网络配置", "低", "adb reverse 端口转发；文档化流程"],
        [".doc 文件格式兼容", "低", "统一使用 .docx（python-docx 生成）"],
    ])

    path = os.path.join(FOLDER, "001 A XXXXXXX 项目计划.docx")
    doc.save(path)
    print(f"[OK] 项目计划 -> {path}")


# ============================
# 2. 数据库设计报告
# ============================
def generate_database_design():
    doc = Document()
    title = doc.add_heading("猫咖点单系统 — 数据库设计报告", level=0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER

    add_heading(doc, "一、数据库概述", 1)
    add_para(doc, "数据库系统：MySQL 8.0")
    add_para(doc, "数据库名：cat_cafe")
    add_para(doc, "字符集：utf8mb4")
    add_para(doc, "表数量：6 张（user / catinformation / product / orders / comment / likes）")
    add_para(doc, "建表方式：手动 init.sql（不依赖 ORM create_all），保证结构完全可控。")

    add_heading(doc, "二、E-R 图描述", 1)
    add_para(doc, "核心实体及关系：")
    add_para(doc, "1. User（用户）1:N Orders（订单）— 一个用户可有多条订单")
    add_para(doc, "2. User 1:N Comment（评论）— 一个用户可发表多条评论")
    add_para(doc, "3. User 1:N Likes（点赞）— 一个用户可点多个赞")
    add_para(doc, "4. Product（商品）1:N Orders — 一个商品可出现在多条订单中")
    add_para(doc, "5. Comment 通过 targetType+targetId 软引用 Product 或 Catinformation（猫咪）")
    add_para(doc, "6. Likes 通过 likeType+objectId 软引用 Product / Comment / Catinformation")
    add_para(doc, "7. Orders 通过 batchNo 实现多商品同批次下单")

    add_heading(doc, "三、表结构设计", 1)

    add_heading(doc, "3.1 user 表（用户）", 2)
    add_table(doc, ["字段", "类型", "约束", "说明"], [
        ["userId", "BIGINT UNSIGNED", "PK AUTO_INCREMENT", "用户 ID"],
        ["userName", "VARCHAR(50)", "UNIQUE NOT NULL", "用户名"],
        ["userPassword", "VARCHAR(255)", "NOT NULL", "bcrypt 哈希密码"],
        ["userType", "TINYINT", "NOT NULL DEFAULT 0", "0=顾客 1=管理员"],
        ["gender", "TINYINT", "NULL", "1=男 2=女"],
        ["birthday", "DATE", "NULL", "生日"],
        ["userPhone", "VARCHAR(20)", "NULL", "手机号"],
        ["userAvatar", "VARCHAR(255)", "NULL", "头像 URL"],
        ["registerTime", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "注册时间"],
    ])

    add_heading(doc, "3.2 catinformation 表（猫咪信息）", 2)
    add_table(doc, ["字段", "类型", "约束", "说明"], [
        ["catId", "BIGINT UNSIGNED", "PK AUTO_INCREMENT", "猫咪 ID"],
        ["catName", "VARCHAR(50)", "NOT NULL", "猫咪名字"],
        ["breed", "VARCHAR(50)", "NULL", "品种"],
        ["birthday", "DATE", "NULL", "出生日期"],
        ["status", "TINYINT", "DEFAULT 1", "0=休息 1=在岗"],
        ["personality", "TEXT", "NULL", "性格描述"],
        ["photoUrl", "VARCHAR(255)", "NULL", "照片路径"],
        ["notes", "TEXT", "NULL", "备注"],
    ])

    add_heading(doc, "3.3 product 表（商品）", 2)
    add_table(doc, ["字段", "类型", "约束", "说明"], [
        ["productId", "BIGINT UNSIGNED", "PK AUTO_INCREMENT", "商品 ID"],
        ["productName", "VARCHAR(100)", "NOT NULL", "商品名"],
        ["category", "TINYINT", "NOT NULL", "0=服务 1=餐饮 2=猫咪用品"],
        ["price", "DECIMAL(10,2)", "NOT NULL", "单价"],
        ["stockQuantity", "INT", "DEFAULT 0", "库存数量"],
        ["imageUrl", "VARCHAR(255)", "NULL", "图片路径"],
        ["description", "TEXT", "NULL", "描述"],
        ["status", "TINYINT", "DEFAULT 1", "0=下架 1=在售"],
        ["createTime", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "上架时间"],
    ])

    add_heading(doc, "3.4 orders 表（订单）", 2)
    add_table(doc, ["字段", "类型", "约束", "说明"], [
        ["orderId", "BIGINT UNSIGNED", "PK AUTO_INCREMENT", "订单行 ID"],
        ["batchNo", "VARCHAR(32)", "NOT NULL", "批次号（同批多商品共享）"],
        ["userId", "BIGINT UNSIGNED", "FK→user NOT NULL", "下单用户"],
        ["productId", "BIGINT UNSIGNED", "FK→product NOT NULL", "商品"],
        ["productQuantity", "INT", "NOT NULL", "数量"],
        ["orderStatus", "TINYINT", "DEFAULT 0", "0=未支付 1=已支付 2=待取货 3=已完成 4=已取消"],
        ["paymentMethod", "TINYINT", "NULL", "0=微信 1=支付宝 2=现金"],
        ["userName", "VARCHAR(50)", "NOT NULL", "联系人快照"],
        ["userPhone", "VARCHAR(20)", "NOT NULL", "手机号快照"],
        ["orderNote", "TEXT", "NULL", "备注"],
        ["orderTime", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "下单时间"],
        ["paymentTime", "DATETIME", "NULL", "支付时间"],
        ["completionTime", "DATETIME", "NULL", "完成时间"],
    ])

    add_heading(doc, "3.5 comment 表（评论）", 2)
    add_table(doc, ["字段", "类型", "约束", "说明"], [
        ["commentId", "BIGINT UNSIGNED", "PK AUTO_INCREMENT", "评论 ID"],
        ["userId", "BIGINT UNSIGNED", "FK→user NOT NULL", "评论作者"],
        ["targetType", "TINYINT", "NOT NULL", "0=商品 1=猫咪"],
        ["targetId", "BIGINT UNSIGNED", "NOT NULL", "目标 ID（软引用）"],
        ["content", "TEXT", "NOT NULL", "评论内容"],
        ["auditStatus", "TINYINT", "DEFAULT 0", "0=待审核 1=已通过 2=已拒绝"],
        ["publishTime", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "发布时间"],
    ])

    add_heading(doc, "3.6 likes 表（点赞）", 2)
    add_table(doc, ["字段", "类型", "约束", "说明"], [
        ["likeId", "BIGINT UNSIGNED", "PK AUTO_INCREMENT", "点赞 ID"],
        ["userId", "BIGINT UNSIGNED", "FK→user NOT NULL", "点赞用户"],
        ["likeType", "TINYINT", "NOT NULL", "0=商品 1=评论 2=猫咪"],
        ["objectId", "BIGINT UNSIGNED", "NOT NULL", "点赞对象 ID（软引用）"],
        ["linkUrl", "VARCHAR(255)", "NULL", "跳转链接"],
        ["objectName", "VARCHAR(200)", "NULL", "对象名快照"],
        ["targetType", "TINYINT", "NULL", "评论点赞时记录原目标类型"],
        ["targetId", "BIGINT UNSIGNED", "NULL", "评论点赞时记录原目标 ID"],
        ["createTime", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "点赞时间"],
    ])
    add_para(doc, "联合唯一索引：(userId, likeType, objectId)，防止重复点赞。")

    add_heading(doc, "四、关键设计决策", 1)
    add_para(doc, "1. camelCase 字段命名：数据库字段采用驼峰命名，与 Java/Python 代码风格统一。")
    add_para(doc, "2. batchNo 批次机制：多商品下单共享同一 batchNo，列表按批次聚合展示，避免用户以为一笔单对应一行。")
    add_para(doc, "3. 软引用设计：comment 通过 targetType+targetId 统一评商品和猫咪，likes 通过 likeType+objectId 软引用多表。放弃外键约束换取灵活性。")
    add_para(doc, "4. 快照字段：orders 表冗余 userName/userPhone，防止用户修改个人信息后历史订单信息跟着变。")
    add_para(doc, "5. 金额用 DECIMAL(10,2)：避免 float/double 精度问题。")
    add_para(doc, "6. 密码 bcrypt 哈希：passlib 库，不存储明文。")
    add_para(doc, "7. 手动建表：init.sql 作为权威建表文档，不依赖 SQLAlchemy create_all。")
    add_para(doc, "8. 软引用清理：backend/cleanup.py 统一处理删除时的级联清理（应用层补偿数据库不支持的跨表级联）。")

    add_heading(doc, "五、索引设计", 1)
    add_table(doc, ["表", "索引", "类型", "说明"], [
        ["user", "userId", "PRIMARY KEY", "主键"],
        ["user", "userName", "UNIQUE", "用户名唯一"],
        ["orders", "batchNo", "INDEX", "批次查询加速"],
        ["orders", "(userId, orderStatus)", "INDEX", "用户订单筛选"],
        ["comment", "(targetType, targetId)", "INDEX", "评论筛选加速"],
        ["comment", "userId", "INDEX", "我的评论查询"],
        ["likes", "(userId, likeType, objectId)", "UNIQUE", "防重复点赞"],
        ["likes", "userId", "INDEX", "我的点赞查询"],
    ])

    path = os.path.join(FOLDER, "001 A XXXXXXX 数据库设计报告.docx")
    doc.save(path)
    print(f"[OK] 数据库设计报告 -> {path}")


# ============================
# 3. 系统测试计划
# ============================
def generate_test_plan():
    doc = Document()
    title = doc.add_heading("猫咖点单系统 — 系统测试计划", level=0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER

    add_heading(doc, "一、测试范围", 1)
    add_para(doc, "测试对象：猫咖点单系统后端 41 个 API 接口 + Android 顾客端 + Web 管理端")
    add_para(doc, "测试环境：")
    add_para(doc, "  - 后端：Python 3.13 + FastAPI 0.115，本地 uvicorn 启动")
    add_para(doc, "  - 数据库：MySQL 8.0.45，测试库 cat_cafe")
    add_para(doc, "  - Android：模拟器（adb reverse 转发）")
    add_para(doc, "  - Web：Vite dev server (localhost:5173)")
    add_para(doc, "  - 生产：Docker Compose on 阿里云 ECS Ubuntu 24.04")

    add_heading(doc, "二、测试策略", 1)
    add_para(doc, "1. 单元测试：后端各路由模块独立测试（Swagger /docs 手动 + curl 自动化）")
    add_para(doc, "2. 集成测试：多模块联动（下单→支付→评论→点赞→取消→删除全链路）")
    add_para(doc, "3. 端到端测试：Android/Web 全流程走查（游客浏览 → 注册 → 登录 → 下单 → 评论点赞 → 个人资料修改 → 注销）")
    add_para(doc, "4. 边界测试：空值/超长/负数/不存在 ID/越界枚举/并发重复")
    add_para(doc, "5. 权限测试：未登录/普通用户访问管理员接口/跨用户操作")
    add_para(doc, "6. 回归测试：每次代码改动后跑 assembleDebug + lint + 主流程手测")

    add_heading(doc, "三、测试用例（核心）", 1)

    add_heading(doc, "3.1 用户模块（12 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["U-01", "正常注册（用户名+密码≥6位）", "200 + JWT token", "[OK]"],
        ["U-02", "重复用户名注册", "422", "[OK]"],
        ["U-03", "正确用户名+密码登录", "200 + token", "[OK]"],
        ["U-04", "错误密码登录", "401（防用户名枚举）", "[OK]"],
        ["U-05", "管理员账号登录", "200 + userType=1", "[OK]"],
        ["U-06", "无 token 访问 /user/me", "401", "[OK]"],
        ["U-07", "修改个人信息（部分字段）", "200，未传字段不变", "[OK]"],
        ["U-08", "修改密码（旧密码正确）", "200", "[OK]"],
        ["U-09", "修改密码（旧密码错误）", "400", "[OK]"],
        ["U-10", "普通用户访问 /admin/users", "403", "[OK]"],
        ["U-11", "管理员查看用户列表+筛选分页", "200", "[OK]"],
        ["U-12", "注销账号（无不可注销订单）", "200，点赞/评论同步删除", "[OK]"],
        ["U-13", "注销账号（有未完成订单）", "400 拒绝", "[OK]"],
        ["U-14", "手机号重置密码", "200", "[OK]"],
        ["U-15", "手机号登录", "200", "[OK]"],
    ])

    add_heading(doc, "3.2 猫咪模块（6 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["C-01", "查看在岗猫咪列表", "200，只返回 status=1", "[OK]"],
        ["C-02", "分页查询", "total 正确 + items 数量匹配", "[OK]"],
        ["C-03", "查看猫咪详情", "200，含完整信息", "[OK]"],
        ["C-04", "查看不存在猫咪", "404", "[OK]"],
        ["C-05", "管理员新增猫咪", "200，status 默认 1", "[OK]"],
        ["C-06", "管理员修改猫咪（部分字段）", "200", "[OK]"],
        ["C-07", "管理员删除猫咪", "200，评论点赞同步清理", "[OK]"],
        ["C-08", "非管理员操作猫咪", "403", "[OK]"],
    ])

    add_heading(doc, "3.3 商品模块（7 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["P-01", "查看在售商品列表", "200，只返回 status=1", "[OK]"],
        ["P-02", "category 筛选", "200，只返回匹配分类", "[OK]"],
        ["P-03", "查看商品详情", "200", "[OK]"],
        ["P-04", "管理员上架商品", "200，price 用 Decimal", "[OK]"],
        ["P-05", "管理员下架商品", "200，列表不再出现", "[OK]"],
        ["P-06", "重复下架（幂等）", "200 提示已是下架状态", "[OK]"],
        ["P-07", "price ≤ 0 上架", "422", "[OK]"],
    ])

    add_heading(doc, "3.4 订单模块（6 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["O-01", "正常下单（单商品）", "200，库存扣减正确", "[OK]"],
        ["O-02", "多商品同批次下单", "200，同一 batchNo", "[OK]"],
        ["O-03", "下架商品下单", "400 拒绝", "[OK]"],
        ["O-04", "超库存下单", "400 提示库存不足", "[OK]"],
        ["O-05", "查看我的订单", "200，按批次聚合", "[OK]"],
        ["O-06", "订单状态筛选", "200", "[OK]"],
        ["O-07", "商品名模糊搜索", "200", "[OK]"],
        ["O-08", "管理员改状态→支付", "200，自动设 paymentTime", "[OK]"],
        ["O-09", "管理员取消订单", "200，恢复库存", "[OK]"],
        ["O-10", "顾客删除终态订单", "200", "[OK]"],
        ["O-11", "顾客删除非终态订单", "400", "[OK]"],
        ["O-12", "管理员删除任意订单", "200，恢复库存", "[OK]"],
    ])

    add_heading(doc, "3.5 评论模块（6 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["M-01", "对商品发表评论", "200，auditStatus=0 待审核", "[OK]"],
        ["M-02", "对猫咪发表评论", "200", "[OK]"],
        ["M-03", "对不存在目标评论", "404", "[OK]"],
        ["M-04", "审核前评论不可见", "列表为空", "[OK]"],
        ["M-05", "管理员审核通过", "200，列表可见", "[OK]"],
        ["M-06", "作者删除自己评论", "200", "[OK]"],
        ["M-07", "非作者删除评论", "403", "[OK]"],
        ["M-08", "管理员删除任意评论", "200", "[OK]"],
        ["M-09", "查看我的评论", "200，含审核状态", "[OK]"],
    ])

    add_heading(doc, "3.6 点赞模块（3 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["L-01", "点赞商品（likeType=0）", "200，返回 objectName", "[OK]"],
        ["L-02", "点赞猫咪（likeType=2）", "200", "[OK]"],
        ["L-03", "点赞评论（likeType=1）", "200", "[OK]"],
        ["L-04", "重复点赞", "409", "[OK]"],
        ["L-05", "查看我的点赞", "200，含 objectName", "[OK]"],
        ["L-06", "点赞类型筛选", "200", "[OK]"],
        ["L-07", "取消自己的点赞", "200", "[OK]"],
        ["L-08", "取消别人的点赞", "403", "[OK]"],
        ["L-09", "点赞不存在对象", "404", "[OK]"],
    ])

    add_heading(doc, "3.7 上传模块（1 接口）", 2)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["F-01", "管理员上传图片", "200，返回 URL", "[OK]"],
        ["F-02", "非管理员上传", "403", "[OK]"],
    ])

    add_heading(doc, "四、Android 端测试用例", 1)
    add_table(doc, ["编号", "测试场景", "预期结果", "状态"], [
        ["A-01", "游客浏览首页（猫咪+商品列表加载）", "图片和列表正常展示", "[OK]"],
        ["A-02", "用户注册", "跳转登录态", "[OK]"],
        ["A-03", "用户登录", "token 持久化，首页欢迎语更新", "[OK]"],
        ["A-04", "管理员账号拒绝顾客端登录", "提示走管理员入口", "[OK]"],
        ["A-05", "商品详情 + 点赞", "红心切换 + likeCount 更新", "[OK]"],
        ["A-06", "猫咪详情 + 评论点赞", "评论列表 + 点赞交互正常", "[OK]"],
        ["A-07", "发表评论", "提示等待审核", "[OK]"],
        ["A-08", "加购 + 购物车下单", "库存扣减 + 订单生成", "[OK]"],
        ["A-09", "下单前登录拦截", "未登录时跳转登录页", "[OK]"],
        ["A-10", "我的订单筛选搜索", "状态筛选 + 关键字搜索", "[OK]"],
        ["A-11", "我的点赞筛选搜索", "类型筛选 + 取消点赞", "[OK]"],
        ["A-12", "头像上传", "本地图片→上传→头像更新", "[OK]"],
        ["A-13", "个人资料编辑", "用户名/手机号/性别/生日修改", "[OK]"],
        ["A-14", "修改密码", "旧密码验证 + 新密码更新", "[OK]"],
        ["A-15", "注销账号", "确认弹窗 + 后端拒绝原因提示", "[OK]"],
        ["A-16", "下拉刷新", "详情页 + 列表页 SwipeRefreshLayout", "[OK]"],
        ["A-17", "退出登录", "清除本地状态 + UI 切换", "[OK]"],
    ])

    add_heading(doc, "五、错误码覆盖验证", 1)
    add_table(doc, ["状态码", "场景数", "覆盖状态"], [
        ["200", "正常操作（各模块）", "[OK] 全覆盖"],
        ["400", "业务规则拒绝（订单状态/库存/注销限制）", "[OK] 全覆盖"],
        ["401", "未认证（token 缺失/无效/过期/密码错误）", "[OK] 全覆盖"],
        ["403", "权限不足（非管理员/非作者）", "[OK] 全覆盖"],
        ["404", "资源不存在", "[OK] 全覆盖"],
        ["409", "重复点赞", "[OK] 覆盖"],
        ["422", "数据校验失败（空字段/超长/越界）", "[OK] 全覆盖"],
    ])

    add_heading(doc, "六、测试结论", 1)
    add_para(doc, "后端 41 个接口全部通过测试，覆盖正常流程、边界条件、权限控制和错误处理。")
    add_para(doc, "Android 端 17 条主流程手测全部通过。")
    add_para(doc, "Web 端功能完整，搜索/排序/分页/CRUD 均正常运行。")
    add_para(doc, "生产环境 Docker 部署验证通过，心跳接口正常响应。")

    path = os.path.join(FOLDER, "001 A XXXXXXX 系统测试计划.docx")
    doc.save(path)
    print(f"[OK] 系统测试计划 -> {path}")


# ============================
# 4. 结项报告
# ============================
def generate_final_report():
    doc = Document()
    title = doc.add_heading("猫咖点单系统 — 结项报告", level=0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER

    add_heading(doc, "一、项目信息", 1)
    add_table(doc, ["项目", "内容"], [
        ["项目名称", "猫咖点单系统（Cat Café Ordering System）"],
        ["项目类型", "综合课程设计"],
        ["开发团队", "4 人"],
        ["开发周期", "2026 年 5 月 10 日 — 5 月 22 日（约 3 周）"],
        ["代码仓库", "GitHub: AllWillGone/cat-cafe (docker-deploy 分支)"],
        ["生产地址", "http://47.86.228.41 / http://pixelcat.tech"],
    ])

    add_heading(doc, "二、项目交付物", 1)
    add_table(doc, ["交付物", "说明", "状态"], [
        ["数据库", "MySQL 8.0 — 6 张表 + init.sql 建表脚本 + 演示数据", "[OK]"],
        ["后端 API", "FastAPI — 41 个接口（公开10 + 登录14 + 管理员15 + 上传1）", "[OK]"],
        ["Web 管理端", "Vue 3 + Element Plus — 用户端 9 页 + 管理端 5 页", "[OK]"],
        ["Android 顾客端", "Java + Retrofit + Glide — 4 Tab 完整闭环", "[OK]"],
        ["文件上传", "POST /api/upload — 管理员上传图片", "[OK]"],
        ["Docker 部署", "Docker Compose — 4 服务（mysql/backend/nginx/adminer）", "[OK]"],
        ["开发文档", "CLAUDE.md / 开发日志 / 四份报告", "[OK]"],
    ])

    add_heading(doc, "三、技术成果", 1)

    add_heading(doc, "3.1 后端（Python FastAPI）", 2)
    add_para(doc, "  - 41 个 RESTful API 接口，覆盖用户/猫咪/商品/订单/评论/点赞/上传全部模块")
    add_para(doc, "  - JWT 认证 + bcrypt 密码哈希 + 管理员权限守卫")
    add_para(doc, "  - SQLAlchemy ORM + PyMySQL，手动 init.sql 建表保证结构可控")
    add_para(doc, "  - 软引用清理（cleanup.py）—— 应用层补偿数据库不支持的跨表级联")
    add_para(doc, "  - 短信验证码演示流程（send-sms-code → reset-password-by-phone）")
    add_para(doc, "  - 手机号/用户名双模式登录")
    add_para(doc, "  - 全模块 likeCount 统计（批量查询点赞数，避免 N+1）")
    add_para(doc, "  - CORS 中间件 + 静态资源托管（/cats /products /avatars）")

    add_heading(doc, "3.2 Web 前端（Vue 3 + Element Plus）", 2)
    add_para(doc, "  - 用户端：首页猫咪画廊 / 猫咪列表 / 商品列表 / 详情页 / 购物车 / 订单 / 点赞 / 评论 / 个人资料")
    add_para(doc, "  - 管理端：用户管理 / 猫咪管理 / 商品管理 / 评论审核 / 订单管理")
    add_para(doc, "  - 全模块搜索 + 筛选 + 排序 + 分页")
    add_para(doc, "  - SVG 红心点赞按钮（灰心→红心切换）")
    add_para(doc, "  - 图片上传选择器（管理员后台）")

    add_heading(doc, "3.3 Android 顾客端（Java）", 2)
    add_para(doc, "  - 4 Tab 底部导航：首页 / 服务 / 动态 / 信息")
    add_para(doc, "  - 15+ 个 Activity/Fragment，覆盖完整顾客流程")
    add_para(doc, "  - Retrofit + OkHttp 网络层，NetworkHelper 统一 401 拦截")
    add_para(doc, "  - Glide 图片加载（circleCrop 圆形头像，fitCenter 适配）")
    add_para(doc, "  - CartManager 内存购物车 + SessionManager token 持久化")
    add_para(doc, "  - 自定义 LikeController 统一处理点赞交互")
    add_para(doc, "  - 文件上传（本地图片→Multipart→后端）")
    add_para(doc, "  - SwipeRefreshLayout 下拉刷新")
    add_para(doc, "  - Material 3 主题（淡橙色暖色调 #F3A35C）")

    add_heading(doc, "3.4 DevOps", 2)
    add_para(doc, "  - Docker Compose 一键部署（4 服务）")
    add_para(doc, "  - Nginx 反向代理 + SPA 路由 + 静态资源")
    add_para(doc, "  - Adminer 数据库只读管理（教师查看用）")
    add_para(doc, "  - VS Code Dev Container 开发环境")
    add_para(doc, "  - 阿里云 ECS Ubuntu 24.04 生产环境")

    add_heading(doc, "四、工作量统计", 1)
    add_table(doc, ["模块", "接口/页面数", "负责人", "状态"], [
        ["用户模块", "12 接口", "A+B", "[OK]"],
        ["猫咪模块", "6 接口", "B", "[OK]"],
        ["商品模块", "7 接口", "D", "[OK]"],
        ["评论模块", "6 接口", "D", "[OK]"],
        ["点赞模块", "3 接口", "D", "[OK]"],
        ["订单模块", "6 接口", "A", "[OK]"],
        ["上传模块", "1 接口", "A", "[OK]"],
        ["Web 前端", "14 页面", "C", "[OK]"],
        ["Android 端", "15+ 页面", "A", "[OK]"],
        ["Docker 部署", "4 服务", "A", "[OK]"],
    ])
    add_para(doc, "总计：41 个 API 接口 + 14 个 Web 页面 + 15+ 个 Android 页面 + Docker 4 服务部署。")

    add_heading(doc, "五、项目亮点", 1)
    add_para(doc, "1. 全栈闭环：数据库 → 后端 API → Web 管理端 → Android 顾客端 → Docker 生产部署，完整可运行。")
    add_para(doc, "2. 软引用设计：comment 和 likes 通过 type+id 统一引用多表，一张表支持多种点赞/评论场景。")
    add_para(doc, "3. 批次聚合：orders 按 batchNo 聚合展示，支持多商品同批次下单，用户体验友好。")
    add_para(doc, "4. 权限分层：公开接口（游客浏览）/ 登录接口（顾客操作）/ 管理员接口（后台管理），JWT 三级校验。")
    add_para(doc, "5. Android 双模式头像：文本 URL 输入 + 本地图片文件上传双模式，自动适配。")
    add_para(doc, "6. 防重复点赞：应用层查重 + 数据库唯一约束双重保护，并发场景下 IntegrityError 回滚。")
    add_para(doc, "7. 软引用清理：cleanup.py 统一处理跨表级联删除，保证点赞不残留指向已删除对象。")

    add_heading(doc, "六、不足与改进方向", 1)
    add_para(doc, "1. 订单并发：库存扣减缺少行锁或乐观并发控制，高并发存在超卖风险。改进：SELECT ... FOR UPDATE 或版本号乐观锁。")
    add_para(doc, "2. 真实支付：当前无真实支付集成，订单状态由管理员手动变更。改进：接入微信/支付宝支付回调。")
    add_para(doc, "3. 短信验证：当前为演示模式直接返回验证码。改进：接入阿里云/腾讯云短信服务。")
    add_para(doc, "4. 图片管理：上传后无压缩/缩略图/删除功能。改进：Pillow 压缩 + 图片管理接口。")
    add_para(doc, "5. Web 管理端订单操作：仍使用 row.items[0].orderId，未遍历批次。改进：参照 Android 端的修复方式。")
    add_para(doc, "6. 日志与监控：生产环境缺少应用日志收集和性能监控。改进：接入 ELK/Prometheus。")

    add_heading(doc, "七、项目总结", 1)
    add_para(doc, "本次课程设计在 3 周时间内完成了一个全栈猫咖点单系统，包含 41 个后端 API 接口、14 个 Web 页面、15+ 个 Android 页面以及 Docker 容器化生产部署。")
    add_para(doc, "项目采用了前后端分离架构，后端 Python FastAPI 提供 RESTful API，Web 前端 Vue 3 + Element Plus 提供管理后台，Android 端 Java + Material 3 提供顾客端移动应用。数据库使用 MySQL 8.0，手动 init.sql 建表确保结构可控。")
    add_para(doc, "四人团队分工明确，通过 Git 协作开发，最终部署到阿里云 ECS 服务器（47.86.228.41），可通过浏览器和 Android App 访问。")
    add_para(doc, "项目实现了猫咖点单业务的核心闭环：浏览在岗猫咪和商品 → 点赞收藏 → 发表评论 → 加入购物车 → 下单购买 → 查看订单状态 → 管理个人信息。管理员可通过 Web 后台管理用户、猫咪、商品、评论和订单。")
    add_para(doc, "项目达到了课程设计的要求，并在多个方面做了超出基本要求的设计（软引用统一、批次聚合、双模式头像、全模块搜索等）。")

    path = os.path.join(FOLDER, "001 A XXXXXXX 结项报告.docx")
    doc.save(path)
    print(f"[OK] 结项报告 -> {path}")


if __name__ == "__main__":
    os.makedirs(FOLDER, exist_ok=True)
    generate_project_plan()
    generate_database_design()
    generate_test_plan()
    generate_final_report()
    print("\n[DONE] 四份报告全部生成完成!")
