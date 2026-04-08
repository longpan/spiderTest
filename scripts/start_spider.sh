#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="/home/ubuntu/java_app"
JAR_FILE="${BASE_DIR}/spider.jar"
LOG_FILE="${BASE_DIR}/spider.log"
PORT="${PORT:-8081}"

RUN_AS=( )
if [[ "$(id -u)" == "0" ]] && command -v sudo >/dev/null 2>&1; then
  RUN_AS=(sudo -u ubuntu -H)
fi

KILL=(kill)
if command -v sudo >/dev/null 2>&1 && sudo -n true >/dev/null 2>&1; then
  KILL=(sudo -n kill)
fi

if [[ ! -f "${JAR_FILE}" ]]; then
  echo "ERROR: jar not found: ${JAR_FILE}" >&2
  exit 1
fi

echo "[start_spider] stopping existing process (if any)..."
PIDS="$(pgrep -f "java -jar ${JAR_FILE}" || true)"
if [[ -n "${PIDS}" ]]; then
  echo "[start_spider] found running pid(s): ${PIDS}"
  for pid in ${PIDS}; do
    "${KILL[@]}" "${pid}" || true
  done

  for _ in $(seq 1 20); do
    if ! pgrep -f "java -jar ${JAR_FILE}" >/dev/null 2>&1; then
      break
    fi
    sleep 0.2
  done

  if pgrep -f "java -jar ${JAR_FILE}" >/dev/null 2>&1; then
    echo "[start_spider] still running after grace period, sending SIGKILL..."
    for pid in $(pgrep -f "java -jar ${JAR_FILE}" || true); do
      "${KILL[@]}" -9 "${pid}" || true
    done
  fi
fi

echo "[start_spider] starting..."
mkdir -p "${BASE_DIR}"

if command -v setsid >/dev/null 2>&1; then
  "${RUN_AS[@]}" setsid -f java -jar "${JAR_FILE}" > "${LOG_FILE}" 2>&1 < /dev/null
else
  "${RUN_AS[@]}" nohup java -jar "${JAR_FILE}" > "${LOG_FILE}" 2>&1 < /dev/null &
fi

sleep 1
NEW_PID="$(pgrep -f "java -jar ${JAR_FILE}" | tail -n 1 || true)"
if [[ -z "${NEW_PID}" ]]; then
  echo "[start_spider] ERROR: process did not start. tail log:" >&2
  tail -n 80 "${LOG_FILE}" | cat >&2
  exit 1
fi

echo "[start_spider] started pid=${NEW_PID} log=${LOG_FILE}"

if command -v ss >/dev/null 2>&1; then
  if ss -lntp 2>/dev/null | grep -q ":${PORT}\\b"; then
    echo "[start_spider] port ${PORT} is listening"
  else
    echo "[start_spider] WARN: port ${PORT} not listening yet"
  fi
elif command -v netstat >/dev/null 2>&1; then
  if netstat -tuln 2>/dev/null | grep -q ":${PORT}\\b"; then
    echo "[start_spider] port ${PORT} is listening"
  else
    echo "[start_spider] WARN: port ${PORT} not listening yet"
  fi
fi

echo "[start_spider] last log lines:"
tail -n 20 "${LOG_FILE}" | cat
