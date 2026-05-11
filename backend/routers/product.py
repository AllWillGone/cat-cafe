"""商品模块 — 商品列表/详情/管理员上架下架"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Product, User
from schemas import ProductCreate, ProductUpdate, ProductResponse, PaginatedProducts
from auth import get_current_admin

router = APIRouter(prefix="/api", tags=["商品模块"])


@router.get("/products", response_model=PaginatedProducts)
def list_products(
    category: int | None = None,           # 查询参数 ?category=0/1/2，不传则不过滤
    skip: int = 0,                         # 分页偏移量
    limit: int = 20,                       # 每页条数
    db: Session = Depends(get_db),
):
    """公开接口 — 浏览在售商品，可按分类筛选"""
    q = db.query(Product).filter(Product.status == 1)  # 只返回 status=1（在售），下架的不出现
    if category is not None:
        q = q.filter(Product.category == category)     # 0=服务 1=餐饮 2=猫咪用品
    total = q.count()                                   # 筛选后的总数
    products = q.order_by(Product.productId.desc()).offset(skip).limit(limit).all()
    return PaginatedProducts(
        total=total,
        items=[ProductResponse.model_validate(p) for p in products],
    )


@router.get("/products/{product_id}", response_model=ProductResponse)
def get_product(product_id: int, db: Session = Depends(get_db)):
    """公开接口 — 查看单个商品详情"""
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    return product


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
