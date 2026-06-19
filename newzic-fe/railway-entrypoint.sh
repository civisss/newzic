#!/bin/sh
# Generates nginx config at runtime, replacing BACKEND_URL with the actual backend service URL.
# On Railway: BACKEND_URL is set via service variables (e.g. http://newzic-be.railway.internal:8080)
# Locally:    defaults to http://backend:8080 (docker-compose service name)

BACKEND=${BACKEND_URL:-http://backend:8080}
PORT=${PORT:-80}

cat > /etc/nginx/conf.d/default.conf <<EOF
server {
    listen ${PORT};
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        client_max_body_size 50M;
        proxy_pass ${BACKEND}/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

echo "Nginx configured: backend=${BACKEND}, port=${PORT}"
