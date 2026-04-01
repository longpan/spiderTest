#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="${BASE_DIR:-/home/ubuntu/selenium}"
DISPLAY_NUM="${DISPLAY_NUM:-99}"
RFB_PORT="${RFB_PORT:-5901}"
WIDTH="${WIDTH:-1280}"
HEIGHT="${HEIGHT:-720}"
DEPTH="${DEPTH:-24}"

mkdir -p "${BASE_DIR}"
chmod 700 "${BASE_DIR}" || true

if [[ -n "${VNC_PASS:-}" ]]; then
  x11vnc -storepasswd "${VNC_PASS}" "${BASE_DIR}/.vncpass"
fi

if [[ ! -f "${BASE_DIR}/.vncpass" ]]; then
  echo "missing ${BASE_DIR}/.vncpass (set VNC_PASS env to generate it)" >&2
  exit 1
fi

pkill -9 x11vnc || true
pkill -9 fluxbox || true
pkill -9 Xvfb || true

nohup Xvfb ":${DISPLAY_NUM}" -ac -screen 0 "${WIDTH}x${HEIGHT}x${DEPTH}" > "${BASE_DIR}/xvfb.log" 2>&1 &

X_SOCKET="/tmp/.X11-unix/X${DISPLAY_NUM}"
for _ in $(seq 1 50); do
  if [[ -S "${X_SOCKET}" ]]; then
    break
  fi
  sleep 0.1
done

export DISPLAY=":${DISPLAY_NUM}"
nohup fluxbox > "${BASE_DIR}/fluxbox.log" 2>&1 &
nohup x11vnc -display ":${DISPLAY_NUM}" -forever -shared -rfbport "${RFB_PORT}" -rfbauth "${BASE_DIR}/.vncpass" > "${BASE_DIR}/x11vnc.log" 2>&1 &

sleep 1
ss -lntp | grep ":${RFB_PORT}" || true
echo "VNC listening on ${RFB_PORT}"

CHROME_BIN="${CHROME_BIN:-}"
if [[ -z "${CHROME_BIN}" ]]; then
  if command -v google-chrome >/dev/null 2>&1; then
    CHROME_BIN="google-chrome"
  elif command -v google-chrome-stable >/dev/null 2>&1; then
    CHROME_BIN="google-chrome-stable"
  elif command -v chromium-browser >/dev/null 2>&1; then
    CHROME_BIN="chromium-browser"
  elif command -v chromium >/dev/null 2>&1; then
    CHROME_BIN="chromium"
  fi
fi

OPEN_CHROME="${OPEN_CHROME:-true}"
if [[ "${OPEN_CHROME}" == "true" && -n "${CHROME_BIN}" ]]; then
  CHROME_PROFILE_DIR="${CHROME_PROFILE_DIR:-${BASE_DIR}/chrome-profile}"
  mkdir -p "${CHROME_PROFILE_DIR}"
  CHROME_URL="${CHROME_URL:-about:blank}"
  nohup "${CHROME_BIN}" --user-data-dir="${CHROME_PROFILE_DIR}" --no-sandbox "${CHROME_URL}" > "${BASE_DIR}/chrome.log" 2>&1 &
  echo "Chrome started: ${CHROME_BIN} (${CHROME_PROFILE_DIR})"
fi
