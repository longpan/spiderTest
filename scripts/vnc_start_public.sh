#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
: "${CHROME_URL:=https://xyq.cbg.163.com/cgi-bin/show_login.py?act=show_login}"
CHROME_URL="$(echo "${CHROME_URL}" | tr -d '\`' | xargs)"
export CHROME_URL
exec "${SCRIPT_DIR}/vnc.sh" start
