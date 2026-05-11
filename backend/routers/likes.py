"""点赞模块 — 点赞/取消点赞/我的点赞"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError    # 数据库唯一约束冲突时抛的异常

from database import get_db
from models import Likes, User, Product, Comment, Catinformation
from schemas import LikeCreate, LikeResponse, PaginatedLikes
from auth import get_current_user

router = APIRouter(prefix="/api", tags=["点赞模块"])


def _resolve_object_name(likeType: int, objectId: int, db: Session) -> str:
    """根据 likeType 查对应表获取点赞对象名称，对象已被删则返回'已删除'"""
    try:
        if likeType == 0:                                             # 0 = 商品
            p = db.query(Product).filter(Product.productId == objectId).first()
            return p.productName if p else "已删除"
        elif likeType == 1:                                           # 1 = 评论（取前30字作为名称）
            c = db.query(Comment).filter(Comment.commentId == objectId).first()
            return c.content[:30] if c else "已删除"
        elif likeType == 2:                                           # 2 = 猫咪
            cat = db.query(Catinformation).filter(Catinformation.catId == objectId).first()
            return cat.catName if cat else "已删除"
    except Exception:
        return "已删除"
    return "已删除"


# ── 接口 ──

@router.post("/likes", response_model=LikeResponse)
def create_like(
    data: LikeCreate,                              # { likeType, objectId, linkUrl? }
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """点赞 — 重复点赞返回 409，既有应用层查重也有数据库唯一约束兜底"""
    # ── 应用层查重 ──
    exists = db.query(Likes).filter(
        Likes.userId == current_user.userId,
        Likes.likeType == data.likeType,
        Likes.objectId == data.objectId,
    ).first()
    if exists:
        raise HTTPException(status_code=409, detail="已经点过赞了")

    # 校验点赞对象是否存在
    name = _resolve_object_name(data.likeType, data.objectId, db)
    if name == "已删除":
        raise HTTPException(status_code=404, detail="点赞对象不存在")

    like = Likes(
        likeType=data.likeType,
        objectId=data.objectId,
        userId=current_user.userId,                # 从 token 取，不信任客户端
        linkUrl=data.linkUrl,
    )
    db.add(like)
    try:
        db.commit()
    except IntegrityError:                         # 数据库唯一约束也冲突（并发等极端情况）
        db.rollback()                              # 回滚，清理失败的事务
        raise HTTPException(status_code=409, detail="已经点过赞了")
    db.refresh(like)
    return LikeResponse(
        likeId=like.likeId,
        likeType=like.likeType,
        objectId=like.objectId,
        userId=like.userId,
        linkUrl=like.linkUrl,
        createTime=like.createTime,
        objectName=_resolve_object_name(like.likeType, like.objectId, db),  # 补充对象名称
    )


@router.delete("/likes/{like_id}")
def delete_like(
    like_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """取消点赞 — 只能取消自己的点赞"""
    like = db.query(Likes).filter(Likes.likeId == like_id).first()
    if not like:
        raise HTTPException(status_code=404, detail="点赞不存在")
    if like.userId != current_user.userId:         # 和评论不同，管理员也不能取消别人的赞
        raise HTTPException(status_code=403, detail="无权取消此点赞")
    db.delete(like)
    db.commit()
    return {"message": "已取消点赞"}


@router.get("/likes", response_model=PaginatedLikes)
def list_my_likes(
    likeType: int | None = None,                   # 查询参数，筛选：0商品/1评论/2猫咪
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """查看我的点赞 — 只看当前用户自己的，可按 likeType 筛选"""
    q = db.query(Likes).filter(Likes.userId == current_user.userId)  # 限定当前用户
    if likeType is not None:
        q = q.filter(Likes.likeType == likeType)
    total = q.count()
    likes = q.order_by(Likes.createTime.desc()).offset(skip).limit(limit).all()
    items = []
    for lk in likes:
        items.append(LikeResponse(                  # 手动构造以补充 objectName
            likeId=lk.likeId,
            likeType=lk.likeType,
            objectId=lk.objectId,
            userId=lk.userId,
            linkUrl=lk.linkUrl,
            createTime=lk.createTime,
            objectName=_resolve_object_name(lk.likeType, lk.objectId, db),
        ))
    return PaginatedLikes(total=total, items=items)
