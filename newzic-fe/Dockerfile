# ── Build stage ──
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration=production

# ── Runtime stage ──
FROM nginx:alpine
COPY --from=build /app/dist/newzic/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx-default.conf

# Use envsubst to replace BACKEND_URL at container start
COPY railway-entrypoint.sh /docker-entrypoint.d/40-railway.sh
RUN chmod +x /docker-entrypoint.d/40-railway.sh
EXPOSE 80
