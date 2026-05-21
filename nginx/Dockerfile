FROM node:22-alpine AS builder
WORKDIR /src
COPY web/cat-cafe-ui/package.json web/cat-cafe-ui/package-lock.json ./
RUN npm install
COPY web/cat-cafe-ui/ .
RUN npm run build

FROM nginx:alpine
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /src/dist /usr/share/nginx/html
