#!/usr/bin/env bash
set -euo pipefail

pkill -9 x11vnc || true
pkill -9 fluxbox || true
pkill -9 Xvfb || true

echo "VNC stopped"
