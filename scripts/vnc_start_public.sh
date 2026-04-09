#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
: "${CHROME_URL:=https://xyq.cbg.163.com/cgi-bin/show_login.py?act=show_login&area_id=39&area_name=%E5%8D%8E%E5%8D%97%E5%8C%BA&server_id=625&server_name=%E9%92%93%E9%B1%BC%E5%B2%9B}"
CHROME_URL="$(echo "${CHROME_URL}" | tr -d '\`' | xargs)"
export CHROME_URL
exec "${SCRIPT_DIR}/vnc.sh" start
