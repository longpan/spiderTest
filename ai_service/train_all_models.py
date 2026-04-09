import pandas as pd
import joblib
import os
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score

# 确保模型存储目录存在
MODEL_DIR = 'train_model'
DATA_DIR = 'train_data'

def train_model(type_name, feature_cols, target_col='price'):
    print(f"\n--- Training {type_name.upper()} Model ---")
    
    # 1. 加载数据
    data_path = os.path.join(DATA_DIR, f'{type_name}_train_data.csv')
    if not os.path.exists(data_path):
        print(f"Error: Data file {data_path} not found!")
        return
    
    df = pd.read_csv(data_path)
    
    # 2. 准备特征和目标
    X = df[feature_cols]
    y = df[target_col]
    
    # 3. 划分训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    # 4. 初始化并训练模型 (使用随机森林回归器)
    model = RandomForestRegressor(n_estimators=100, random_state=42, n_jobs=-1)
    model.fit(X_train, y_train)
    
    # 5. 评估模型
    y_pred = model.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    
    print(f"Model Evaluation for {type_name}:")
    print(f"  - Mean Absolute Error: {mae:.2f}")
    print(f"  - R2 Score: {r2:.4f}")
    
    # 6. 保存模型
    os.makedirs(MODEL_DIR, exist_ok=True)
    model_path = os.path.join(MODEL_DIR, f'mh_{type_name}_model.pkl')
    joblib.dump(model, model_path)
    print(f"Model saved to: {model_path}")

if __name__ == "__main__":
    # 定义各类型物品的特征列 (必须与 Java 端传参一致)
    pet_features = [
        'level', 'skillNum', 'attackQualification', 'defenseQualification', 
        'physicalQualification', 'manaQualification', 'speedQualification', 
        'growUp', 'collect'
    ]
    
    equip_features = [
        'level', 'accessoryAttributeNum', 'primeValue', 'collect'
    ]
    
    lingshi_features = [
        'level', 'accessoryAttributeNum', 'primeValue', 'collect'
    ]
    
    # 切换到脚本所在目录
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
    # 执行训练
    train_model('pet', pet_features)
    train_model('equip', equip_features)
    train_model('lingshi', lingshi_features)
    
    print("\nAll models trained and saved successfully.")
