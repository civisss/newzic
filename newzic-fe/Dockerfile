# ── Stage 1: Build Angular ──
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration=production

# ── Stage 2: Serve with Nginx ──
FROM nginx:alpine
COPY --from=build /app/dist/newzic/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx-default.conf
COPY railway-entrypoint.sh /docker-entrypoint.d/40-railway.sh
RUN chmod +x /docker-entrypoint.d/40-railway.sh
EXPOSE 80
