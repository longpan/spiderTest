#!/usr/bin/env python3
"""
使用真实数据库数据训练AI估值模型
支持宠物、装备、灵饰三类物品的模型训练
"""

import pandas as pd
import joblib
import os
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score, mean_squared_error
from sklearn.preprocessing import StandardScaler, MinMaxScaler
import matplotlib.pyplot as plt
import matplotlib
# 设置matplotlib使用英文字体，避免中文字体警告
matplotlib.rcParams['font.sans-serif'] = ['Arial', 'DejaVu Sans', 'Liberation Sans']
matplotlib.rcParams['axes.unicode_minus'] = False
from datetime import datetime
import json

# 确保目录存在
MODEL_DIR = 'train_model'
REAL_DATA_DIR = 'train_data'
os.makedirs(MODEL_DIR, exist_ok=True)

# 训练参数
RANDOM_STATE = 42
TEST_SIZE = 0.2

def load_real_data(type_name: str):
    """加载真实训练数据"""
    data_path = os.path.join(REAL_DATA_DIR, f'real_{type_name}_train_data.csv')
    
    if not os.path.exists(data_path):
        print(f"警告: 真实数据文件 {data_path} 不存在")
        print("请先运行 extract_train_data.py 提取数据")
        return None
    
    try:
        df = pd.read_csv(data_path)
        print(f"加载 {type_name} 数据: {len(df)} 条记录")
        return df
    except Exception as e:
        print(f"加载 {type_name} 数据失败: {e}")
        return None

def preprocess_data(df: pd.DataFrame, feature_cols: list):
    """数据预处理"""
    if df is None or len(df) == 0:
        return None, None, None, None, None
    
    # 确保所有需要的列都存在
    missing_cols = [col for col in feature_cols if col not in df.columns]
    if missing_cols:
        print(f"警告: 缺少列 {missing_cols}")
        # 尝试用默认值填充
        for col in missing_cols:
            df[col] = 0.0
    
    # 移除空值行（目标变量price必须非空）
    original_len = len(df)
    df_clean = df.dropna(subset=['price'] + feature_cols)
    removed_count = original_len - len(df_clean)
    
    if removed_count > 0:
        print(f"移除 {removed_count} 条包含空值的记录")
    
    if len(df_clean) == 0:
        print("错误: 清理后无有效数据")
        return None, None, None, None, None
    
    # 分离特征和目标
    X = df_clean[feature_cols]
    y = df_clean['price']
    
    # 检查数据分布
    print(f"\n价格统计:")
    print(f"  最小值: {y.min():.2f}")
    print(f"  最大值: {y.max():.2f}")
    print(f"  平均值: {y.mean():.2f}")
    print(f"  中位数: {y.median():.2f}")
    print(f"  标准差: {y.std():.2f}")
    
    # 检测异常值（价格过高或过低）
    q1 = y.quantile(0.25)
    q3 = y.quantile(0.75)
    iqr = q3 - q1
    lower_bound = q1 - 1.5 * iqr
    upper_bound = q3 + 1.5 * iqr
    
    outliers = (y < lower_bound) | (y > upper_bound)
    outlier_count = outliers.sum()
    
    if outlier_count > 0:
        print(f"\n检测到 {outlier_count} 个价格异常值 (占比: {outlier_count/len(y)*100:.1f}%)")
        print(f"异常值范围: {y[outliers].min():.2f} ~ {y[outliers].max():.2f}")
        
        # 可以选择移除异常值或保留
        print("将保留异常值以保持数据完整性")
    
    # 特征缩放（可选）
    scaler_x = StandardScaler()
    X_scaled = scaler_x.fit_transform(X)
    
    scaler_y = StandardScaler()
    y_scaled = scaler_y.fit_transform(y.values.reshape(-1, 1)).ravel()
    
    return X_scaled, y_scaled, scaler_x, scaler_y, df_clean

def train_model(type_name: str, feature_cols: list, use_real_data: bool = True):
    """训练模型"""
    print(f"\n{'='*60}")
    print(f"训练 {type_name.upper()} 估值模型")
    print(f"{'='*60}")
    
    # 加载数据
    if use_real_data:
        df = load_real_data(type_name)
        if df is None:
            print(f"{type_name} 真实数据加载失败")
            return
    else:
        # 使用模拟数据（兼容旧版本）
        data_path = os.path.join(REAL_DATA_DIR, f'{type_name}_train_data.csv')
        if not os.path.exists(data_path):
            print(f"数据文件不存在: {data_path}")
            return
        df = pd.read_csv(data_path)
        print(f"加载模拟数据: {len(df)} 条记录")
    
    # 数据预处理
    print("\n=== 数据预处理 ===")
    X, y, scaler_x, scaler_y, df_clean = preprocess_data(df, feature_cols)
    
    if X is None or y is None:
        print("数据预处理失败，跳过训练")
        return
    
    # 划分训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=TEST_SIZE, random_state=RANDOM_STATE
    )
    
    print(f"\n数据划分:")
    print(f"  训练集: {len(X_train)} 条记录")
    print(f"  测试集: {len(X_test)} 条记录")
    print(f"  特征维度: {X_train.shape[1]} 维")
    
    # 初始化模型
    print(f"\n=== 模型训练 ===")
    
    # 使用随机森林回归器
    model = RandomForestRegressor(
        n_estimators=100,          # 树的数量
        max_depth=None,            # 树的最大深度
        min_samples_split=2,       # 分裂所需最小样本数
        min_samples_leaf=1,        # 叶节点最小样本数
        max_features='sqrt',       # 考虑的特征数
        random_state=RANDOM_STATE,
        n_jobs=-1,                 # 使用所有CPU核心
        verbose=0
    )
    
    # 训练模型
    print("正在训练模型...")
    model.fit(X_train, y_train)
    print("模型训练完成！")
    
    # 评估模型
    print(f"\n=== 模型评估 ===")
    
    # 在测试集上预测
    y_pred_scaled = model.predict(X_test)
    
    # 反标准化预测值
    y_test_original = scaler_y.inverse_transform(y_test.reshape(-1, 1)).ravel()
    y_pred_original = scaler_y.inverse_transform(y_pred_scaled.reshape(-1, 1)).ravel()
    
    # 计算评估指标
    mae = mean_absolute_error(y_test_original, y_pred_original)
    mse = mean_squared_error(y_test_original, y_pred_original)
    rmse = np.sqrt(mse)
    r2 = r2_score(y_test_original, y_pred_original)
    
    print(f"评估指标:")
    print(f"  平均绝对误差 (MAE): {mae:.2f}")
    print(f"  均方误差 (MSE): {mse:.2f}")
    print(f"  均方根误差 (RMSE): {rmse:.2f}")
    print(f"  决定系数 (R²): {r2:.4f}")
    
    # 计算相对误差
    relative_errors = np.abs((y_pred_original - y_test_original) / (y_test_original + 1e-10))
    median_relative_error = np.median(relative_errors) * 100
    mean_relative_error = np.mean(relative_errors) * 100
    
    print(f"误差分析:")
    print(f"  中位数相对误差: {median_relative_error:.1f}%")
    print(f"  平均相对误差: {mean_relative_error:.1f}%")
    
    # 特征重要性分析
    print(f"\n=== 特征重要性 ===")
    feature_importance = model.feature_importances_
    importance_df = pd.DataFrame({
        'feature': feature_cols,
        'importance': feature_importance
    }).sort_values('importance', ascending=False)
    
    print("特征重要性排序:")
    for idx, row in importance_df.iterrows():
        print(f"  {row['feature']:25}: {row['importance']:.4f}")
    
    # 保存模型和相关文件
    print(f"\n=== 保存模型 ===")
    
    # 保存模型
    model_path = os.path.join(MODEL_DIR, f'mh_{type_name}_model_real.pkl')
    joblib.dump(model, model_path)
    print(f"模型已保存到: {model_path}")
    
    # 保存标准化器
    scaler_x_path = os.path.join(MODEL_DIR, f'scaler_x_{type_name}.pkl')
    scaler_y_path = os.path.join(MODEL_DIR, f'scaler_y_{type_name}.pkl')
    
    joblib.dump(scaler_x, scaler_x_path)
    joblib.dump(scaler_y, scaler_y_path)
    
    print(f"特征标准化器已保存到: {scaler_x_path}")
    print(f"目标标准化器已保存到: {scaler_y_path}")
    
    # 保存特征列
    features_info = {
        'type': type_name,
        'feature_columns': feature_cols,
        'training_date': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        'training_stats': {
            'train_samples': len(X_train),
            'test_samples': len(X_test),
            'mae': float(mae),
            'mse': float(mse),
            'rmse': float(rmse),
            'r2': float(r2),
            'median_relative_error': float(median_relative_error),
            'mean_relative_error': float(mean_relative_error)
        },
        'feature_importance': importance_df.to_dict('records')
    }
    
    features_path = os.path.join(MODEL_DIR, f'{type_name}_features_info.json')
    with open(features_path, 'w', encoding='utf-8') as f:
        json.dump(features_info, f, ensure_ascii=False, indent=2)
    
    print(f"特征信息已保存到: {features_path}")
    
    # 生成可视化评估报告（可选）
    generate_evaluation_plots(y_test_original, y_pred_original, type_name)
    
    return model, scaler_x, scaler_y

def generate_evaluation_plots(y_true, y_pred, type_name):
    """生成评估图表"""
    try:
        import matplotlib
        matplotlib.use('Agg')  # 无头模式
        
        # 创建图表目录
        plot_dir = os.path.join(MODEL_DIR, 'plots')
        os.makedirs(plot_dir, exist_ok=True)
        
        # 1. 实际值 vs 预测值散点图
        plt.figure(figsize=(10, 8))
        plt.scatter(y_true, y_pred, alpha=0.5, s=20)
        
        # 添加对角线（理想预测线）
        max_val = max(y_true.max(), y_pred.max())
        min_val = min(y_true.min(), y_pred.min())
        plt.plot([min_val, max_val], [min_val, max_val], 'r--', linewidth=2, label='理想预测线')
        
        plt.xlabel('实际价格')
        plt.ylabel('预测价格')
        plt.title(f'{type_name.upper()} 估值模型: 实际值 vs 预测值')
        plt.legend()
        plt.grid(True, alpha=0.3)
        
        scatter_path = os.path.join(plot_dir, f'{type_name}_scatter.png')
        plt.tight_layout()
        plt.savefig(scatter_path, dpi=150)
        plt.close()
        
        # 2. 残差分布图
        residuals = y_pred - y_true
        
        plt.figure(figsize=(10, 6))
        plt.hist(residuals, bins=50, alpha=0.7, edgecolor='black')
        plt.xlabel('残差 (预测值 - 实际值)')
        plt.ylabel('频次')
        plt.title(f'{type_name.upper()} 估值模型: 残差分布')
        plt.grid(True, alpha=0.3)
        
        residual_path = os.path.join(plot_dir, f'{type_name}_residuals.png')
        plt.tight_layout()
        plt.savefig(residual_path, dpi=150)
        plt.close()
        
        print(f"评估图表已保存到: {plot_dir}/")
        
    except Exception as e:
        print(f"生成评估图表失败: {e} (可能需要安装matplotlib)")

def main():
    """主函数"""
    print("=" * 80)
    print("梦幻西游藏宝阁AI估值模型训练工具 (使用真实数据)")
    print("=" * 80)
    print(f"开始时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    # 切换到脚本所在目录
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
    # 检查是否有真实数据
    real_data_exists = os.path.exists(os.path.join(REAL_DATA_DIR, 'real_pet_train_data.csv'))
    
    if not real_data_exists:
        print("警告: 未找到真实训练数据文件")
        print("请先运行 extract_train_data.py 从数据库提取数据")
        print("将使用模拟数据进行训练...")
    
    # 宠物特征定义（与Java端保持一致）
    pet_features = [
        'level', 'skillNum', 'attackQualification', 'defenseQualification',
        'physicalQualification', 'manaQualification', 'speedQualification',
        'growUp', 'collect'
    ]
    
    # 增强版宠物特征（包含衍生特征）
    enhanced_pet_features = [
        'level', 'skillNum', 'attackQualification', 'defenseQualification',
        'physicalQualification', 'manaQualification', 'speedQualification',
        'growUp', 'collect',
        'rareSkillCount', 'qualPercent', 'skillScore', 'growLevelScore', 'isBaby'
    ]
    
    # 装备/灵饰特征
    equip_features = [
        'level', 'accessoryAttributeNum', 'primeValue', 'collect'
    ]
    
    # 选择使用增强特征还是基础特征
    use_enhanced_features = True
    
    if use_enhanced_features:
        print("使用增强特征集 (包含衍生特征)")
        pet_feature_set = enhanced_pet_features
    else:
        print("使用基础特征集")
        pet_feature_set = pet_features
    
    # 训练模型
    models = {}
    
    # 1. 训练宠物模型
    print("\n" + "="*80)
    print("1. 训练宠物估值模型")
    print("="*80)
    
    pet_model, pet_scaler_x, pet_scaler_y = train_model(
        'pet', pet_feature_set, use_real_data=real_data_exists
    )
    
    if pet_model:
        models['pet'] = {
            'model': pet_model,
            'scaler_x': pet_scaler_x,
            'scaler_y': pet_scaler_y,
            'features': pet_feature_set
        }
    
    # 2. 训练装备模型（如果有数据）
    print("\n" + "="*80)
    print("2. 训练装备估值模型")
    print("="*80)
    
    # 检查装备数据
    equip_data_path = os.path.join(REAL_DATA_DIR, 'real_equip_train_data.csv')
    if not os.path.exists(equip_data_path):
        print("未找到装备数据，跳过装备模型训练")
    else:
        equip_model, equip_scaler_x, equip_scaler_y = train_model(
            'equip', equip_features, use_real_data=real_data_exists
        )
        
        if equip_model:
            models['equip'] = {
                'model': equip_model,
                'scaler_x': equip_scaler_x,
                'scaler_y': equip_scaler_y,
                'features': equip_features
            }
    
    # 3. 训练灵饰模型（如果有数据）
    print("\n" + "="*80)
    print("3. 训练灵饰估值模型")
    print("="*80)
    
    # 检查灵饰数据
    lingshi_data_path = os.path.join(REAL_DATA_DIR, 'real_lingshi_train_data.csv')
    if not os.path.exists(lingshi_data_path):
        print("未找到灵饰数据，跳过灵饰模型训练")
    else:
        lingshi_model, lingshi_scaler_x, lingshi_scaler_y = train_model(
            'lingshi', equip_features, use_real_data=real_data_exists
        )
        
        if lingshi_model:
            models['lingshi'] = {
                'model': lingshi_model,
                'scaler_x': lingshi_scaler_x,
                'scaler_y': lingshi_scaler_y,
                'features': equip_features
            }
    
    # 生成训练总结
    print("\n" + "="*80)
    print("训练总结")
    print("="*80)
    
    if len(models) == 0:
        print("没有成功训练任何模型")
    else:
        print(f"成功训练 {len(models)} 个模型:")
        for model_type, model_info in models.items():
            print(f"  - {model_type.upper()} 模型")
            print(f"    特征数量: {len(model_info['features'])}")
            print(f"    特征: {', '.join(model_info['features'][:5])}...")
    
    print(f"\n完成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*80)

if __name__ == "__main__":
    main()