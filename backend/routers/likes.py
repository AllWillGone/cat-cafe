"""点赞模块 — 点赞/取消点赞/我的点赞"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError

from database import get_db
from models import Likes, User, Product, Comment, Catinformation
from schemas import LikeCreate, LikeResponse, PaginatedLikes
from auth import get_current_user

router = APIRouter(prefix="/api", tags=["点赞模块"])


def _resolve_object_name(likeType: int, objectId: int, db: Session) -> str:
    """根据 likeType 查对应表获取点赞对象名称"""
    try:
        if likeType == 0:
            p = db.query(Product).filter(Product.productId == objectId).first()
            return p.productName if p else "已删除"
        elif likeType == 1:
            c = db.query(Comment).filter(Comment.commentId == objectId).first()
            return c.content[:30] if c else "已删除"
        elif likeType == 2:
            cat = db.query(Catinformation).filter(Catinformation.catId == objectId).first()
            return cat.catName if cat else "已删除"
    except Exception:
        return "已删除"
    return "已删除"


@router.post("/likes", response_model=LikeResponse)
def create_like(
    data: LikeCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """点赞 — 重复点赞返回 409"""
    exists = db.query(Likes).filter(
        Likes.userId == current_user.userId,
        Likes.likeType == data.likeType,
        Likes.objectId == data.objectId,
    ).first()
    if exists:
        raise HTTPException(status_code=409, detail="已经点过赞了")

    name = _resolve_object_name(data.likeType, data.objectId, db)
    if name == "已删除":
        raise HTTPException(status_code=404, detail="点赞对象不存在")

    like = Likes(
        likeType=data.likeType,
        objectId=data.objectId,
        userId=current_user.userId,
        linkUrl=data.linkUrl,
    )
    db.add(like)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=409, detail="已经点过赞了")
    db.refresh(like)
    target_type = None
    target_id = None
    if like.likeType == 1:
        comment = db.query(Comment).filter(Comment.commentId == like.objectId).first()
        if comment:
            target_type = comment.targetType
            target_id = comment.targetId
    return LikeResponse(
        likeId=like.likeId,
        likeType=like.likeType,
        objectId=like.objectId,
        userId=like.userId,
        linkUrl=like.linkUrl,
        createTime=like.createTime,
        objectName=_resolve_object_name(like.likeType, like.objectId, db),
        targetType=target_type,
        targetId=target_id,
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
    if like.userId != current_user.userId:
        raise HTTPException(status_code=403, detail="无权取消此点赞")
    db.delete(like)
    db.commit()
    return {"message": "已取消点赞"}


@router.get("/likes", response_model=PaginatedLikes)
def list_my_likes(
    likeType: int | None = None,
    keyword: str | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """查看我的点赞 — 可按 likeType 筛选 + 关键字搜索对象名"""
    q = db.query(Likes).filter(Likes.userId == current_user.userId)
    if likeType is not None:
        q = q.filter(Likes.likeType == likeType)
    likes = q.order_by(Likes.createTime.desc()).all()
    # 构造完整列表（带 objectName），支持关键字过滤
    all_items = []
    for lk in likes:
        name = _resolve_object_name(lk.likeType, lk.objectId, db)
        if keyword and keyword.lower() not in name.lower():
            continue
        target_type = None
        target_id = None
        if lk.likeType == 1:
            comment = db.query(Comment).filter(Comment.commentId == lk.objectId).first()
            if comment:
                target_type = comment.targetType
                target_id = comment.targetId
        all_items.append(LikeResponse(
            likeId=lk.likeId,
            likeType=lk.likeType,
            objectId=lk.objectId,
            userId=lk.userId,
            linkUrl=lk.linkUrl,
            createTime=lk.createTime,
            objectName=name,
            targetType=target_type,
            targetId=target_id,
        ))
    total = len(all_items)
    paged = all_items[skip:skip + limit]
    return PaginatedLikes(total=total, items=paged)
