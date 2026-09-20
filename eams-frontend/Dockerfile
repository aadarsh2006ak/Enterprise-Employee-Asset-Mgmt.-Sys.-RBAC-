# ==============================================================================
# Multi-Stage Production Dockerfile for EAMS Frontend (React + Vite + Tailwind)
# ==============================================================================

# Stage 1: Build Application Artifacts
FROM node:20-alpine AS builder
WORKDIR /app

# Copy dependency manifests
COPY package.json package-lock.json ./

# Install dependencies (frozen lockfile for deterministic build)
RUN npm ci

# Copy source code and build configs
COPY . .

# Run production build (Vite bundle & TypeScript compilation)
RUN npm run build

# Stage 2: Minimalist Nginx Web Server Runtime
FROM nginx:1.25-alpine AS runner
WORKDIR /usr/share/nginx/html

# Remove default Nginx website
RUN rm -rf ./*

# Copy built frontend assets from builder stage
COPY --from=builder /app/dist .

# Copy custom Nginx configuration with reverse proxy & SPA fallback
COPY nginx.conf /etc/nginx/nginx.conf

# Expose HTTP port
EXPOSE 80

# Run Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
