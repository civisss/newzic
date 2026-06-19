#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "🚀 Building & starting all services via Docker Compose..."
echo "   (multi-stage Dockerfiles handle the build inside containers)"
echo ""

cd "$SCRIPT_DIR"
podman compose up --build "$@"
