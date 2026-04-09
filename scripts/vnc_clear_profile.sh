#!/usr/bin/env bash
set -euo pipefail

PROFILE_DIR="${PROFILE_DIR:-/home/ubuntu/selenium/chrome-profile}"

SUDO=( )
if command -v sudo >/dev/null 2>&1 && sudo -n true >/dev/null 2>&1; then
  SUDO=(sudo -n)
fi

"${SUDO[@]}" pkill -f "/opt/google/chrome/chrome.*--user-data-dir=${PROFILE_DIR}" || true
"${SUDO[@]}" pkill -f "/snap/chromium/.*/chrome.*--user-data-dir=${PROFILE_DIR}" || true
"${SUDO[@]}" pkill -f "chromedriver" || true

"${SUDO[@]}" rm -f "${PROFILE_DIR}/SingletonLock" \
  "${PROFILE_DIR}/SingletonCookie" \
  "${PROFILE_DIR}/SingletonSocket" || true

if [[ -d "${PROFILE_DIR}" ]]; then
  "${SUDO[@]}" chown -R ubuntu:ubuntu "${PROFILE_DIR}" || true
  "${SUDO[@]}" chmod -R u+rwX,go-rwx "${PROFILE_DIR}" || true
fi

echo "profile cleared: ${PROFILE_DIR}"
