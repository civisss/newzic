#!/usr/bin/env bash
set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo -e "${CYAN}═══════════════════════════════════════${NC}"
echo -e "${CYAN}  NEWZIC — Running all tests${NC}"
echo -e "${CYAN}═══════════════════════════════════════${NC}"
echo ""

# ── Backend tests ──
echo -e "${CYAN}▶ Backend (Kotlin / JUnit 5)${NC}"
cd "$ROOT/newzic-be"
if ./gradlew test --no-daemon --quiet; then
  echo -e "${GREEN}✔ Backend tests passed${NC}"
  BE_OK=true
else
  echo -e "${RED}✘ Backend tests failed${NC}"
  BE_OK=false
fi
echo ""

# ── Frontend tests ──
echo -e "${CYAN}▶ Frontend (Angular / Karma + Jasmine)${NC}"
cd "$ROOT/newzic-fe"
if npx ng test --watch=false --browsers=ChromeHeadless; then
  echo -e "${GREEN}✔ Frontend tests passed${NC}"
  FE_OK=true
else
  echo -e "${RED}✘ Frontend tests failed${NC}"
  FE_OK=false
fi
echo ""

# ── Summary ──
echo -e "${CYAN}═══════════════════════════════════════${NC}"
if $BE_OK && $FE_OK; then
  echo -e "${GREEN}  ✔ All tests passed${NC}"
  exit 0
else
  [ "$BE_OK" = false ] && echo -e "${RED}  ✘ Backend: FAILED${NC}"
  [ "$FE_OK" = false ] && echo -e "${RED}  ✘ Frontend: FAILED${NC}"
  exit 1
fi
