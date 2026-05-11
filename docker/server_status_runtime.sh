#!/bin/bash
cd "$(dirname "$0")/.."

echo "========== 1. Docker 容器状态 =========="
sudo -E docker compose -f docker-compose.runtime.yml ps

echo ""
echo "========== 2. 容器内部进程状态 (Supervisor) =========="
if sudo -E docker compose -f docker-compose.runtime.yml ps --status running | grep -q spider-suite; then
    sudo -E docker compose -f docker-compose.runtime.yml exec spider-suite supervisorctl status
else
    echo "[!] 容器当前未运行或未就绪，无法获取内部进程状态。"
fi

echo ""
echo "========== 3. 宿主机端口映射情况 =========="
if sudo -E docker compose -f docker-compose.runtime.yml ps --status running | grep -q spider-suite; then
    echo -n "Spider (容器 8081) 映射到宿主机: "
    sudo -E docker compose -f docker-compose.runtime.yml port spider-suite 8081 || echo "未映射"
    echo -n "AI Server (容器 8000) 映射到宿主机: "
    sudo -E docker compose -f docker-compose.runtime.yml port spider-suite 8000 || echo "未映射"
    echo -n "VNC (容器 5901) 映射到宿主机: "
    sudo -E docker compose -f docker-compose.runtime.yml port spider-suite 5901 || echo "未映射"
else
    echo "[!] 容器未运行，端口未映射。"
fi

echo ""
echo "========== 4. 容器最新日志 (Spider & AI & 桌面环境) =========="
sudo -E docker compose -f docker-compose.runtime.yml logs --tail 30

echo ""
echo "[提示] 如果想实时跟踪最新日志，请运行命令:"
echo "sudo docker compose -f docker-compose.runtime.yml logs -f"
echo "=========================================================="
