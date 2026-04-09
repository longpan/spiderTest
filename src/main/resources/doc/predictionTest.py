import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error

# 假设我们已经有一个包含装备特征和价格的DataFrame
data = pd.read_csv('dream_of_mirror_equipment_data.csv')

# 特征列（需要根据实际情况调整）
features = ['type', 'quality', 'level', 'attack', 'defense', 'hp', 'mp', 'special_skill', 'rarity']

# 目标列（价格）
target = 'price'

# 划分训练集和测试集
X = data[features]
y = data[target]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 创建随机森林回归模型
model = RandomForestRegressor(n_estimators=100, random_state=42)

# 训练模型
model.fit(X_train, y_train)

# 预测测试集价格
y_pred = model.predict(X_test)

# 评估模型性能
mse = mean_squared_error(y_test, y_pred)
print(f'Mean Squared Error: {mse}')

# 对新的装备进行估价（示例）
new_equipment = pd.DataFrame({
    'type': ['weapon'],
    'quality': ['legendary'],
    'level': [100],
    'attack': [500],
    'defense': [200],
    'hp': [300],
    'mp': [150],
    'special_skill': ['fireball'],
    'rarity': ['rare']
})

# 预测新装备的价格
predicted_price = model.predict(new_equipment)
print(f'Predicted Price for New Equipment: {predicted_price[0]}')