#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "${DIR}"

echo "[docker/server_down_runtime] Stopping spider-suite containers..."
sudo -E docker compose -f docker-compose.runtime.yml down

echo "[docker/server_down_runtime] Done."