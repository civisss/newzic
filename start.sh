#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "🔨 Building backend..."
cd "$SCRIPT_DIR/newzic-be"
./gradlew bootJar -x test --no-daemon

echo "🔨 Building frontend..."
cd "$SCRIPT_DIR/newzic-fe"
npm install
npx ng build --configuration=production

echo "🚀 Starting containers (db + backend + frontend)..."
cd "$SCRIPT_DIR"
podman compose up --build "$@"
