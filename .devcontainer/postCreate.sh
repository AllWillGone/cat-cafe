#!/bin/bash
set -e

echo "==> Installing Python dependencies..."
pip install -r backend/requirements.txt

echo "==> Installing Web frontend dependencies..."
cd web/cat-cafe-ui && npm install && cd /workspace

echo "==> Setting up backend .env..."
if [ ! -f backend/.env ]; then
    cp backend/.env.example backend/.env
    echo ".env created from .env.example"
else
    echo ".env already exists, checking DB_HOST..."
fi
# 无论新建还是已有，确保指向容器内的 mysql 服务
sed -i 's/^DB_HOST=.*/DB_HOST=mysql/' backend/.env
echo "DB_HOST set to mysql"

echo "==> Dev container ready!"
echo "    Backend:  cd backend && uvicorn main:app --reload --host 0.0.0.0"
echo "    Web:      cd web/cat-cafe-ui && npm run dev"
