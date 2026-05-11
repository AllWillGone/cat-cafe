"""评论模块 — 发表评论/查看评论/审核评论/删除评论"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Comment, User, Product, Catinformation
from schemas import CommentCreate, CommentAudit, CommentResponse, PaginatedComments
from auth import get_current_user, get_current_admin

router = APIRouter(prefix="/api", tags=["评论模块"])


# ── 辅助函数（不暴露为接口） ──

def _resolve_target(targetType: int, targetId: int, db: Session) -> str | None:
    """根据 targetType 查对应表确认目标是否存在，返回目标名称（用于后续扩展，当前仅校验用）"""
    if targetType == 0:                                     # 0 = 商品
        p = db.query(Product).filter(Product.productId == targetId).first()
        return p.productName if p else None
    elif targetType == 1:                                   # 1 = 猫咪
        c = db.query(Catinformation).filter(Catinformation.catId == targetId).first()
        return c.catName if c else None
    return None                                             # 无效的 targetType


def _make_comment_response(c: Comment) -> CommentResponse:
    """将 ORM 对象转为响应模型，补齐 userName（通过 relationship 链式获取）"""
    return CommentResponse(
        commentId=c.commentId,
        targetType=c.targetType,
        targetId=c.targetId,
        userId=c.userId,
        userName=c.user.userName if c.user else None,       # relationship: comment.user → User 对象 → userName
        content=c.content,
        publishTime=c.publishTime,
        auditStatus=c.auditStatus,
    )


# ── 接口 ──

@router.post("/comments", response_model=CommentResponse)
def create_comment(
    data: CommentCreate,                         # { targetType, targetId, content }
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),  # 需登录
):
    """发表评论 — 需登录，评论对象必须存在"""
    target_name = _resolve_target(data.targetType, data.targetId, db)  # 校验目标存在
    if target_name is None:
        raise HTTPException(status_code=404, detail="评论对象不存在")
    comment = Comment(
        targetType=data.targetType,
        targetId=data.targetId,
        userId=current_user.userId,              # 从 token 中取，不信任客户端传参
        content=data.content,
        auditStatus=0,                           # 默认待审核
    )
    db.add(comment)
    db.commit()
    db.refresh(comment)
    return _make_comment_response(comment)       # 手动构造响应以包含 userName


@router.get("/comments", response_model=PaginatedComments)
def list_comments(
    targetType: int | None = None,               # 查询参数，筛选：0=商品 1=猫咪
    targetId: int | None = None,                 # 查询参数，和目标类型配合使用
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
):
    """查看评论 — 公开，只展示审核通过的（auditStatus=1），可按 targetType+targetId 联合筛选"""
    q = db.query(Comment).filter(Comment.auditStatus == 1)  # 只展示已通过审核的
    if targetType is not None and targetId is not None:     # 两个筛选条件同时传入才生效
        q = q.filter(Comment.targetType == targetType, Comment.targetId == targetId)
    total = q.count()
    comments = q.order_by(Comment.publishTime.desc()).offset(skip).limit(limit).all()  # 最新评论在前
    return PaginatedComments(
        total=total,
        items=[_make_comment_response(c) for c in comments],
    )


@router.put("/admin/comments/{comment_id}/audit", response_model=CommentResponse)
def audit_comment(
    comment_id: int,
    data: CommentAudit,                          # { auditStatus: 1(通过) 或 2(拒绝) }
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),    # 管理员守卫
):
    """管理员审核评论 — 通过(status=1)或拒绝(status=2)"""
    comment = db.query(Comment).filter(Comment.commentId == comment_id).first()
    if not comment:
        raise HTTPException(status_code=404, detail="评论不存在")
    comment.auditStatus = data.auditStatus       # 覆盖为审核结果
    db.commit()
    db.refresh(comment)
    return _make_comment_response(comment)


@router.delete("/comments/{comment_id}")
def delete_comment(
    comment_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),  # 需登录，但不限管理员
):
    """删除评论 — 评论作者或管理员均可删除"""
    comment = db.query(Comment).filter(Comment.commentId == comment_id).first()
    if not comment:
        raise HTTPException(status_code=404, detail="评论不存在")
    # 双重权限：作者本人（userId 匹配）或 管理员（userType=1）
    if comment.userId != current_user.userId and current_user.userType != 1:
        raise HTTPException(status_code=403, detail="无权删除此评论")
    db.delete(comment)
    db.commit()
    return {"message": "评论已删除"}
