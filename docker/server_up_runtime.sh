#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="${PROJECT_DIR:-/home/ubuntu/docker/spider-suite}"

HOST_SPIDER_PORT="${HOST_SPIDER_PORT:-8081}"
HOST_AI_PORT="${HOST_AI_PORT:-8000}"
HOST_VNC_PORT="${HOST_VNC_PORT:-5901}"

if [[ -z "${VNC_PASS:-}" ]]; then
  echo "missing VNC_PASS env" >&2
  exit 1
fi

cd "${PROJECT_DIR}"

export HOST_SPIDER_PORT HOST_AI_PORT HOST_VNC_PORT VNC_PASS

sudo -E docker compose -f docker-compose.runtime.yml up -d
sudo docker ps | cat
