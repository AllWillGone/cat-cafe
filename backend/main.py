from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routers.user import router as user_router
from routers.cat import router as cat_router
from routers.product import router as product_router
from routers.comment import router as comment_router
from routers.likes import router as likes_router
from routers.order import router as order_router

app = FastAPI(title="猫咖点单系统")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(user_router)
app.include_router(cat_router)
app.include_router(product_router)
app.include_router(comment_router)
app.include_router(likes_router)
app.include_router(order_router)


@app.get("/health")
def health():
    return {"status": "ok", "message": "猫咖服务运行中"}
