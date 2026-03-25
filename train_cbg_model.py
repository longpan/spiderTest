import pandas as pd
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
import joblib

# 1. 加载数据 (假设你从数据库导出了 cbg_item.csv)
# 字段包括: price, overall_score, person_score, level, wrap_name (sect), collect
def train_model():
    try:
        data = pd.read_csv('cbg_item.csv')
        
        # 2. 预处理
        # 处理数值字段
        data['price'] = pd.to_numeric(data['price'].str.replace(r'[^0-9.]', '', regex=True), errors='coerce')
        
        # 处理分类字段 (门派)
        le = LabelEncoder()
        data['sect_encoded'] = le.fit_transform(data['wrapName'])
        
        # 选择特征 (Features) 和 标签 (Target)
        features = ['overallScore', 'personScore', 'level', 'sect_encoded', 'collect']
        X = data[features]
        y = data['price']
        
        # 3. 训练模型 (XGBoost)
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        
        model = xgb.XGBRegressor(
            n_estimators=100,
            learning_rate=0.1,
            max_depth=5,
            objective='reg:squarederror'
        )
        
        model.fit(X_train, y_train)
        
        # 4. 保存模型和编码器
        joblib.dump(model, 'cbg_xgboost_model.pkl')
        joblib.dump(le, 'sect_encoder.pkl')
        print("模型训练完成并保存为 cbg_xgboost_model.pkl")

    except Exception as e:
        print(f"训练失败: {e}")

if __name__ == "__main__":
    train_model()
