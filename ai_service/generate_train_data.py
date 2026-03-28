import pandas as pd
import numpy as np
import os

def generate_pet_data(n=10000):
    """生成宠物模拟数据"""
    data = {
        'level': np.random.randint(69, 176, n),
        'skillNum': np.random.randint(4, 15, n),
        'attackQualification': np.random.randint(1100, 1680, n),
        'defenseQualification': np.random.randint(1000, 1600, n),
        'physicalQualification': np.random.randint(3000, 6500, n),
        'manaQualification': np.random.randint(1500, 3200, n),
        'speedQualification': np.random.randint(1000, 1600, n),
        'growUp': np.round(np.random.uniform(1.15, 1.3, n), 3),
        'collect': np.random.randint(0, 800, n)
    }
    
    # 模拟一个相对合理的价格公式
    # 基础价 = 资质加成 + 技能加成 + 等级加成 + 收藏热度
    base_price = (data['attackQualification'] * 0.5 + 
                  data['growUp'] * 2000 + 
                  (data['skillNum'] ** 2) * 100 + 
                  data['level'] * 20 + 
                  data['collect'] * 5)
    
    # 加入随机波动
    data['price'] = np.round(base_price * np.random.uniform(0.85, 1.15, n), 2)
    
    df = pd.DataFrame(data)
    os.makedirs('train_data', exist_ok=True)
    df.to_csv('train_data/pet_train_data.csv', index=False)
    print(f"Generated {n} pet data items.")

def generate_equip_data(n=10000):
    """生成装备模拟数据"""
    data = {
        'level': np.random.choice([60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160], n),
        'accessoryAttributeNum': np.random.randint(0, 4, n),
        'primeValue': np.random.randint(100, 450, n),
        'collect': np.random.randint(0, 500, n)
    }
    
    # 价格逻辑：等级越高价格基数越大，主属性和附属性显著影响价格
    base_price = (data['level'] * 50 + 
                  data['primeValue'] * 5 + 
                  data['accessoryAttributeNum'] * 800 + 
                  data['collect'] * 3)
    
    data['price'] = np.round(base_price * np.random.uniform(0.7, 1.3, n), 2)
    
    df = pd.DataFrame(data)
    os.makedirs('train_data', exist_ok=True)
    df.to_csv('train_data/equip_train_data.csv', index=False)
    print(f"Generated {n} equip data items.")

def generate_lingshi_data(n=10000):
    """生成灵饰模拟数据"""
    data = {
        'level': np.random.choice([60, 80, 100, 120, 140], n),
        'accessoryAttributeNum': np.random.randint(1, 4, n),
        'primeValue': np.random.randint(15, 45, n),
        'collect': np.random.randint(0, 400, n)
    }
    
    # 价格逻辑：灵饰等级和附属性条数（3条最贵）是核心
    base_price = (data['level'] * 30 + 
                  (data['accessoryAttributeNum'] ** 3) * 200 + 
                  data['primeValue'] * 10 + 
                  data['collect'] * 4)
    
    data['price'] = np.round(base_price * np.random.uniform(0.75, 1.25, n), 2)
    
    df = pd.DataFrame(data)
    os.makedirs('train_data', exist_ok=True)
    df.to_csv('train_data/lingshi_train_data.csv', index=False)
    print(f"Generated {n} lingshi data items.")

if __name__ == "__main__":
    generate_pet_data()
    generate_equip_data()
    generate_lingshi_data()
