#!/bin/bash
# 远程服务器AI服务部署和训练脚本
# 服务器: 150.158.130.52
# 用户: ubuntu
# 目标目录: /home/ubuntu/ai_service

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}AI服务远程部署和训练脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 配置变量
REMOTE_USER="ubuntu"
REMOTE_HOST="150.158.130.52"
REMOTE_DIR="/home/ubuntu/ai_service"
LOCAL_DIR="/Users/onglchen/projects/ai/Qcoder/javaProjects/spiderTest/ai_service"

# 步骤1: 上传文件到远程服务器
echo -e "${YELLOW}[步骤 1/6] 上传文件到远程服务器...${NC}"
echo "创建远程目录..."
ssh ${REMOTE_USER}@${REMOTE_HOST} "mkdir -p ${REMOTE_DIR}/train_data ${REMOTE_DIR}/train_model"

echo "上传Python脚本..."
scp ${LOCAL_DIR}/extract_train_data.py ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/
scp ${LOCAL_DIR}/train_real_models.py ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/
scp ${LOCAL_DIR}/mh_ai_service.py ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/
scp ${LOCAL_DIR}/requirements.txt ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/

echo -e "${GREEN}✓ 文件上传完成${NC}"
echo ""

# 步骤2: 安装Python依赖
echo -e "${YELLOW}[步骤 2/6] 安装Python依赖...${NC}"
ssh ${REMOTE_USER}@${REMOTE_HOST} << 'EOF'
cd /home/ubuntu/ai_service
echo "检查Python版本..."
python3 --version

# 创建虚拟环境
echo "创建Python虚拟环境..."
python3 -m venv venv
source venv/bin/activate

echo "升级pip..."
pip install --upgrade pip

echo "安装pip依赖..."
pip install -r requirements.txt

echo "安装mysql-connector-python..."
pip install mysql-connector-python

echo "安装matplotlib（用于生成图表）..."
pip install matplotlib

echo "虚拟环境配置完成"
EOF

echo -e "${GREEN}✓ 依赖安装完成${NC}"
echo ""

# 步骤3: 从数据库提取训练数据
echo -e "${YELLOW}[步骤 3/6] 从数据库提取训练数据...${NC}"
ssh ${REMOTE_USER}@${REMOTE_HOST} << 'EOF'
cd /home/ubuntu/ai_service
echo "开始提取数据..."
./venv/bin/python extract_train_data.py

echo ""
echo "检查生成的文件..."
ls -lh train_data/
EOF

echo -e "${GREEN}✓ 数据提取完成${NC}"
echo ""

# 步骤4: 训练AI模型
echo -e "${YELLOW}[步骤 4/6] 训练AI估值模型...${NC}"
ssh ${REMOTE_USER}@${REMOTE_HOST} << 'EOF'
cd /home/ubuntu/ai_service
echo "开始训练模型..."
./venv/bin/python train_real_models.py

echo ""
echo "检查生成的模型文件..."
ls -lh train_model/
EOF

echo -e "${GREEN}✓ 模型训练完成${NC}"
echo ""

# 步骤5: 启动AI预测服务（后台运行）
echo -e "${YELLOW}[步骤 5/6] 启动AI预测服务...${NC}"
ssh ${REMOTE_USER}@${REMOTE_HOST} << 'EOF'
cd /home/ubuntu/ai_service

# 检查是否已有服务在运行
if pgrep -f "mh_ai_service.py" > /dev/null; then
    echo "检测到已运行的AI服务，正在停止..."
    pkill -f "mh_ai_service.py"
    sleep 2
fi

echo "启动AI服务（后台运行）..."
nohup ./venv/bin/python mh_ai_service.py > ai_service.log 2>&1 &

echo "等待服务启动..."
sleep 3

# 检查服务是否启动成功
if pgrep -f "mh_ai_service.py" > /dev/null; then
    echo "✓ AI服务已成功启动"
    echo "服务PID: $(pgrep -f 'mh_ai_service.py')"
    echo "日志文件: /home/ubuntu/ai_service/ai_service.log"
else
    echo "✗ AI服务启动失败，请检查日志"
    exit 1
fi
EOF

echo -e "${GREEN}✓ AI服务启动完成${NC}"
echo ""

# 步骤6: 测试服务
echo -e "${YELLOW}[步骤 6/6] 测试AI预测服务...${NC}"
echo "等待服务完全启动..."
sleep 2

# 健康检查
echo "执行健康检查..."
HEALTH_RESPONSE=$(curl -s http://${REMOTE_HOST}:8000/health)
echo "健康检查响应: ${HEALTH_RESPONSE}"
echo ""

# 测试预测
echo "执行预测测试..."
TEST_RESPONSE=$(curl -s -X POST http://${REMOTE_HOST}:8000/predict \
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
  }')

echo "预测响应: ${TEST_RESPONSE}"
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "服务信息:"
echo "  - 服务地址: http://${REMOTE_HOST}:8000"
echo "  - 健康检查: http://${REMOTE_HOST}:8000/health"
echo "  - 预测接口: http://${REMOTE_HOST}:8000/predict (POST)"
echo "  - 日志文件: ${REMOTE_DIR}/ai_service.log"
echo ""
echo "管理命令:"
echo "  - 查看日志: ssh ${REMOTE_USER}@${REMOTE_HOST} 'tail -f ${REMOTE_DIR}/ai_service.log'"
echo "  - 停止服务: ssh ${REMOTE_USER}@${REMOTE_HOST} 'pkill -f mh_ai_service.py'"
echo "  - 重启服务: ssh ${REMOTE_USER}@${REMOTE_HOST} 'cd ${REMOTE_DIR} && nohup python3 mh_ai_service.py > ai_service.log 2>&1 &'"
echo ""
echo "API使用示例:"
echo '  curl -X POST http://${REMOTE_HOST}:8000/predict \'
echo '    -H "Content-Type: application/json" \'
echo '    -d '\''{"type":"pet","level":100,"collect":50,"skillNum":8,...}'\''
echo ""
