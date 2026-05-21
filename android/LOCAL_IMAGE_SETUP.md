# Local Image Setup

Current debug image flow:

- `IMAGE_BASE_URL = http://127.0.0.1:8000`
- Android emulator uses `adb reverse tcp:8000 tcp:8000`
- Backend mounts:
  - `/cats` -> `web/cat-cafe-ui/public/cats`
  - `/products` -> `web/cat-cafe-ui/public/products`
  - `/avatars` -> `web/cat-cafe-ui/public/avatars`

Result:

- `photoUrl = /cats/mimi.jpg`
- `imageUrl = /products/cola.jpg`

Run the backend, then the app. No separate Vite image server is required.
