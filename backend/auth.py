"""JWT 认证模块 — token 签发 + get_current_user / get_current_admin 依赖注入"""
import os
from datetime import UTC, datetime, timedelta  # UTC: 时区常量, datetime: 日期时间对象, timedelta: 时间差

from fastapi import Depends, HTTPException, status  # Depends: 依赖注入, HTTPException: 手动抛 HTTP 错误
from fastapi.security import OAuth2PasswordBearer  # 从请求头 Authorization: Bearer xxx 提取 token
from jose import JWTError, jwt  # JWTError: token 无效/过期时抛的异常, jwt: encode/decode token
from sqlalchemy.orm import Session  # 数据库会话类型标注

from config import JWT_SECRET, JWT_ALGORITHM  # 密钥和算法配置
from database import get_db  # 获取数据库会话的依赖函数
from models import User  # 用户表 ORM

# OAuth2PasswordBearer: 定义一个 token 提取器
# tokenUrl="/api/login": 告诉 OpenAPI 文档，token 来源于 /api/login 接口
# 框架会自动从请求头 Authorization: Bearer <token> 中取出 token 字符串
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/login")

# 令牌过期时间: 优先读环境变量，没设就用默认 24 小时
JWT_EXPIRE_HOURS = int(os.getenv("JWT_EXPIRE_HOURS", 24))


def create_access_token(data: dict) -> str:
    """用用户数据生成一个带过期时间的 JWT 令牌"""
    to_encode = data.copy()                                          # 复制一份，避免影响调用方
    expire = datetime.now(UTC) + timedelta(hours=JWT_EXPIRE_HOURS)   # 当前时间 + 过期时长 = 过期时刻
    to_encode.update({"exp": expire})                                # 把过期时间塞入 payload
    return jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALGORITHM)  # 用密钥签名，生成 token 字符串


def get_current_user(
    db: Session = Depends(get_db),       # Depends(get_db): 自动注入一个数据库会话
    token: str = Depends(oauth2_scheme),  # Depends(oauth2_scheme): 自动从请求头提取 token
) -> User:
    """从请求头取出 token，校验后返回当前登录用户"""
    # ── 第 1 步：解码 token ──
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])  # 验签 + 解码，拿到 payload
        user_id: str = payload.get("sub")     # "sub" 是标准字段，存的是用户 ID
        if user_id is None:
            raise HTTPException(status_code=401, detail="认证失败")  # 401 Unauthorized
    except JWTError:                           # token 过期 / 签名伪造 / 格式不对
        raise HTTPException(status_code=401, detail="认证失败")

    # ── 第 2 步：查数据库确认用户存在 ──
    user = db.query(User).filter(User.userId == int(user_id)).first()
    if user is None:
        raise HTTPException(status_code=401, detail="用户不存在")
    return user  # 返回 ORM 对象，后续接口直接用


def get_current_admin(current_user: User = Depends(get_current_user)) -> User:
    """检查当前用户是否为管理员，不是则拒绝访问"""
    # Depends(get_current_user): 先走上面的 get_current_user 逻辑，拿到当前用户
    if current_user.userType != 1:           # userType=1 是管理员
        raise HTTPException(status_code=403, detail="无管理员权限")  # 403 Forbidden
    return current_user
