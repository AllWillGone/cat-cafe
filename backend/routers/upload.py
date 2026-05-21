import uuid
import os
from pathlib import Path

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form

from auth import get_current_admin

router = APIRouter(prefix="/api/admin", tags=["文件上传"])

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"}
MAX_SIZE = 5 * 1024 * 1024  # 5 MB
TYPE_DIR = {"cat": "cats", "product": "products", "avatar": "avatars"}

UPLOAD_BASE = Path(
    os.environ.get(
        "UPLOAD_DIR",
        str(Path(__file__).resolve().parent.parent.parent / "web" / "cat-cafe-ui" / "public"),
    )
)


@router.post("/upload")
def upload_file(
    file: UploadFile = File(...),
    type: str = Form(...),
    admin=Depends(get_current_admin),
):
    if type not in TYPE_DIR:
        raise HTTPException(status_code=400, detail="type 必须是 cat、product 或 avatar")

    ext = Path(file.filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=400, detail=f"不支持的文件类型: {ext}")

    name = f"{uuid.uuid4().hex}{ext}"
    target_dir = UPLOAD_BASE / TYPE_DIR[type]
    target_dir.mkdir(parents=True, exist_ok=True)

    filepath = target_dir / name
    with open(filepath, "wb") as f:
        f.write(file.file.read())

    url = f"/{TYPE_DIR[type]}/{name}"
    return {"url": url, "message": "上传成功"}
