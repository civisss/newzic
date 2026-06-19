#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MISSING=()

# ─────────────────────────────────────────────
# 1. CHECK & INSTALL DEPENDENCIES
# ─────────────────────────────────────────────

echo "🔍 Checking dependencies..."

# ── Homebrew ──
if ! command -v brew &>/dev/null; then
  echo "📦 Installing Homebrew..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  eval "$(/opt/homebrew/bin/brew shellenv)"
fi

# ── Java 21 ──
if ! command -v java &>/dev/null; then
  echo "📦 Installing OpenJDK 21..."
  brew install openjdk@21
  sudo ln -sfn "$(brew --prefix openjdk@21)/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-21.jdk
elif ! java --version 2>&1 | grep -q "21\."; then
  echo "⚠️  Java found but not version 21. Installing OpenJDK 21..."
  brew install openjdk@21
  sudo ln -sfn "$(brew --prefix openjdk@21)/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-21.jdk
fi

# ── Zscaler cert in Java trust store ──
if [ -f "$SCRIPT_DIR/zscaler-root.pem" ]; then
  JAVA_CACERTS="$(/usr/libexec/java_home 2>/dev/null)/lib/security/cacerts" || true
  if [ -f "$JAVA_CACERTS" ]; then
    if ! keytool -list -keystore "$JAVA_CACERTS" -storepass changeit -alias zscaler &>/dev/null; then
      echo "🔐 Adding Zscaler cert to Java trust store..."
      sudo keytool -importcert -noprompt -trustcacerts \
        -alias zscaler -file "$SCRIPT_DIR/zscaler-root.pem" \
        -keystore "$JAVA_CACERTS" -storepass changeit
    fi
  fi
fi

# ── Node.js ──
if ! command -v node &>/dev/null; then
  echo "📦 Installing Node.js..."
  brew install node
fi

# ── Angular CLI (local via npx, but ensure npm is available) ──
if ! command -v npm &>/dev/null; then
  echo "📦 Installing npm..."
  brew install node
fi

# ── Podman ──
if ! command -v podman &>/dev/null; then
  echo "📦 Installing Podman..."
  brew install podman
fi

# ── Podman machine ──
if ! podman machine inspect &>/dev/null; then
  echo "📦 Initializing Podman machine..."
  podman machine init
  podman machine start
elif ! podman machine info 2>&1 | grep -q "Running"; then
  echo "▶️  Starting Podman machine..."
  podman machine start 2>/dev/null || true
fi

# ── Zscaler cert in Podman VM ──
if [ -f "$SCRIPT_DIR/zscaler-root.pem" ]; then
  if ! podman machine ssh -- "test -f /etc/containers/certs.d/docker.io/ca.crt" 2>/dev/null; then
    echo "🔐 Adding Zscaler cert to Podman VM..."
    cat "$SCRIPT_DIR/zscaler-root.pem" | podman machine ssh -- \
      "sudo tee /etc/pki/ca-trust/source/anchors/zscaler-root.pem > /dev/null && \
       sudo update-ca-trust && \
       sudo mkdir -p /etc/containers/certs.d/docker.io && \
       sudo cp /etc/pki/ca-trust/source/anchors/zscaler-root.pem /etc/containers/certs.d/docker.io/ca.crt"
  fi
fi

# ── docker-compose (for podman compose) ──
if ! command -v docker-compose &>/dev/null; then
  echo "📦 Installing docker-compose..."
  brew install docker-compose
fi

echo "✅ All dependencies are ready."
echo ""

# ─────────────────────────────────────────────
# 2. BUILD
# ─────────────────────────────────────────────

echo "🔨 Building backend..."
cd "$SCRIPT_DIR/newzic-be"
./gradlew bootJar -x test --no-daemon

echo "🔨 Building frontend..."
cd "$SCRIPT_DIR/newzic-fe"
npm install
npx ng build --configuration=production

# ─────────────────────────────────────────────
# 3. START
# ─────────────────────────────────────────────

echo "🚀 Starting containers (db + backend + frontend)..."
cd "$SCRIPT_DIR"
podman compose up --build "$@"
