#!/bin/bash
set -e

# ╔══════════════════════════════════════════════════════════╗
# ║  expose-newzic.sh                                        ║
# ║  Uguale a start.sh ma espone l'app via Cloudflare Tunnel ║
# ╚══════════════════════════════════════════════════════════╝

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ── Tunnel tool: cloudflared (Cloudflare Tunnel, no bandwidth limits) ──
if ! command -v cloudflared &> /dev/null; then
    echo "❌ cloudflared non trovato. Installalo con: brew install cloudflared"
    exit 1
fi

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
# 2. CLOUDFLARE TUNNEL (prima dei container per ottenere l'URL)
# ─────────────────────────────────────────────

echo ""
echo "🌐 Avvio Cloudflare Tunnel..."
echo ""

# Cleanup: quando premi Ctrl+C, ferma tunnel e container
CF_PID=""
cleanup() {
    echo ""
    echo "⏹  Arresto tunnel e container..."
    [ -n "$CF_PID" ] && kill "$CF_PID" 2>/dev/null
    cd "$SCRIPT_DIR" && podman compose down
    echo "✅ Tutto spento."
}
trap cleanup SIGINT SIGTERM EXIT

# Avvia cloudflared in background e cattura l'URL
CF_LOG=$(mktemp)
cloudflared tunnel --url http://localhost:4200 2>"$CF_LOG" &
CF_PID=$!

# Attendi che cloudflared generi l'URL pubblico
echo -n "⏳ Attendo URL del tunnel..."
TUNNEL_URL=""
for i in $(seq 1 30); do
    TUNNEL_URL=$(grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' "$CF_LOG" 2>/dev/null | head -1)
    if [ -n "$TUNNEL_URL" ]; then
        break
    fi
    sleep 1
    echo -n "."
done
echo ""

if [ -z "$TUNNEL_URL" ]; then
    echo "❌ Timeout in attesa dell'URL del tunnel. Log:"
    cat "$CF_LOG"
    exit 1
fi

echo "✅ Tunnel pronto: $TUNNEL_URL"

# ─────────────────────────────────────────────
# 3. START CONTAINERS con CORS aggiornato
# ─────────────────────────────────────────────

# Setta CORS prima di avviare i container così il backend lo riceve
export CORS_ORIGINS="http://localhost:4200,$TUNNEL_URL"

echo ""
echo "🚀 Starting containers (db + backend + frontend)..."
echo "   CORS_ORIGINS=$CORS_ORIGINS"
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

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  🚀  NEWZIC È ONLINE!                                    ║"
echo "║                                                          ║"
echo "║  🌐  $TUNNEL_URL"
echo "║  📍  http://localhost:4200 (locale)                      ║"
echo "║                                                          ║"
echo "║  ☁️  Cloudflare Tunnel — nessun limite di banda          ║"
echo "║  Premi Ctrl+C per spegnere tutto                         ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Resta in attesa (il tunnel gira in background)
wait $CF_PID
