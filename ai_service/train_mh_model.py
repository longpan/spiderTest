import pandas as pd
import joblib
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
import numpy as np

def train_mock_pet_model():
    # 模拟一些训练数据
    data = {
        'level': np.random.randint(69, 175, 100),
        'skillNum': np.random.randint(4, 12, 100),
        'attackQualification': np.random.randint(1200, 1650, 100),
        'growUp': np.random.uniform(1.2, 1.3, 100),
        'collect': np.random.randint(0, 500, 100),
        'price': np.random.randint(500, 10000, 100)
    }
    df = pd.DataFrame(data)
    
    X = df[['level', 'skillNum', 'attackQualification', 'growUp', 'collect']]
    y = df['price']
    
    # 使用随机森林作为示例
    model = RandomForestRegressor(n_estimators=100)
    model.fit(X, y)
    
    # 保存模型
    joblib.dump(model, 'mh_pet_model.pkl')
    print("Pet model trained and saved as mh_pet_model.pkl")

if __name__ == "__main__":
    train_mock_pet_model()
