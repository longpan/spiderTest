# 使用真实数据库数据训练AI估值模型指南

## 概述

本指南介绍如何使用数据库中的真实宠物数据（mh_pet_item表）来训练AI估值模型，以替代之前的模拟数据训练。

## 准备工作

### 1. 确保数据库连接信息正确
检查 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://150.158.130.52:3306/spider?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: qwer@1234
```

### 2. 安装必要的Python依赖
```bash
pip install mysql-connector-python pandas scikit-learn joblib
```

如果已安装过，请确保版本兼容：
- mysql-connector-python >= 8.0
- pandas >= 1.4
- scikit-learn >= 1.2
- joblib >= 1.2

## 数据提取步骤

### 步骤1：从数据库提取训练数据
```bash
cd /Users/onglchen/projects/ai/codeBuddy/javaProjects/spiderTest/ai_service
python extract_train_data.py
```

**输出文件：**
- `train_data/real_pet_train_data.csv` - 宠物训练数据（5907条记录）
- `train_data/data_dictionary.json` - 数据字段解释
- `train_data/data_quality_report.json` - 数据质量报告

**关键特征：**
- 基础特征（9个）：`level`, `skillNum`, `attackQualification`, `defenseQualification`, `physicalQualification`, `manaQualification`, `speedQualification`, `growUp`, `collect`
- 衍生特征（5个）：`rareSkillCount`, `qualPercent`, `skillScore`, `growLevelScore`, `isBaby`
- 目标变量：`price`

## 模型训练步骤

### 步骤2：使用真实数据训练模型
```bash
python train_real_models.py
```

**训练过程：**
1. **数据加载**：加载真实数据库提取的数据
2. **数据预处理**：
   - 空值处理
   - 异常值检测
   - 特征标准化（StandardScaler）
3. **模型训练**：使用RandomForestRegressor（100棵树）
4. **模型评估**：
   - MAE（平均绝对误差）
   - MSE（均方误差）
   - R²（决定系数）
   - 相对误差分析
5. **特征重要性分析**：识别对价格影响最大的特征
6. **模型保存**：保存训练好的模型和标准化器

**输出文件：**
- `train_model/mh_pet_model_real.pkl` - 宠物估值模型
- `train_model/scaler_x_pet.pkl` - 特征标准化器
- `train_model/scaler_y_pet.pkl` - 目标标准化器
- `train_model/pet_features_info.json` - 特征信息
- `train_model/plots/` - 评估图表

## 特征工程详解

### 1. 稀有技能计数（rareSkillCount）
从技能列表（skillList）中识别稀有技能数量：
- **高价值技能**：须弥真言(50分), 力劈华山(40分), 壁垒击破(25分)
- **加分项**：拥有多个稀有技能显著提升价值

### 2. 资质百分比（qualPercent）
计算各资质占满资质的比例：
- 攻击资质：1600（上限参考值）
- 防御资质：1400
- 体力资质：6500
- 法力资质：2800
- 速度资质：1450
- 闪避资质：1200

**计算公式**：
```
qualPercent = (攻击/1600 + 防御/1400 + 体力/6500 + 法力/2800 + 速度/1450) / 5
```

### 3. 技能价值评分（skillScore）
特殊技能加权评分系统：
- **S级技能**：须弥真言(50), 观照万象(45)
- **A级技能**：力劈华山(40), 谛听(40)
- **B级技能**：壁垒击破(25), 死亡召唤(25)
- **普通技能**：连击(8), 必杀(8)等

**归一化处理**：
```
skillScore = totalScore / √skillNum
```

### 4. 成长等级评分（growLevelScore）
高成长+高等级的交叉特征：
```
growLevelScore = growUp × level × 10
```

### 5. 宝宝标识（isBaby）
布尔值转换为0/1：
- `1` = 宝宝（价值更高）
- `0` = 野生

## 训练评估指标

### 预期性能
基于5907条真实数据训练：

| 指标 | 期望范围 | 说明 |
|------|----------|------|
| MAE | 300-600 | 平均预测误差（金币） |
| R² | 0.7-0.9 | 模型解释力 |
| 中位数相对误差 | 15-25% | 典型预测误差率 |

### 模型优势
1. **真实数据**：基于实际交易数据，更贴近市场
2. **特征丰富**：包含技能、资质、成长等多维度信息
3. **非线性建模**：随机森林能捕捉复杂的价格关系
4. **可解释性**：特征重要性分析帮助理解定价因素

## 更新AI服务

### 步骤3：更新AI服务使用真实模型
修改 `mh_ai_service.py` 中的模型加载部分：

```python
# 修改模型加载路径
try:
    # 加载真实数据训练的模型
    models['pet'] = joblib.load(os.path.join(MODEL_DIR, 'mh_pet_model_real.pkl'))
    
    # 加载对应的标准化器
    scaler_x_pet = joblib.load(os.path.join(MODEL_DIR, 'scaler_x_pet.pkl'))
    scaler_y_pet = joblib.load(os.path.join(MODEL_DIR, 'scaler_y_pet.pkl'))
    
    print(f"[{datetime.now().strftime('%H:%M:%S')}] 真实数据训练的模型加载成功")
except Exception as e:
    print(f"[{datetime.now().strftime('%H:%M:%S')}] 警告: 真实模型加载失败, 使用旧模型或降级逻辑: {e}")
```

### 步骤4：修改预测函数
更新 `_predict_with_model` 函数以处理标准化数据：

```python
def _predict_with_model(model, data: ValuationRequest, scaler_x=None, scaler_y=None):
    # ... 构建特征字典 ...
    
    if data.type == "pet":
        df = pd.DataFrame([feat_dict])
        
        # 标准化特征
        if scaler_x is not None:
            X_scaled = scaler_x.transform(df)
            prediction_scaled = model.predict(X_scaled)
            
            # 反标准化预测值
            if scaler_y is not None:
                prediction = scaler_y.inverse_transform(prediction_scaled.reshape(-1, 1))
                return float(prediction[0])
        
        # 如果标准化器不可用，回退到原始预测
        prediction = model.predict(df)
        return float(prediction[0])
```

## 质量监控

### 数据质量检查
运行 `extract_train_data.py` 后检查：
1. **空值比例**：确保关键字段空值率<5%
2. **异常值检测**：识别价格极端值
3. **分布合理性**：检查各特征的数值范围是否符合预期

### 模型评估
运行 `train_real_models.py` 后检查：
1. **评估指标**：MAE, R² 是否在合理范围
2. **特征重要性**：排名前5的特征是否符合业务认知
3. **残差分布**：是否近似正态分布，无明显偏差

## 常见问题

### Q1：数据库连接失败
**症状**：`mysql.connector.Error: Access denied for user...`
**解决**：
1. 检查 `application.yml` 中的用户名密码
2. 确认数据库服务器可达：`ping 150.158.130.52`
3. 检查防火墙设置

### Q2：数据量不足
**症状**：模型评估指标较差（R² < 0.5）
**解决**：
1. 运行爬虫收集更多数据
2. 考虑使用数据增强技术
3. 调整模型参数（增加树的数量）

### Q3：特征缺失
**症状**：某些字段大量空值
**解决**：
1. 运行爬虫更新数据
2. 考虑使用特征工程生成替代特征
3. 调整模型使用更少的特征

### Q4：性能问题
**症状**：训练时间过长
**解决**：
1. 减少树的数量（n_estimators）
2. 增加 `min_samples_split` 参数
3. 使用特征选择减少特征数量

## 最佳实践

### 1. 定期更新模型
- 建议每周重新训练一次模型
- 使用最新爬取的数据
- 比较新旧模型性能

### 2. 监控模型性能
- 记录每次训练的评估指标
- 监控线上预测误差
- 设置性能告警阈值

### 3. 版本管理
- 为每个训练版本打标签
- 保留历史模型用于比对
- 记录训练参数和数据源

### 4. 持续优化
- 根据业务反馈调整特征
- 尝试不同的机器学习算法
- 优化模型超参数

## 下一步计划

### 短期（1-2周）
1. ✅ 完成真实数据提取脚本
2. ✅ 完成真实数据训练脚本
3. 更新AI服务使用真实模型
4. 评估真实模型性能

### 中期（1个月）
1. 扩展到装备、灵饰模型
2. 实现自动化训练流水线
3. 建立模型性能监控系统

### 长期（3个月）
1. 深度学习模型探索
2. 多模型集成
3. 实时在线学习

## 联系方式

如有问题，请联系：
- **系统负责人**：onglchen
- **技术支持**：技术支持团队
- **问题反馈**：在issue中提交问题

---

*最后更新：2026年4月13日*  
*版本：1.0*