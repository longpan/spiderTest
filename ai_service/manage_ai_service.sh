#!/bin/bash
# AI服务管理脚本
# 用法: ./manage_ai_service.sh [start|stop|restart|status|logs|test|train|retrain]

SERVICE_DIR="/home/ubuntu/ai_service"
PYTHON="${SERVICE_DIR}/venv/bin/python"
SERVICE_SCRIPT="mh_ai_service.py"
LOG_FILE="${SERVICE_DIR}/ai_service.log"
PID_FILE="${SERVICE_DIR}/ai_service.pid"

case "$1" in
    start)
        echo "启动AI预测服务..."
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)
            if ps -p $PID > /dev/null 2>&1; then
                echo "服务已在运行 (PID: $PID)"
                exit 0
            fi
        fi
        
        cd $SERVICE_DIR
        nohup $PYTHON $SERVICE_SCRIPT > $LOG_FILE 2>&1 &
        echo $! > $PID_FILE
        sleep 2
        
        if ps -p $(cat $PID_FILE) > /dev/null 2>&1; then
            echo "✓ 服务启动成功 (PID: $(cat $PID_FILE))"
            echo "  访问地址: http://150.158.130.52:8000"
            echo "  健康检查: http://150.158.130.52:8000/health"
        else
            echo "✗ 服务启动失败，请查看日志: $LOG_FILE"
            exit 1
        fi
        ;;
        
    stop)
        echo "停止AI预测服务..."
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)
            if ps -p $PID > /dev/null 2>&1; then
                kill $PID
                echo "✓ 服务已停止 (PID: $PID)"
                rm -f $PID_FILE
            else
                echo "服务未运行"
                rm -f $PID_FILE
            fi
        else
            # 尝试通过进程名停止
            pkill -f "$SERVICE_SCRIPT"
            echo "✓ 服务已停止"
        fi
        ;;
        
    restart)
        $0 stop
        sleep 2
        $0 start
        ;;
        
    status)
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)
            if ps -p $PID > /dev/null 2>&1; then
                echo "✓ 服务运行中 (PID: $PID)"
                echo "  访问地址: http://150.158.130.52:8000"
                netstat -tlnp 2>/dev/null | grep 8000 || ss -tlnp | grep 8000
            else
                echo "✗ 服务未运行 (PID文件存在但进程不存在)"
                rm -f $PID_FILE
            fi
        else
            if pgrep -f "$SERVICE_SCRIPT" > /dev/null; then
                PID=$(pgrep -f "$SERVICE_SCRIPT")
                echo "✓ 服务运行中 (PID: $PID)"
                echo "  访问地址: http://150.158.130.52:8000"
            else
                echo "✗ 服务未运行"
            fi
        fi
        ;;
        
    logs)
        if [ -f "$LOG_FILE" ]; then
            echo "=== 最新日志 (最后50行) ==="
            tail -50 $LOG_FILE
        else
            echo "日志文件不存在: $LOG_FILE"
        fi
        ;;
        
    test)
        echo "=== 健康检查 ==="
        curl -s http://localhost:8000/health | python3 -m json.tool
        
        echo ""
        echo "=== 预测测试 ==="
        curl -s -X POST http://localhost:8000/predict \
          -H "Content-Type: application/json" \
          -d '{
            "type": "pet",
            "level": 100,
            "collect": 50,
            "skillNum": 8,
            "attackQualification": 1500,
            "defenseQualification": 1300,
            "physicalQualification": 6000,
            "manaQualification": 2600,
            "speedQualification": 1350,
            "growUp": 1.3,
            "skillScore": 45.5,
            "qualPercent": 0.85,
            "growLevelScore": 1300,
            "rareSkillCount": 2,
            "isBaby": 1
          }' | python3 -m json.tool
        ;;
        
    train)
        echo "=========================================="
        echo "模型训练（仅训练，不重启服务）"
        echo "=========================================="
        echo ""
        
        cd $SERVICE_DIR
        
        # 步骤1：提取最新数据
        echo "[步骤 1/2] 从数据库提取最新训练数据..."
        $PYTHON extract_train_data.py
        
        if [ $? -ne 0 ]; then
            echo "✗ 数据提取失败！"
            exit 1
        fi
        
        echo ""
        
        # 步骤2：训练模型
        echo "[步骤 2/2] 训练AI估值模型..."
        $PYTHON train_real_models.py
        
        if [ $? -ne 0 ]; then
            echo "✗ 模型训练失败！"
            exit 1
        fi
        
        echo ""
        echo "=========================================="
        echo "✓ 模型训练完成！"
        echo "=========================================="
        echo ""
        echo "注意：新模型尚未加载到服务中。"
        echo "如需使用新模型，请执行: $0 restart"
        echo ""
        ;;
        
    retrain)
        echo "=========================================="
        echo "模型重新训练（训练并重启服务）"
        echo "=========================================="
        echo ""
        
        cd $SERVICE_DIR
        
        # 步骤1：提取最新数据
        echo "[步骤 1/3] 从数据库提取最新训练数据..."
        $PYTHON extract_train_data.py
        
        if [ $? -ne 0 ]; then
            echo "✗ 数据提取失败！"
            exit 1
        fi
        
        echo ""
        
        # 步骤2：训练模型
        echo "[步骤 2/3] 训练AI估值模型..."
        $PYTHON train_real_models.py
        
        if [ $? -ne 0 ]; then
            echo "✗ 模型训练失败！"
            exit 1
        fi
        
        echo ""
        
        # 步骤3：重启服务
        echo "[步骤 3/3] 重启AI服务以加载新模型..."
        $0 restart
        
        if [ $? -ne 0 ]; then
            echo "✗ 服务重启失败！"
            exit 1
        fi
        
        echo ""
        echo "=========================================="
        echo "✓ 模型重新训练完成！"
        echo "=========================================="
        echo ""
        echo "新模型已加载到服务中。"
        echo "可以使用以下命令测试:"
        echo "  $0 test"
        echo ""
        ;;
        
    *)
        echo "用法: $0 {start|stop|restart|status|logs|test|train|retrain}"
        echo ""
        echo "命令说明:"
        echo "  start    - 启动AI预测服务"
        echo "  stop     - 停止AI预测服务"
        echo "  restart  - 重启AI预测服务"
        echo "  status   - 查看服务状态"
        echo "  logs     - 查看服务日志"
        echo "  test     - 测试服务功能"
        echo "  train    - 训练新模型（不重启服务）"
        echo "  retrain  - 重新训练模型并重启服务"
        exit 1
        ;;
esac

exit 0
