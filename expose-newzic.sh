#!/bin/bash
set -e

# ╔══════════════════════════════════════════════════════════╗
# ║  expose-newzic.sh                                        ║
# ║  Uguale a start.sh ma espone l'app via ngrok             ║
# ╚══════════════════════════════════════════════════════════╝

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ── Dominio ngrok gratuito ──
NGROK_DOMAIN="${NGROK_DOMAIN:-autopilot-regalia-phonebook.ngrok-free.dev}"

# ── Configura CORS per accettare il dominio ngrok ──
export CORS_ORIGINS="http://localhost:4200,https://$NGROK_DOMAIN"

echo ""
echo "🎵 NEWZIC — Local Expose"
echo "========================"
echo ""

# ─────────────────────────────────────────────
# 1. BUILD (identico a start.sh)
# ─────────────────────────────────────────────

echo "🔨 Building backend..."
cd "$SCRIPT_DIR/newzic-be"
./gradlew bootJar -x test --no-daemon

echo "🔨 Building frontend..."
cd "$SCRIPT_DIR/newzic-fe"
npm install
npx ng build --configuration=development

# ─────────────────────────────────────────────
# 2. START CONTAINERS (podman compose in background)
# ─────────────────────────────────────────────

echo "🚀 Starting containers (db + backend + frontend)..."
cd "$SCRIPT_DIR"
podman compose up --build -d

# Attendi che il FE sia pronto
echo -n "⏳ Attendo che i servizi siano pronti..."
WAITED=0
while ! curl -s http://localhost:4200 > /dev/null 2>&1; do
    sleep 2
    WAITED=$((WAITED + 2))
    echo -n "."
    if [ $WAITED -ge 120 ]; then
        echo ""
        echo "❌ Timeout! Controlla i log con: podman compose logs"
        exit 1
    fi
done
echo ""
echo "✅ Servizi pronti!"

# ─────────────────────────────────────────────
# 3. NGROK TUNNEL
# ─────────────────────────────────────────────

echo ""
echo "🌐 Avvio ngrok tunnel..."
echo "   Dominio: https://$NGROK_DOMAIN"
echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  🚀  NEWZIC È ONLINE!                                    ║"
echo "║                                                          ║"
echo "║  🌐  https://$NGROK_DOMAIN"
echo "║  📍  http://localhost:4200 (locale)                      ║"
echo "║                                                          ║"
echo "║  Premi Ctrl+C per spegnere tutto                         ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Cleanup: quando premi Ctrl+C, ferma anche i container
cleanup() {
    echo ""
    echo "⏹  Arresto ngrok e container..."
    podman compose down
    echo "✅ Tutto spento."
}
trap cleanup SIGINT SIGTERM EXIT

# ngrok in foreground (Ctrl+C lo ferma)
ngrok http --url="$NGROK_DOMAIN" 4200
