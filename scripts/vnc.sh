#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="${BASE_DIR:-/home/ubuntu/selenium}"
DISPLAY_NUM="${DISPLAY_NUM:-99}"
RFB_PORT="${RFB_PORT:-5901}"
WIDTH="${WIDTH:-1280}"
HEIGHT="${HEIGHT:-720}"
DEPTH="${DEPTH:-24}"

CHROME_PROFILE_DIR="${CHROME_PROFILE_DIR:-${BASE_DIR}/chrome-profile}"
OPEN_CHROME="${OPEN_CHROME:-true}"
CHROME_URL="${CHROME_URL:-about:blank}"
VNC_PASS="${VNC_PASS:-}"

mkdir -p "${BASE_DIR}"
chmod 700 "${BASE_DIR}" || true

SUDO=( )
if command -v sudo >/dev/null 2>&1 && sudo -n true >/dev/null 2>&1; then
  SUDO=(sudo -n)
fi

RUN_AS_UBUNTU=( )
if [[ "$(id -u)" == "0" ]] && command -v sudo >/dev/null 2>&1; then
  RUN_AS_UBUNTU=(sudo -u ubuntu -H)
fi

spawn_detached() {
  local log_file="$1"
  shift
  if command -v setsid >/dev/null 2>&1; then
    setsid -f "$@" > "${log_file}" 2>&1 </dev/null
  else
    nohup "$@" > "${log_file}" 2>&1 </dev/null &
  fi
}

ensure_vncpass() {
  if [[ -n "${VNC_PASS}" ]]; then
    "${RUN_AS_UBUNTU[@]}" x11vnc -storepasswd "${VNC_PASS}" "${BASE_DIR}/.vncpass"
  fi
  if [[ ! -f "${BASE_DIR}/.vncpass" ]]; then
    echo "missing ${BASE_DIR}/.vncpass (set VNC_PASS env to generate it)" >&2
    exit 1
  fi
}

kill_vnc_stack() {
  "${SUDO[@]}" pkill -9 x11vnc || true
  "${SUDO[@]}" pkill -9 fluxbox || true
  "${SUDO[@]}" pkill -9 Xvfb || true
}

clear_display_lock() {
  "${SUDO[@]}" rm -f "/tmp/.X${DISPLAY_NUM}-lock" "/tmp/.X11-unix/X${DISPLAY_NUM}" || true
}

detect_chrome_bin() {
  if [[ -n "${CHROME_BIN:-}" ]]; then
    echo "${CHROME_BIN}"
    return 0
  fi
  if command -v google-chrome >/dev/null 2>&1; then
    echo "google-chrome"
    return 0
  fi
  if command -v google-chrome-stable >/dev/null 2>&1; then
    echo "google-chrome-stable"
    return 0
  fi
  if command -v chromium-browser >/dev/null 2>&1; then
    echo "chromium-browser"
    return 0
  fi
  if command -v chromium >/dev/null 2>&1; then
    echo "chromium"
    return 0
  fi
  return 1
}

free_profile() {
  "${SUDO[@]}" pkill -f "/opt/google/chrome/chrome.*--user-data-dir=${CHROME_PROFILE_DIR}" || true
  "${SUDO[@]}" pkill -f "/snap/chromium/.*/chrome.*--user-data-dir=${CHROME_PROFILE_DIR}" || true
  "${SUDO[@]}" pkill -f "chromedriver" || true
  "${SUDO[@]}" rm -f "${CHROME_PROFILE_DIR}/SingletonLock" "${CHROME_PROFILE_DIR}/SingletonCookie" "${CHROME_PROFILE_DIR}/SingletonSocket" || true
}

fix_profile_ownership() {
  if [[ -d "${CHROME_PROFILE_DIR}" ]]; then
    "${SUDO[@]}" chown -R ubuntu:ubuntu "${CHROME_PROFILE_DIR}" || true
    "${SUDO[@]}" chmod -R u+rwX,go-rwx "${CHROME_PROFILE_DIR}" || true
  fi
}

start_vnc() {
  ensure_vncpass
  kill_vnc_stack
  clear_display_lock

  mkdir -p "${CHROME_PROFILE_DIR}"
  chmod 700 "${CHROME_PROFILE_DIR}" || true
  fix_profile_ownership

  if [[ "$(id -u)" == "0" ]] && [[ -z "${RUN_AS_UBUNTU[*]}" ]]; then
    echo "ERROR: running as root but cannot switch to ubuntu" >&2
    exit 1
  fi

  if [[ "$(id -u)" == "0" ]]; then
    exec "${RUN_AS_UBUNTU[@]}" BASE_DIR="${BASE_DIR}" DISPLAY_NUM="${DISPLAY_NUM}" RFB_PORT="${RFB_PORT}" WIDTH="${WIDTH}" HEIGHT="${HEIGHT}" DEPTH="${DEPTH}" CHROME_PROFILE_DIR="${CHROME_PROFILE_DIR}" OPEN_CHROME="${OPEN_CHROME}" CHROME_URL="${CHROME_URL}" VNC_PASS="${VNC_PASS}" "$0" start
  fi

  spawn_detached "${BASE_DIR}/xvfb.log" Xvfb ":${DISPLAY_NUM}" -ac -screen 0 "${WIDTH}x${HEIGHT}x${DEPTH}"

  X_SOCKET="/tmp/.X11-unix/X${DISPLAY_NUM}"
  for _ in $(seq 1 50); do
    if [[ -S "${X_SOCKET}" ]]; then
      break
    fi
    sleep 0.1
  done
  if [[ ! -S "${X_SOCKET}" ]]; then
    echo "ERROR: Xvfb did not create X socket ${X_SOCKET}" >&2
    tail -n 80 "${BASE_DIR}/xvfb.log" 2>/dev/null | cat >&2 || true
    exit 1
  fi

  export DISPLAY=":${DISPLAY_NUM}"
  spawn_detached "${BASE_DIR}/fluxbox.log" fluxbox
  spawn_detached "${BASE_DIR}/x11vnc.log" x11vnc -display ":${DISPLAY_NUM}" -forever -shared -rfbport "${RFB_PORT}" -rfbauth "${BASE_DIR}/.vncpass"

  sleep 1
  ss -lntp | grep ":${RFB_PORT}" || true
  echo "VNC listening on ${RFB_PORT}"

  if [[ "${OPEN_CHROME}" == "true" ]]; then
    if CHROME_BIN_RESOLVED="$(detect_chrome_bin)"; then
      free_profile
      spawn_detached "${BASE_DIR}/chrome.log" "${CHROME_BIN_RESOLVED}" --user-data-dir="${CHROME_PROFILE_DIR}" --no-sandbox "${CHROME_URL}"
      echo "Chrome started: ${CHROME_BIN_RESOLVED} (${CHROME_PROFILE_DIR})"
    fi
  fi
}

stop_vnc() {
  kill_vnc_stack
  echo "VNC stopped"
}

status() {
  ss -lntp | egrep ":${RFB_PORT}\\b" || true
  ps -ef | egrep "Xvfb :${DISPLAY_NUM}|x11vnc|fluxbox|chrome.*${CHROME_PROFILE_DIR}|chromedriver" | egrep -v egrep || true
}

cmd="${1:-}"
case "${cmd}" in
  start)
    start_vnc
    ;;
  stop)
    stop_vnc
    ;;
  restart)
    stop_vnc
    start_vnc
    ;;
  status)
    status
    ;;
  free-profile)
    free_profile
    ;;
  *)
    echo "usage: $0 {start|stop|restart|status|free-profile}" >&2
    exit 2
    ;;
esac
