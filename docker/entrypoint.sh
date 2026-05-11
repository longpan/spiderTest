#!/usr/bin/env bash
set -euo pipefail

mkdir -p /home/ubuntu/selenium /data/webmagic/webmagic-selenium
chmod 700 /home/ubuntu/selenium || true

if [[ -n "${VNC_PASS:-}" ]]; then
  x11vnc -storepasswd "${VNC_PASS}" /home/ubuntu/selenium/.vncpass
fi

if [[ ! -f /home/ubuntu/selenium/.vncpass ]]; then
  echo "missing /home/ubuntu/selenium/.vncpass; set VNC_PASS env to create it" >&2
  exit 1
fi

cat > "${SELENIUM_CONFIG}" <<EOF
driver=chrome
user_data_dir=${CHROME_PROFILE_DIR}
profile_directory=Default
EOF

mkdir -p /home/ubuntu/selenium
ln -sf "${SELENIUM_CONFIG}" /home/ubuntu/selenium/config.ini

if [[ ! -x /home/ubuntu/selenium/vnc/vnc_start_public.sh ]] || [[ ! -x /home/ubuntu/selenium/vnc/vnc.sh ]]; then
  echo "missing /home/ubuntu/selenium/vnc scripts. mount host /home/ubuntu/selenium/vnc to container /home/ubuntu/selenium/vnc" >&2
  exit 1
fi

exec /usr/bin/supervisord -n -c /etc/supervisor/conf.d/supervisord.conf
