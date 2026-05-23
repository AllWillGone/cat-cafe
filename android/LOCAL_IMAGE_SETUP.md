# Android 图片与上传速查

## 图片地址

Debug 本地后端配置通常为：

```text
API_BASE_URL=http://127.0.0.1:8000/
IMAGE_BASE_URL=http://127.0.0.1:8000
```

模拟器需要：

```cmd
adb reverse tcp:8000 tcp:8000
```

后端托管静态目录：

- `/cats` -> `web/cat-cafe-ui/public/cats`
- `/products` -> `web/cat-cafe-ui/public/products`
- `/avatars` -> `web/cat-cafe-ui/public/avatars`

数据库中保存相对路径：

- `photoUrl=/cats/mimi.jpg`
- `imageUrl=/products/cola.jpg`
- `userAvatar=/avatars/admin.jpg`

Android 使用 `AppConfig.buildImageUrl(path)` 拼接完整地址。

## 上传接口

- 普通用户头像：`POST /api/upload`，`type=avatar`
- 管理员商品/猫咪图片：`POST /api/admin/upload`
  - 商品：`type=product`，回填 `imageUrl`
  - 猫咪：`type=cat`，回填 `photoUrl`

上传成功后后端返回：

```json
{"url": "/products/xxx.jpg", "message": "上传成功"}
```

