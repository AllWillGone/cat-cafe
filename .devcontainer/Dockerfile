FROM python:3.13-slim

# Node.js 22.x (for Vue/Vite web frontend)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    default-mysql-client \
    && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
