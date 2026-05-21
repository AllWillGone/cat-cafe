from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from routers.user import router as user_router
from routers.cat import router as cat_router
from routers.product import router as product_router
from routers.comment import router as comment_router
from routers.likes import router as likes_router
from routers.order import router as order_router

app = FastAPI(title="Cat Cafe Backend")

public_dir = Path(__file__).resolve().parent.parent / "web" / "cat-cafe-ui" / "public"
for asset_name in ("cats", "products", "avatars"):
    asset_dir = public_dir / asset_name
    if asset_dir.is_dir():
        app.mount(f"/{asset_name}", StaticFiles(directory=str(asset_dir)), name=asset_name)

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
    return {"status": "ok", "message": "backend is running"}
