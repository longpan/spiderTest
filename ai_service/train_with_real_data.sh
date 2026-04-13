#!/bin/bash

# 使用真实数据库数据训练AI估值模型 - 快速启动脚本
# 作者: onglchen
# 版本: 1.0

set -e  # 遇到错误时退出

echo "========================================"
echo "梦幻西游藏宝阁AI估值模型训练工具"
echo "========================================"
echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# 检查Python环境
echo "1. 检查Python环境..."
python3 --version
pip3 --version

# 检查依赖包
echo -e "\n2. 检查Python依赖包..."
REQUIRED_PACKAGES=("mysql-connector-python" "pandas" "scikit-learn" "joblib")
for pkg in "${REQUIRED_PACKAGES[@]}"; do
    if python3 -c "import $pkg" 2>/dev/null; then
        echo "  ✓ $pkg 已安装"
    else
        echo "  ✗ $pkg 未安装，正在安装..."
        pip3 install "$pkg"
    fi
done

# 切换到脚本所在目录
cd "$(dirname "$0")"
echo -e "\n3. 当前工作目录: $(pwd)"

# 提取训练数据
echo -e "\n4. 从数据库提取训练数据..."
if [ -f "extract_train_data.py" ]; then
    python3 extract_train_data.py
    EXTRACT_STATUS=$?
    
    if [ $EXTRACT_STATUS -eq 0 ]; then
        echo "  ✓ 数据提取完成"
        
        # 检查提取的数据
        if [ -f "train_data/real_pet_train_data.csv" ]; then
            DATA_COUNT=$(wc -l < "train_data/real_pet_train_data.csv" | awk '{print $1-1}')
            echo "  ✓ 提取到 $DATA_COUNT 条宠物数据"
        else
            echo "  ✗ 未生成训练数据文件"
            exit 1
        fi
    else
        echo "  ✗ 数据提取失败"
        exit 1
    fi
else
    echo "  ✗ 找不到 extract_train_data.py"
    exit 1
fi

# 训练模型
echo -e "\n5. 训练AI估值模型..."
if [ -f "train_real_models.py" ]; then
    python3 train_real_models.py
    TRAIN_STATUS=$?
    
    if [ $TRAIN_STATUS -eq 0 ]; then
        echo "  ✓ 模型训练完成"
        
        # 检查训练的模型
        if [ -f "train_model/mh_pet_model_real.pkl" ]; then
            echo "  ✓ 宠物模型已生成: train_model/mh_pet_model_real.pkl"
        fi
        
        if [ -f "train_model/pet_features_info.json" ]; then
            echo "  ✓ 特征信息已保存: train_model/pet_features_info.json"
        fi
    else
        echo "  ✗ 模型训练失败"
        exit 1
    fi
else
    echo "  ✗ 找不到 train_real_models.py"
    exit 1
fi

# 验证模型
echo -e "\n6. 验证模型可用性..."
if [ -f "verify_ai_service.py" ]; then
    # 先启动AI服务（后台）
    if [ -f "mh_ai_service.py" ]; then
        echo "  启动AI服务进行验证..."
        python3 mh_ai_service.py &
        AI_PID=$!
        echo "  AI服务进程ID: $AI_PID"
        
        # 等待服务启动
        sleep 3
        
        # 运行验证脚本
        python3 verify_ai_service.py
        VERIFY_STATUS=$?
        
        # 停止AI服务
        kill $AI_PID 2>/dev/null || true
        
        if [ $VERIFY_STATUS -eq 0 ]; then
            echo "  ✓ 模型验证通过"
        else
            echo "  ✗ 模型验证失败"
        fi
    else
        echo "  ! 跳过验证，找不到 mh_ai_service.py"
    fi
else
    echo "  ! 跳过验证，找不到 verify_ai_service.py"
fi

# 生成报告
echo -e "\n7. 生成训练报告..."
REPORT_FILE="training_report_$(date '+%Y%m%d_%H%M%S').txt"
{
    echo "梦幻西游藏宝阁AI估值模型训练报告"
    echo "========================================"
    echo "生成时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    
    echo "1. 数据源信息"
    echo "   数据库: mysql://150.158.130.52:3306/spider"
    echo "   数据表: mh_pet_item"
    
    if [ -f "train_data/real_pet_train_data.csv" ]; then
        DATA_COUNT=$(wc -l < "train_data/real_pet_train_data.csv" | awk '{print $1-1}')
        echo "   数据量: $DATA_COUNT 条记录"
    fi
    
    echo ""
    echo "2. 模型文件"
    echo "   宠物模型: train_model/mh_pet_model_real.pkl"
    echo "   特征标准化器: train_model/scaler_x_pet.pkl"
    echo "   目标标准化器: train_model/scaler_y_pet.pkl"
    
    echo ""
    echo "3. 特征信息"
    if [ -f "train_model/pet_features_info.json" ]; then
        echo "   特征文件: train_model/pet_features_info.json"
        echo "   特征数量: $(grep -o '"feature_columns"' train_model/pet_features_info.json | wc -l) 个"
    fi
    
    echo ""
    echo "4. 评估图表"
    if [ -d "train_model/plots" ]; then
        PLOT_COUNT=$(ls -1 train_model/plots/*.png 2>/dev/null | wc -l)
        echo "   图表数量: $PLOT_COUNT 个"
    fi
    
    echo ""
    echo "5. 使用说明"
    echo "   启动AI服务: python mh_ai_service.py"
    echo "   测试模型: python verify_ai_service.py"
    echo "   重新训练: 运行本脚本或执行 train_real_models.py"
    
    echo ""
    echo "6. 注意事项"
    echo "   - 确保数据库连接正常"
    echo "   - 定期更新训练数据"
    echo "   - 监控模型性能变化"
    
} > "$REPORT_FILE"

echo "  ✓ 训练报告已生成: $REPORT_FILE"

echo -e "\n========================================"
echo "训练流程完成!"
echo "完成时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"
echo ""
echo "下一步:"
echo "1. 启动AI服务: python mh_ai_service.py"
echo "2. 测试服务: python verify_ai_service.py"
echo "3. 查看详细文档: cat README_REAL_DATA_TRAINING.md"
echo ""
echo "感谢使用!"