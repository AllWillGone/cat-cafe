"""Cleanup helpers for soft-referenced comments and likes."""
from sqlalchemy.orm import Session

from models import Comment, Likes


def delete_likes_for_objects(db: Session, like_type: int, object_ids: list[int]) -> None:
    """Delete likes that point at soft-referenced objects."""
    if not object_ids:
        return
    db.query(Likes).filter(
        Likes.likeType == like_type,
        Likes.objectId.in_(object_ids),
    ).delete(synchronize_session=False)


def delete_likes_for_object(db: Session, like_type: int, object_id: int) -> None:
    """Delete likes for one soft-referenced object."""
    db.query(Likes).filter(
        Likes.likeType == like_type,
        Likes.objectId == object_id,
    ).delete(synchronize_session=False)


def delete_comments_for_target(db: Session, target_type: int, target_id: int) -> None:
    """Delete comments under a target and first remove likes on those comments."""
    comments = db.query(Comment.commentId).filter(
        Comment.targetType == target_type,
        Comment.targetId == target_id,
    ).all()
    comment_ids = [comment_id for (comment_id,) in comments]
    delete_likes_for_objects(db, 1, comment_ids)
    db.query(Comment).filter(
        Comment.targetType == target_type,
        Comment.targetId == target_id,
    ).delete(synchronize_session=False)


def delete_comment_likes_for_user(db: Session, user_id: int) -> None:
    """Delete likes that point at comments written by one user."""
    comments = db.query(Comment.commentId).filter(Comment.userId == user_id).all()
    comment_ids = [comment_id for (comment_id,) in comments]
    delete_likes_for_objects(db, 1, comment_ids)
