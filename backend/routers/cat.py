"""猫咪模块 — 猫咪列表/详情/管理员CRUD"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Catinformation, User
from schemas import CatCreate, CatUpdate, CatResponse, PaginatedCats
from auth import get_current_admin

router = APIRouter(prefix="/api", tags=["猫咪模块"])


@router.get("/cats", response_model=PaginatedCats)
def list_cats(skip: int = 0, limit: int = 20, db: Session = Depends(get_db)):
    """公开接口 — 查看猫咪列表，只返回在岗(status=1)的猫咪"""
    q = db.query(Catinformation).filter(Catinformation.status == 1)  # WHERE status = 1，过滤休息中的猫
    total = q.count()                        # SELECT COUNT(*) 获取总数（分页用）
    cats = q.order_by(Catinformation.catId.desc()).offset(skip).limit(limit).all()  # 降序 + 偏移 + 限量 → 分页查询
    return PaginatedCats(
        total=total,
        items=[CatResponse.model_validate(c) for c in cats],  # 列表推导式：把每个 ORM 对象转成响应模型
    )


@router.get("/cats/{cat_id}", response_model=CatResponse)
def get_cat(cat_id: int, db: Session = Depends(get_db)):
    """公开接口 — 查看单只猫咪详情，不限制 status"""
    cat = db.query(Catinformation).filter(Catinformation.catId == cat_id).first()  # first() 查到返回对象，查不到返回 None
    if not cat:
        raise HTTPException(status_code=404, detail="猫咪不存在")
    return cat  # 框架自动按 response_model 转成 CatResponse


@router.post("/admin/cats", response_model=CatResponse)
def create_cat(
    data: CatCreate,                        # 请求体，Pydantic 自动校验
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),  # 管理员守卫，非管理员直接 403
):
    """管理员接口 — 新增猫咪"""
    cat = Catinformation(**data.model_dump())  # model_dump() 将请求体转字典，** 展开为关键字参数传给 ORM
    db.add(cat)                                # 加入待插入队列
    db.commit()                                # 提交事务，写入数据库
    db.refresh(cat)                            # 刷新以获取数据库生成的 catId
    return cat


@router.put("/admin/cats/{cat_id}", response_model=CatResponse)
def update_cat(
    cat_id: int,                              # URL 路径参数 /admin/cats/{cat_id}
    data: CatUpdate,                          # 请求体，全部字段可选
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员接口 — 修改猫咪信息，只更新传入的字段"""
    cat = db.query(Catinformation).filter(Catinformation.catId == cat_id).first()
    if not cat:
        raise HTTPException(status_code=404, detail="猫咪不存在")
    for field, value in data.model_dump(exclude_none=True).items():  # 遍历传入的非 None 字段
        setattr(cat, field, value)             # 等价于 cat.field = value，但 field 是变量名所以用 setattr
    db.commit()
    db.refresh(cat)
    return cat


@router.delete("/admin/cats/{cat_id}")
def delete_cat(
    cat_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    """管理员接口 — 删除猫咪"""
    cat = db.query(Catinformation).filter(Catinformation.catId == cat_id).first()
    if not cat:
        raise HTTPException(status_code=404, detail="猫咪不存在")
    db.delete(cat)   # 标记删除
    db.commit()      # 提交，真正从数据库移除
    return {"message": "猫咪信息已删除"}
