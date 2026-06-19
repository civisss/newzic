# ── Runtime-only image (for local use with pre-built dist) ──
FROM nginx:alpine
COPY dist/newzic/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx-default.conf
COPY railway-entrypoint.sh /docker-entrypoint.d/40-railway.sh
RUN chmod +x /docker-entrypoint.d/40-railway.sh
EXPOSE 80
