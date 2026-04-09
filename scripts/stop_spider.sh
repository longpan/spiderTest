#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="/home/ubuntu/java_app"
JAR_FILE="${BASE_DIR}/spider.jar"
LOG_FILE="${BASE_DIR}/spider.log"
PORT="${PORT:-8081}"

KILL=(kill)
if command -v sudo >/dev/null 2>&1 && sudo -n true >/dev/null 2>&1; then
  KILL=(sudo -n kill)
fi

echo "[stop_spider] stopping..."

PIDS="$(pgrep -f "java -jar ${JAR_FILE}" || true)"
if [[ -z "${PIDS}" ]]; then
  echo "[stop_spider] no running process found"
else
  echo "[stop_spider] found pid(s): ${PIDS}"
  for pid in ${PIDS}; do
    "${KILL[@]}" "${pid}" || true
  done

  for _ in $(seq 1 30); do
    if ! pgrep -f "java -jar ${JAR_FILE}" >/dev/null 2>&1; then
      break
    fi
    sleep 0.2
  done

  if pgrep -f "java -jar ${JAR_FILE}" >/dev/null 2>&1; then
    echo "[stop_spider] still running after grace period, sending SIGKILL..."
    for pid in $(pgrep -f "java -jar ${JAR_FILE}" || true); do
      "${KILL[@]}" -9 "${pid}" || true
    done
  fi
fi

if command -v ss >/dev/null 2>&1; then
  if ss -lntp 2>/dev/null | grep -q ":${PORT}\\b"; then
    echo "[stop_spider] WARN: port ${PORT} is still listening"
  else
    echo "[stop_spider] port ${PORT} is not listening"
  fi
elif command -v netstat >/dev/null 2>&1; then
  if netstat -tuln 2>/dev/null | grep -q ":${PORT}\\b"; then
    echo "[stop_spider] WARN: port ${PORT} is still listening"
  else
    echo "[stop_spider] port ${PORT} is not listening"
  fi
fi

if [[ -f "${LOG_FILE}" ]]; then
  echo "[stop_spider] last log lines:"
  tail -n 20 "${LOG_FILE}" | cat
fi
