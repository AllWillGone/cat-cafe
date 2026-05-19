"""用户模块 — 注册/登录/个人信息管理/管理员用户管理"""
import random
import time
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from passlib.context import CryptContext

from database import get_db
from models import User
from schemas import (
    UserLogin, UserRegister, LoginResponse,
    UserDetailResponse, UserUpdate, UserPasswordChange,
    UserResetPassword, UserResetPasswordByPhone, SendSmsCodeRequest,
    AdminUserListItem, AdminUserUpdate, PaginatedUsers,
)
from auth import create_access_token, get_current_user, get_current_admin

router = APIRouter(prefix="/api", tags=["用户模块"])
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# ── 短信验证码存储（演示用内存存储，生产环境应使用 Redis）──
_sms_codes: dict[str, dict] = {}  # { phone: { "code": "123456", "expires": 1234567890 } }
_CODE_EXPIRE_SECONDS = 300  # 5 分钟有效


def _clean_expired_codes():
    """清理过期的验证码"""
    now = time.time()
    expired = [p for p, v in _sms_codes.items() if v["expires"] < now]
    for p in expired:
        del _sms_codes[p]

#注册
@router.post("/register", response_model=LoginResponse)
def register(data: UserRegister, db: Session = Depends(get_db)):
    exists = db.query(User).filter(User.userName == data.userName).first()
    if exists:
        raise HTTPException(status_code=422, detail="用户名已存在")

    if data.userPhone:
        phone_exists = db.query(User).filter(User.userPhone == data.userPhone).first()
        if phone_exists:
            raise HTTPException(status_code=422, detail="该手机号已注册")

    hashed = pwd_context.hash(data.userPassword)
    user = User(userPassword=hashed, userName=data.userName, userType=0, userPhone=data.userPhone)
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token({"sub": str(user.userId), "userType": user.userType})
    return LoginResponse(
        userId=user.userId,
        userName=user.userName,
        userType=user.userType,
        registerTime=user.registerTime,
        token=token,
    )

#登陆
@router.post("/login", response_model=LoginResponse)
def login(data: UserLogin, db: Session = Depends(get_db)):
    user = None
    if data.userName:
        user = db.query(User).filter(User.userName == data.userName).first()
    elif data.userPhone:
        user = db.query(User).filter(User.userPhone == data.userPhone).first()
    else:
        raise HTTPException(status_code=422, detail="请提供用户名或手机号")

    if not user:
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    if not pwd_context.verify(data.userPassword, user.userPassword):
        raise HTTPException(status_code=401, detail="用户名或密码错误")

    token = create_access_token({"sub": str(user.userId), "userType": user.userType})
    return LoginResponse(
        userId=user.userId,
        userName=user.userName,
        userType=user.userType,
        registerTime=user.registerTime,
        token=token,
    )



#查看自己信息
@router.get("/user/me", response_model=UserDetailResponse)
def get_me(current_user: User = Depends(get_current_user)):
    return current_user

#修改自己信息
@router.put("/user/me", response_model=UserDetailResponse)
def update_me(
    data: UserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if data.userName is not None and data.userName != current_user.userName:
        dup = db.query(User).filter(
            User.userName == data.userName,
            User.userId != current_user.userId,
        ).first()
        if dup:
            raise HTTPException(status_code=422, detail="用户名已存在")
        current_user.userName = data.userName
    if data.gender is not None:
        current_user.gender = data.gender
    if data.birthday is not None:
        current_user.birthday = data.birthday
    if data.userPhone is not None and data.userPhone != current_user.userPhone:
        dup = db.query(User).filter(
            User.userPhone == data.userPhone,
            User.userId != current_user.userId,
        ).first()
        if dup:
            raise HTTPException(status_code=422, detail="该手机号已被使用")
        current_user.userPhone = data.userPhone
    if data.userAvatar is not None:
        current_user.userAvatar = data.userAvatar
    db.commit()
    db.refresh(current_user)
    return current_user

#修改密码  后期要修改
@router.put("/user/me/password")
def change_password(
    data: UserPasswordChange,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if not pwd_context.verify(data.oldPassword, current_user.userPassword):
        raise HTTPException(status_code=400, detail="原密码错误")
    if data.oldPassword == data.newPassword:
        raise HTTPException(status_code=400, detail="新密码不能与旧密码相同")
    current_user.userPassword = pwd_context.hash(data.newPassword)
    db.commit()
    return {"message": "密码修改成功"}

#忘记密码
@router.post("/reset-password")
def reset_password(data: UserResetPassword, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.userName == data.userName).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    user.userPassword = pwd_context.hash(data.newPassword)
    db.commit()
    return {"message": "密码重置成功"}


#发送短信验证码
@router.post("/send-sms-code")
def send_sms_code(data: SendSmsCodeRequest):
    """发送短信验证码 — 演示模式：直接返回验证码，生产环境应通过短信通道发送"""
    _clean_expired_codes()

    code = f"{random.randint(100000, 999999)}"
    _sms_codes[data.userPhone] = {
        "code": code,
        "expires": time.time() + _CODE_EXPIRE_SECONDS,
    }
    return {"message": "验证码已发送", "code": code}  # 演示返回 code，生产去掉


#忘记密码 — 手机号 + 验证码
@router.post("/reset-password-by-phone")
def reset_password_by_phone(data: UserResetPasswordByPhone, db: Session = Depends(get_db)):
    _clean_expired_codes()

    # 校验验证码
    stored = _sms_codes.get(data.userPhone)
    if not stored:
        raise HTTPException(status_code=400, detail="请先获取验证码")
    if stored["code"] != data.code:
        raise HTTPException(status_code=400, detail="验证码错误")
    if stored["expires"] < time.time():
        del _sms_codes[data.userPhone]
        raise HTTPException(status_code=400, detail="验证码已过期，请重新获取")

    user = db.query(User).filter(User.userPhone == data.userPhone).first()
    if not user:
        raise HTTPException(status_code=404, detail="未找到该手机号对应的用户")
    user.userPassword = pwd_context.hash(data.newPassword)
    db.commit()

    # 用完后清理
    del _sms_codes[data.userPhone]
    return {"message": "密码重置成功"}

# 注销账号
@router.delete("/user/me")
def delete_me(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    db.delete(current_user)
    db.commit()
    return {"message": "账户已注销"}


# ── 管理员 ──
#查看用户列表
@router.get("/admin/users", response_model=PaginatedUsers)
def admin_list_users(
    keyword: str | None = None,
    userType: int | None = None,
    sortBy: str = "userId",
    sortOrder: str = "desc",
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    q = db.query(User)
    if keyword is not None:
        like = f"%{keyword}%"
        q = q.filter(
            User.userName.like(like) | User.userPhone.like(like)
        )
    if userType is not None:
        q = q.filter(User.userType == userType)
    total = q.count()
    sort_col = {
        "userId": User.userId,
        "userName": User.userName,
        "registerTime": User.registerTime,
    }.get(sortBy, User.userId)
    if sortOrder == "asc":
        q = q.order_by(sort_col.asc())
    else:
        q = q.order_by(sort_col.desc())
    users = q.offset(skip).limit(limit).all()
    return PaginatedUsers(
        total=total,
        items=[AdminUserListItem.model_validate(u) for u in users],
    )

#管理员编辑用户
@router.put("/admin/users/{user_id}", response_model=AdminUserListItem)
def admin_update_user(
    user_id: int,
    data: AdminUserUpdate,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    target = db.query(User).filter(User.userId == user_id).first()
    if not target:
        raise HTTPException(status_code=404, detail="用户不存在")
    if data.userName is not None and data.userName != target.userName:
        dup = db.query(User).filter(
            User.userName == data.userName,
            User.userId != user_id,
        ).first()
        if dup:
            raise HTTPException(status_code=422, detail="用户名已存在")
        target.userName = data.userName
    if data.gender is not None:
        target.gender = data.gender
    if data.birthday is not None:
        target.birthday = data.birthday
    if data.userPhone is not None and data.userPhone != target.userPhone:
        dup = db.query(User).filter(
            User.userPhone == data.userPhone,
            User.userId != user_id,
        ).first()
        if dup:
            raise HTTPException(status_code=422, detail="该手机号已被使用")
        target.userPhone = data.userPhone
    if data.userAvatar is not None:
        target.userAvatar = data.userAvatar
    db.commit()
    db.refresh(target)
    return target

#删除用户
@router.delete("/admin/users/{user_id}")
def admin_delete_user(
    user_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    if user_id == admin.userId:
        raise HTTPException(status_code=400, detail="不能删除自己的账号，请使用注销功能")
    target = db.query(User).filter(User.userId == user_id).first()
    if not target:
        raise HTTPException(status_code=404, detail="用户不存在")
    db.delete(target)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=400, detail="无法删除该用户：存在关联的订单记录")
    return {"message": "用户已删除"}
