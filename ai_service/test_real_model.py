#!/usr/bin/env python3
"""
测试真实数据训练的AI估值模型
"""
import subprocess
import time
import requests
import json
import sys
import os

def start_ai_service():
    """启动AI服务"""
    print("=== 启动AI估值服务 ===")
    try:
        # 启动AI服务的子进程
        process = subprocess.Popen(
            [sys.executable, "mh_ai_service.py"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        print(f"AI服务进程启动，PID: {process.pid}")
        
        # 等待服务启动
        time.sleep(3)
        return process
    except Exception as e:
        print(f"启动AI服务失败: {e}")
        return None

def test_pet_valuation():
    """测试宠物估值"""
    print("\n=== 测试宠物估值 ===")
    
    # 创建一个真实的宠物数据样本（基于数据库中的典型数据）
    pet_data = {
        "type": "pet",
        "level": 37,
        "skillNum": 6,
        "attackQualification": 1480,
        "defenseQualification": 1400,
        "physicalQualification": 4500,
        "manaQualification": 2000,
        "speedQualification": 1200,
        "growUp": 1.27,
        "collect": 5,
        "skillScore": 28.5,  # Java端计算的衍生特征
        "qualPercent": 0.75,
        "growLevelScore": 469.9,
        "rareSkillCount": 1,
        "isBaby": 1
    }
    
    print(f"测试数据:")
    for key, value in pet_data.items():
        print(f"  {key}: {value}")
    
    try:
        response = requests.post("http://localhost:8000/predict", json=pet_data, timeout=5)
        if response.status_code == 200:
            result = response.json()
            print(f"\n估值结果:")
            print(json.dumps(result, indent=2, ensure_ascii=False))
            
            # 检查是否使用了AI模型
            if result.get("source") == "AI_Model":
                print(f"✓ 成功使用AI模型进行估值")
                print(f"✓ 估值价格: {result.get('estimated_price', 0):.2f}")
                return True
            else:
                print(f"⚠ 使用了降级逻辑: {result.get('source', 'Unknown')}")
                print(f"⚠ 未使用AI模型，可能模型加载有问题")
                return False
        else:
            print(f"请求失败: {response.status_code}")
            print(f"响应: {response.text}")
            return False
    except Exception as e:
        print(f"请求错误: {e}")
        return False

def main():
    """主函数"""
    print("=== 梦幻西游藏宝阁AI估值模型测试 ===")
    print(f"Python路径: {sys.executable}")
    print(f"工作目录: {os.getcwd()}")
    
    # 检查模型文件
    print("\n=== 检查模型文件 ===")
    required_files = [
        "train_model/mh_pet_model_real.pkl",
        "train_model/scaler_x_pet.pkl", 
        "train_model/scaler_y_pet.pkl",
        "train_model/pet_features_info.json"
    ]
    
    all_files_exist = True
    for file in required_files:
        if os.path.exists(file):
            size = os.path.getsize(file)
            print(f"✓ {file}: {size:,} bytes")
        else:
            print(f"✗ {file}: 文件不存在")
            all_files_exist = False
    
    if not all_files_exist:
        print("\n❌ 模型文件不完整，请先运行训练脚本")
        return
    
    # 启动并测试服务
    process = start_ai_service()
    if not process:
        return
    
    try:
        # 测试宠物估值
        success = test_pet_valuation()
        
        if success:
            print("\n✅ 测试成功！AI估值模型工作正常")
            print("\n=== 模型信息 ===")
            print("模型类型: 基于真实数据库数据训练的RandomForest模型")
            print("训练数据: 5,907条真实宠物交易数据")
            print("特征数量: 14个（9个基础特征 + 5个衍生特征）")
            print("模型性能: MAE=499.68, R²=0.4688")
            print("特征重要性排名: 1.skillNum 2.collect 3.speedQualification")
        else:
            print("\n❌ 测试失败，请检查模型和服务")
            
    finally:
        # 停止服务
        print("\n=== 停止AI服务 ===")
        process.terminate()
        process.wait()
        print("AI服务已停止")

if __name__ == "__main__":
    main()