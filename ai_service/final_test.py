#!/usr/bin/env python3
"""
最终测试：验证真实数据训练的AI估值模型
"""
import subprocess
import time
import requests
import json
import sys
import os
import signal

class AITestService:
    def __init__(self):
        self.process = None
        self.service_url = "http://localhost:8000"
        
    def start(self):
        """启动AI服务"""
        print("🚀 启动AI估值服务...")
        try:
            self.process = subprocess.Popen(
                [sys.executable, "mh_ai_service.py"],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                preexec_fn=os.setsid  # 创建新的进程组
            )
            print(f"   进程ID: {self.process.pid}")
            
            # 等待服务启动
            for i in range(10):
                try:
                    requests.get(f"{self.service_url}/health", timeout=1)
                    print(f"   ✅ 服务启动成功!")
                    return True
                except:
                    time.sleep(0.5)
                    if i == 9:
                        print("   ❌ 服务启动超时")
                        return False
                        
            return True
        except Exception as e:
            print(f"   ❌ 启动失败: {e}")
            return False
    
    def stop(self):
        """停止AI服务"""
        if self.process:
            print("🛑 停止AI服务...")
            try:
                # 发送SIGTERM信号给进程组
                os.killpg(os.getpgid(self.process.pid), signal.SIGTERM)
                self.process.wait(timeout=5)
                print("   ✅ 服务已停止")
            except subprocess.TimeoutExpired:
                os.killpg(os.getpgid(self.process.pid), signal.SIGKILL)
                print("   ⚠ 服务强制停止")
            except Exception as e:
                print(f"   ⚠ 停止错误: {e}")
    
    def test_health(self):
        """测试健康检查"""
        try:
            response = requests.get(f"{self.service_url}/health", timeout=3)
            if response.status_code == 200:
                data = response.json()
                print(f"   ✅ 健康检查通过")
                print(f"     状态: {data.get('status')}")
                print(f"     已加载模型: {data.get('loaded_models', [])}")
                print(f"     版本: {data.get('version')}")
                return True
            else:
                print(f"   ❌ 健康检查失败: {response.status_code}")
                return False
        except Exception as e:
            print(f"   ❌ 健康检查错误: {e}")
            return False
    
    def test_pet_valuation(self):
        """测试宠物估值"""
        print("\n📊 测试宠物估值...")
        
        # 从数据库提取的典型真实数据
        test_cases = [
            {
                "name": "典型高价值宝宝",
                "data": {
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
                    "skillScore": 28.5,
                    "qualPercent": 0.75,
                    "growLevelScore": 469.9,
                    "rareSkillCount": 1,
                    "isBaby": 1
                }
            },
            {
                "name": "低等级野生宠物",
                "data": {
                    "type": "pet",
                    "level": 24,
                    "skillNum": 5,
                    "attackQualification": 1200,
                    "defenseQualification": 1100,
                    "physicalQualification": 4000,
                    "manaQualification": 1500,
                    "speedQualification": 1000,
                    "growUp": 1.20,
                    "collect": 0,
                    "skillScore": 15.0,
                    "qualPercent": 0.60,
                    "growLevelScore": 288.0,
                    "rareSkillCount": 0,
                    "isBaby": 0
                }
            }
        ]
        
        all_success = True
        for test_case in test_cases:
            print(f"\n   📋 测试: {test_case['name']}")
            
            try:
                response = requests.post(
                    f"{self.service_url}/predict",
                    json=test_case["data"],
                    timeout=5
                )
                
                if response.status_code == 200:
                    result = response.json()
                    
                    print(f"     状态: ✅ 成功")
                    print(f"     来源: {result.get('source', 'Unknown')}")
                    print(f"     价格: {result.get('estimated_price', 0):.2f} 金币")
                    
                    if result.get("source") == "AI_Model":
                        print(f"     模型: ✅ 使用AI模型")
                        # 检查是否使用了真实数据训练的模型
                        if "real data trained" in result.get("note", "").lower():
                            print(f"     备注: ✅ 基于真实数据训练的模型")
                        else:
                            print(f"     备注: ⚠ 使用旧版模型")
                    else:
                        print(f"     模型: ⚠ 使用降级逻辑")
                        all_success = False
                        
                else:
                    print(f"     状态: ❌ 失败 (HTTP {response.status_code})")
                    print(f"     响应: {response.text[:100]}")
                    all_success = False
                    
            except Exception as e:
                print(f"     状态: ❌ 错误: {e}")
                all_success = False
        
        return all_success
    
    def run_complete_test(self):
        """运行完整测试"""
        print("=" * 60)
        print("🐉 梦幻西游藏宝阁AI估值模型 - 最终测试")
        print("=" * 60)
        
        # 检查模型文件
        print("\n🔍 检查模型文件...")
        required_files = [
            "train_model/mh_pet_model_real.pkl",
            "train_model/scaler_x_pet.pkl", 
            "train_model/scaler_y_pet.pkl",
            "train_model/pet_features_info.json"
        ]
        
        for file in required_files:
            if os.path.exists(file):
                size = os.path.getsize(file)
                print(f"   ✅ {file}: {size:,} bytes")
            else:
                print(f"   ❌ {file}: 文件不存在")
                return False
        
        print(f"   ✅ 所有模型文件存在")
        
        # 启动服务
        if not self.start():
            return False
        
        try:
            # 测试健康检查
            if not self.test_health():
                return False
            
            # 测试宠物估值
            if not self.test_pet_valuation():
                return False
            
            print("\n" + "=" * 60)
            print("🎉 测试完成！AI估值系统工作正常")
            print("=" * 60)
            
            # 显示模型信息
            print("\n📈 模型信息:")
            try:
                with open("train_model/pet_features_info.json", "r", encoding="utf-8") as f:
                    model_info = json.load(f)
                
                print(f"   类型: {model_info.get('type', 'Unknown')}")
                print(f"   训练日期: {model_info.get('training_date', 'Unknown')}")
                print(f"   特征数量: {len(model_info.get('feature_columns', []))}")
                print(f"   训练样本: {model_info.get('training_stats', {}).get('train_samples', 0)}")
                print(f"   测试样本: {model_info.get('training_stats', {}).get('test_samples', 0)}")
                print(f"   平均绝对误差: {model_info.get('training_stats', {}).get('mae', 0):.2f} 金币")
                print(f"   决定系数 (R²): {model_info.get('training_stats', {}).get('r2', 0):.4f}")
                
                print(f"\n🎯 特征重要性前5:")
                features = model_info.get('feature_importance', [])[:5]
                for i, feat in enumerate(features, 1):
                    importance = feat.get('importance', 0) * 100
                    print(f"   {i}. {feat.get('feature', 'Unknown')}: {importance:.1f}%")
                    
            except Exception as e:
                print(f"   模型信息读取失败: {e}")
            
            print("\n🚀 系统已准备好使用！")
            print("\n下一步:")
            print("1. 保持AI服务运行: python mh_ai_service.py")
            print("2. 使用Java端调用: Spring Boot服务会自动连接")
            print("3. 手动测试: python verify_ai_service.py")
            
            return True
            
        finally:
            # 停止服务
            self.stop()

def main():
    """主函数"""
    test_service = AITestService()
    success = test_service.run_complete_test()
    
    if success:
        print("\n✅ 所有测试通过！基于真实数据库数据的AI估值模型已部署完成。")
    else:
        print("\n❌ 测试失败，请检查问题。")

if __name__ == "__main__":
    main()