"""订单模块 — 下单/查单/改单/删单"""
import uuid                                         # 生成唯一批次号
from datetime import datetime
from decimal import Decimal
from collections import defaultdict                 # 自动创建默认值的字典，按 batchNo 分组用

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Order, Product, User
from schemas import (
    OrderCreate, OrderUpdate, AdminOrderUpdate,
    BatchOrderResponse, OrderDetailItem, PaginatedOrders,
)
from auth import get_current_user, get_current_admin

router = APIRouter(prefix="/api", tags=["订单模块"])


# ── 辅助函数 ──

def _restore_stock(order: Order, db: Session):
    """取消/删除订单时恢复商品库存"""
    if order.batchNo:                                          # 有批次号 → 恢复同一批所有商品
        siblings = db.query(Order).filter(Order.batchNo == order.batchNo).all()
    else:
        siblings = [order]                                     # 无批次号（老数据）→ 只恢复自己
    for o in siblings:
        product = db.query(Product).filter(Product.productId == o.productId).first()
        if product:
            product.stockQuantity += o.productQuantity         # 把扣掉的库存加回去


def _build_batch(orders: list[Order]) -> BatchOrderResponse:
    """将同一批次的多条 Order 行聚合为一个订单响应（含明细列表 + 总金额）"""
    first = orders[0]
    items = []
    total = Decimal("0")                                       # Decimal 精确加总，避免浮点误差
    for o in orders:
        product_name = o.product.productName if o.product else "已删除的商品"
        items.append(OrderDetailItem(
            orderId=o.orderId,
            productId=o.productId,
            productName=product_name,
            productQuantity=o.productQuantity,
            totalAmount=o.totalAmount,
        ))
        total += o.totalAmount
    return BatchOrderResponse(
        batchNo=first.batchNo,
        userId=first.userId,
        orderStatus=first.orderStatus,                         # 同批次状态一致
        paymentMethod=first.paymentMethod,
        orderTime=first.orderTime,
        userPhone=first.userPhone,
        userName=first.userName,
        orderNote=first.orderNote,
        totalAmount=total,                                     # 整批总价 = 各商品小计之和
        items=items,
    )


def _group_by_batch(orders: list[Order]) -> list[BatchOrderResponse]:
    """将订单列表按 batchNo 分组，每组聚合为一个 BatchOrderResponse，按时间降序"""
    groups: dict[str, list[Order]] = defaultdict(list)        # defaultdict(list): 不存在的 key 自动创建空列表
    singles: list[Order] = []                                  # 无批次号的单商品订单
    for o in orders:
        if o.batchNo:
            groups[o.batchNo].append(o)
        else:
            singles.append(o)
    result = []
    for batch_orders in groups.values():
        result.append(_build_batch(batch_orders))
    for o in singles:
        result.append(_build_batch([o]))                       # 单个也包装成批次格式
    result.sort(key=lambda b: b.orderTime, reverse=True)       # lambda: 匿名函数，按 orderTime 降序
    return result


# ── 接口 ──

@router.post("/orders", response_model=BatchOrderResponse)
def create_order(
    data: OrderCreate,                                         # { items, paymentMethod?, userPhone, userName, orderNote? }
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """下单 — 支持多商品同批次，扣库存"""
    batch_no = uuid.uuid4().hex[:16]                           # 生成 16 位随机批次号，同一批商品共用
    created: list[Order] = []

    for item in data.items:                                    # 遍历订单中每个商品
        product = db.query(Product).filter(Product.productId == item.productId).first()
        if not product:
            raise HTTPException(status_code=404, detail=f"商品不存在: ID={item.productId}")
        if product.status == 0:                                # 已下架不能下单
            raise HTTPException(status_code=400, detail=f"商品已下架: {product.productName}")
        if product.stockQuantity < item.productQuantity:       # 库存不够
            raise HTTPException(status_code=400,
                detail=f"库存不足: {product.productName} (剩余 {product.stockQuantity})")

        product.stockQuantity -= item.productQuantity          # 扣库存（此时未 commit，其他请求看不到）
        total_amount = product.price * item.productQuantity    # 小计 = 单价 × 数量

        order = Order(
            batchNo=batch_no,
            userId=current_user.userId,
            productId=item.productId,
            productQuantity=item.productQuantity,
            totalAmount=total_amount,
            orderStatus=0,                                     # 0 = 待处理
            paymentMethod=data.paymentMethod,
            userPhone=data.userPhone,
            userName=data.userName,
            orderNote=data.orderNote,
        )
        db.add(order)
        created.append(order)

    db.commit()                                                # 一次事务提交所有操作
    for o in created:
        db.refresh(o)                                          # 获取自增 orderId 和时间戳
    return _build_batch(created)                               # 聚合为批次响应


@router.get("/orders", response_model=PaginatedOrders)
def list_orders(
    orderStatus: int | None = None,                            # 查询参数，五态筛选
    keyword: str | None = None,                                # 查询参数，按商品名模糊搜
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """查看自己的订单 — 支持状态筛选 + 商品名模糊搜索，按 batchNo 聚合展示"""
    q = db.query(Order).filter(Order.userId == current_user.userId)  # 只看当前用户的订单

    if orderStatus is not None:
        q = q.filter(Order.orderStatus == orderStatus)
    if keyword:                                                # 按商品名模糊匹配，LEFT JOIN 产品表
        q = q.join(Product, Order.productId == Product.productId, isouter=True)\
             .filter(Product.productName.like(f"%{keyword}%"))

    q = q.order_by(Order.orderTime.desc())
    total = q.count()
    orders = q.offset(skip).limit(limit).all()

    # 去重：同一批次只保留一个代表，避免列表里同一单出现多次
    seen_batches: set[str] = set()
    unique_orders: list[Order] = []
    for o in orders:
        if o.batchNo:
            if o.batchNo not in seen_batches:
                seen_batches.add(o.batchNo)
                unique_orders.append(o)
        else:
            unique_orders.append(o)                            # 无批次号的单商品直接保留

    batched = _group_by_batch(unique_orders)                   # 按批次聚合
    return PaginatedOrders(total=total, items=batched)


# ── 管理员订单管理 ──

@router.get("/admin/orders", response_model=PaginatedOrders)
def admin_list_orders(
    orderStatus: int | None = None,
    keyword: str | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员查看所有订单 — 按状态筛选 + 关键字搜批次号/用户名"""
    q = db.query(Order)
    if orderStatus is not None:
        q = q.filter(Order.orderStatus == orderStatus)
    if keyword:
        like = f"%{keyword}%"
        q = q.filter(
            Order.batchNo.like(like) | Order.userName.like(like)
        )
    q = q.order_by(Order.orderTime.desc())
    total = q.count()
    orders = q.offset(skip).limit(limit).all()

    # 按批次去重
    seen = set()
    unique = []
    for o in orders:
        key = o.batchNo or str(o.orderId)
        if key not in seen:
            seen.add(key)
            unique.append(o)

    batched = _group_by_batch(unique)
    return PaginatedOrders(total=total, items=batched)


@router.get("/orders/{order_id}", response_model=BatchOrderResponse)
def get_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """订单详情 — 顾客看自己，管理员看任意"""
    order = db.query(Order).filter(Order.orderId == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="订单不存在")

    if current_user.userType != 1 and order.userId != current_user.userId:  # 非管理员且不是自己的
        raise HTTPException(status_code=403, detail="无权查看此订单")

    if order.batchNo:                                          # 有批次 → 拉取同批所有商品
        siblings = db.query(Order).filter(Order.batchNo == order.batchNo).all()
    else:
        siblings = [order]
    return _build_batch(siblings)


@router.put("/orders/{order_id}", response_model=BatchOrderResponse)
def update_order(
    order_id: int,
    data: AdminOrderUpdate,                                    # 管理员可改状态，顾客仅联系方式
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """修改订单 — 顾客只能改联系方式/备注（仅限待处理状态），管理员可改状态"""
    order = db.query(Order).filter(Order.orderId == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="订单不存在")

    is_admin = current_user.userType == 1

    # ── 顾客权限校验 ──
    if not is_admin:
        if order.userId != current_user.userId:
            raise HTTPException(status_code=403, detail="无权修改此订单")
        if order.orderStatus != 0:                             # status=0（待处理）才允许改
            raise HTTPException(status_code=400, detail="订单状态不允许修改")
        if data.orderStatus is not None:                       # 顾客不允许改状态字段
            raise HTTPException(status_code=403, detail="无权修改订单状态")

    # ── 管理员改状态时自动记录时间 ──
    if is_admin and data.orderStatus is not None:
        if data.orderStatus == 1 and order.orderStatus != 1:   # 改为"已支付"→ 记录支付时间
            order.paymentTime = datetime.now()
        elif data.orderStatus == 3 and order.orderStatus != 3: # 改为"已完成"→ 记录完成时间
            order.completionTime = datetime.now()
        elif data.orderStatus == 4 and order.orderStatus != 4: # 改为"已取消"→ 恢复库存
            _restore_stock(order, db)

    # ── 更新传入的字段 ──
    if data.orderStatus is not None:
        order.orderStatus = data.orderStatus
    if data.userPhone is not None:
        order.userPhone = data.userPhone
    if data.userName is not None:
        order.userName = data.userName
    if data.orderNote is not None:
        order.orderNote = data.orderNote

    db.commit()
    db.refresh(order)

    if order.batchNo:
        siblings = db.query(Order).filter(Order.batchNo == order.batchNo).all()
    else:
        siblings = [order]
    return _build_batch(siblings)


@router.delete("/orders/{order_id}")
def delete_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """删除订单 — 顾客只能删已完成/已取消的，管理员删非终态订单会恢复库存"""
    order = db.query(Order).filter(Order.orderId == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="订单不存在")

    is_admin = current_user.userType == 1

    if not is_admin:
        if order.userId != current_user.userId:
            raise HTTPException(status_code=403, detail="无权删除此订单")
        # 顾客只能删终态订单（已完成3 或 已取消4）
        if order.orderStatus not in (3, 4):
            raise HTTPException(status_code=400, detail="只能删除已完成或已取消的订单")
    else:
        # 管理员删未完成订单 → 先恢复库存
        if order.orderStatus not in (3, 4):
            _restore_stock(order, db)

    db.delete(order)
    db.commit()
    return {"message": "订单已删除"}
