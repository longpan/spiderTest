#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="${PROJECT_DIR:-/home/ubuntu/docker/spider-suite}"
PIP_INDEX_URL="${PIP_INDEX_URL:-https://mirrors.cloud.tencent.com/pypi/simple}"

cd "${PROJECT_DIR}"

export PIP_INDEX_URL

sudo -E docker compose -f docker-compose.runtime.yml build --no-cache
