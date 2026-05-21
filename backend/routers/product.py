"""商品模块 — 商品列表/详情/管理员上架下架"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import func

from database import get_db
from models import Product, User, Likes, Order
from schemas import ProductCreate, ProductUpdate, ProductResponse, PaginatedProducts
from auth import get_current_admin
from cleanup import delete_comments_for_target, delete_likes_for_object

router = APIRouter(prefix="/api", tags=["商品模块"])


@router.get("/products", response_model=PaginatedProducts)
def list_products(
    category: int | None = None,
    keyword: str | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
):
    """公开接口 — 浏览在售商品，可按分类筛选 + 关键字搜索"""
    q = db.query(Product).filter(Product.status == 1)
    if category is not None:
        q = q.filter(Product.category == category)
    if keyword:
        q = q.filter(Product.productName.like(f"%{keyword}%"))
    total = q.count()
    products = q.order_by(Product.productId.desc()).offset(skip).limit(limit).all()

    pids = [p.productId for p in products]
    counts = {}
    if pids:
        rows = db.query(Likes.objectId, func.count(Likes.likeId)).filter(
            Likes.likeType == 0, Likes.objectId.in_(pids)
        ).group_by(Likes.objectId).all()
        counts = dict(rows)

    items = []
    for p in products:
        d = ProductResponse.model_validate(p)
        d.likeCount = counts.get(p.productId, 0)
        items.append(d)
    return PaginatedProducts(total=total, items=items)


@router.get("/products/{product_id}", response_model=ProductResponse)
def get_product(product_id: int, db: Session = Depends(get_db)):
    """公开接口 — 查看单个商品详情"""
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    like_count = db.query(func.count(Likes.likeId)).filter(
        Likes.likeType == 0, Likes.objectId == product_id
    ).scalar()
    resp = ProductResponse.model_validate(product)
    resp.likeCount = like_count or 0
    return resp


@router.get("/admin/products", response_model=PaginatedProducts)
def admin_list_products(
    category: int | None = None,
    status: int | None = None,
    keyword: str | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员查看所有商品 — 含下架商品，可按分类/状态筛选，关键字搜商品名"""
    q = db.query(Product)
    if category is not None:
        q = q.filter(Product.category == category)
    if status is not None:
        q = q.filter(Product.status == status)
    if keyword:
        q = q.filter(Product.productName.like(f"%{keyword}%"))
    total = q.count()
    products = q.order_by(Product.productId.desc()).offset(skip).limit(limit).all()
    return PaginatedProducts(
        total=total,
        items=[ProductResponse.model_validate(p) for p in products],
    )


@router.post("/admin/products", response_model=ProductResponse)
def create_product(
    data: ProductCreate,                       # 请求体，含 productName/category/price 等
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),  # 管理员守卫
):
    """管理员接口 — 上架新商品，status 默认 1（在售）"""
    product = Product(**data.model_dump())      # ** 解包请求体字段传给 ORM 构造器
    db.add(product)
    db.commit()
    db.refresh(product)                         # 获取自增的 productId 和 createTime
    return product


@router.put("/admin/products/{product_id}", response_model=ProductResponse)
def update_product(
    product_id: int,
    data: ProductUpdate,                       # 全部字段可选，只更新传入的
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员接口 — 修改商品信息，只更新传入的字段"""
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    for field, value in data.model_dump(exclude_none=True).items():  # exclude_none=True 跳过未传字段
        setattr(product, field, value)          # 动态设置属性
    db.commit()
    db.refresh(product)
    return product


@router.delete("/admin/products/{product_id}")
def delete_product(
    product_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员接口 — 删除无订单商品；有历史订单时改为下架保留数据"""
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    has_orders = db.query(Order).filter(Order.productId == product_id).first()
    if has_orders:
        delete_likes_for_object(db, 0, product_id)
        delete_comments_for_target(db, 0, product_id)
        if product.status != 0:
            product.status = 0
        db.commit()
        return {"message": "商品存在历史订单，已改为下架"}
    delete_likes_for_object(db, 0, product_id)
    delete_comments_for_target(db, 0, product_id)
    db.delete(product)
    db.commit()
    return {"message": "商品已删除"}


@router.put("/admin/products/{product_id}/off")
def delist_product(
    product_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员接口 — 下架商品（status 改为 0），已下架的再调直接提示"""
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    if product.status == 0:                     # 已下架，幂等处理，直接返回提示
        return {"message": "商品已是下架状态"}
    product.status = 0                          # 设为 0（下架）
    db.commit()
    return {"message": "商品已下架"}
