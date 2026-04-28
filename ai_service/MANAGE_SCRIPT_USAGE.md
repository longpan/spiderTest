# AI服务管理脚本使用说明

## 快速开始

管理脚本已上传到远程服务器：`/home/ubuntu/ai_service/manage_ai_service.sh`

### 基本用法

```bash
ssh ubuntu@150.158.130.52
cd /home/ubuntu/ai_service

# 查看帮助
./manage_ai_service.sh

# 查看服务状态
./manage_ai_service.sh status
```

---

## 命令详解

### 1. 服务管理命令

#### start - 启动服务
```bash
./manage_ai_service.sh start
```
- 启动AI预测服务
- 如果服务已在运行，会提示当前PID

#### stop - 停止服务
```bash
./manage_ai_service.sh stop
```
- 停止正在运行的AI服务

#### restart - 重启服务
```bash
./manage_ai_service.sh restart
```
- 停止并重新启动服务
- 用于加载新模型或应用配置更改

#### status - 查看状态
```bash
./manage_ai_service.sh status
```
- 显示服务是否在运行
- 显示服务PID
- 显示访问地址

---

### 2. 监控和测试命令

#### logs - 查看日志
```bash
./manage_ai_service.sh logs
```
- 显示最新50行服务日志
- 用于排查问题和监控服务运行状态

#### test - 测试服务
```bash
./manage_ai_service.sh test
```
- 执行健康检查
- 执行预测测试
- 验证服务是否正常工作

---

### 3. 模型训练命令 ⭐ 新增

#### train - 训练新模型（不重启服务）
```bash
./manage_ai_service.sh train
```

**功能：**
1. 从数据库提取最新的宠物数据
2. 使用新数据训练AI模型
3. 保存新模型到 `train_model/` 目录

**特点：**
- ✅ 仅训练模型，不影响当前运行的服务
- ✅ 适合在非高峰期预先训练模型
- ✅ 训练完成后可以测试新模型效果
- ⚠️ 新模型不会自动加载到服务中

**使用场景：**
- 想要先训练模型，测试效果后再决定是否上线
- 在业务低峰期提前训练好模型
- 对比新旧模型的性能

**执行流程：**
```
[步骤 1/2] 从数据库提取最新训练数据...
  ↓ 提取8986+条宠物数据
[步骤 2/2] 训练AI估值模型...
  ↓ 训练随机森林模型
✓ 模型训练完成！
注意：新模型尚未加载到服务中。
如需使用新模型，请执行: ./manage_ai_service.sh restart
```

---

#### retrain - 重新训练模型并重启服务 ⭐ 推荐
```bash
./manage_ai_service.sh retrain
```

**功能：**
1. 从数据库提取最新的宠物数据
2. 使用新数据训练AI模型
3. **自动重启服务**加载新模型

**特点：**
- ✅ 一键完成训练和部署
- ✅ 新模型立即生效
- ✅ 适合日常模型更新

**使用场景：**
- 定期更新模型（建议每周一次）
- 数据有较大变化时重新训练
- 快速部署新模型到生产环境

**执行流程：**
```
[步骤 1/3] 从数据库提取最新训练数据...
  ↓ 提取最新宠物数据
[步骤 2/3] 训练AI估值模型...
  ↓ 训练并评估模型
[步骤 3/3] 重启AI服务以加载新模型...
  ↓ 重启服务
✓ 模型重新训练完成！
新模型已加载到服务中。
可以使用以下命令测试:
  ./manage_ai_service.sh test
```

---

## 使用示例

### 场景1：日常模型更新（推荐）

```bash
# 一键完成训练和部署
./manage_ai_service.sh retrain

# 验证服务是否正常
./manage_ai_service.sh test
```

### 场景2：谨慎更新（先训练，测试后再上线）

```bash
# 步骤1：训练新模型（不影响当前服务）
./manage_ai_service.sh train

# 步骤2：查看训练结果
cat train_model/pet_features_info.json

# 步骤3：如果满意，重启服务加载新模型
./manage_ai_service.sh restart

# 步骤4：测试新模型
./manage_ai_service.sh test
```

### 场景3：查看服务状态和日志

```bash
# 查看服务是否运行
./manage_ai_service.sh status

# 查看服务日志
./manage_ai_service.sh logs

# 实时监控日志
tail -f ai_service.log
```

### 场景4：服务出现问题时

```bash
# 查看服务状态
./manage_ai_service.sh status

# 查看日志排查问题
./manage_ai_service.sh logs

# 重启服务
./manage_ai_service.sh restart

# 验证服务恢复
./manage_ai_service.sh test
```

---

## 定时任务（可选）

### 每周自动重新训练模型

```bash
# 编辑cron任务
crontab -e

# 添加以下行（每周日凌晨2点自动重新训练）
0 2 * * 0 cd /home/ubuntu/ai_service && ./manage_ai_service.sh retrain >> /home/ubuntu/ai_service/retrain.log 2>&1
```

### 每天检查服务状态

```bash
# 编辑cron任务
crontab -e

# 添加以下行（每天早上8点检查服务状态）
0 8 * * * cd /home/ubuntu/ai_service && ./manage_ai_service.sh status >> /home/ubuntu/ai_service/status.log 2>&1 || ./manage_ai_service.sh start
```

---

## 训练数据说明

### 数据来源
- 数据库表：`mh_pet_item`
- 当前数据量：8986+ 条宠物记录
- 特征数量：14个（9个基础 + 5个衍生）

### 训练输出文件

训练完成后会生成以下文件：

```
train_data/
├── real_pet_train_data.csv      # 训练数据CSV
├── data_dictionary.json         # 数据字典
└── data_quality_report.json     # 数据质量报告

train_model/
├── mh_pet_model_real.pkl        # 训练好的模型
├── scaler_x_pet.pkl             # 特征标准化器
├── scaler_y_pet.pkl             # 目标标准化器
├── pet_features_info.json       # 特征信息和性能指标
└── plots/                       # 评估图表
    ├── pet_scatter.png          # 实际值vs预测值
    └── pet_residuals.png        # 残差分布
```

### 模型性能指标

训练完成后会显示：
- **MAE** (平均绝对误差)：预测价格与实际价格的平均差距
- **R²** (决定系数)：模型的解释力（0-1之间，越接近1越好）
- **特征重要性**：各特征对价格预测的影响程度

---

## 常见问题

### Q1: train 和 retrain 有什么区别？

| 命令 | 训练模型 | 重启服务 | 新模型生效 | 适用场景 |
|------|---------|---------|-----------|---------|
| `train` | ✅ | ❌ | ❌ | 先训练测试，稍后上线 |
| `retrain` | ✅ | ✅ | ✅ | 一键训练并部署 |

### Q2: 训练需要多长时间？

- **数据提取**：约5-10秒（8986条数据）
- **模型训练**：约1-2秒（随机森林100棵树）
- **服务重启**：约3-5秒
- **总计**：约10-20秒

### Q3: 训练会影响正在运行的服务吗？

- 使用 `train` 命令：**不会影响**，服务继续运行
- 使用 `retrain` 命令：**会短暂中断**（约3-5秒重启时间）

### Q4: 多久需要重新训练一次模型？

**建议：**
- **正常情况**：每周一次
- **数据变化快**：每3-4天一次
- **数据量大幅增加**：立即重新训练
- **模型性能下降**：立即重新训练

### Q5: 如何判断新模型是否更好？

```bash
# 1. 查看新模型的性能指标
cat train_model/pet_features_info.json | python3 -m json.tool

# 2. 关注以下指标
- r2: 越高越好（>0.3可接受，>0.5良好，>0.7优秀）
- mae: 越低越好（<500良好，<300优秀）
- median_relative_error: 越低越好（<50%可接受，<30%良好）

# 3. 对比新旧模型的性能
# 如果新模型指标更好，说明训练有效
```

### Q6: 训练失败怎么办？

```bash
# 1. 检查数据库连接
mysql -h 150.158.130.52 -u root -p spider

# 2. 检查数据量
ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && ./venv/bin/python -c \"import mysql.connector; conn=mysql.connector.connect(host='150.158.130.52',user='root',password='qwer@1234',database='spider'); cursor=conn.cursor(); cursor.execute('SELECT COUNT(*) FROM mh_pet_item'); print('数据量:', cursor.fetchone()[0])\""

# 3. 检查Python环境
./manage_ai_service.sh status

# 4. 重新尝试训练
./manage_ai_service.sh retrain
```

---

## 最佳实践

### 1. 定期更新模型
```bash
# 每周一早上执行
./manage_ai_service.sh retrain
./manage_ai_service.sh test
```

### 2. 监控服务健康
```bash
# 每天检查
./manage_ai_service.sh status

# 查看错误日志
grep ERROR ai_service.log | tail -20
```

### 3. 备份模型文件
```bash
# 训练前备份旧模型
cd /home/ubuntu/ai_service
cp train_model/mh_pet_model_real.pkl train_model/mh_pet_model_real.pkl.backup.$(date +%Y%m%d)

# 然后重新训练
./manage_ai_service.sh retrain
```

### 4. 对比新旧模型
```bash
# 查看新模型性能
cat train_model/pet_features_info.json | python3 -m json.tool | grep -E '"r2"|"mae"'

# 如果性能下降，可以回滚
cp train_model/mh_pet_model_real.pkl.backup.20260428 train_model/mh_pet_model_real.pkl
./manage_ai_service.sh restart
```

---

## 总结

| 命令 | 用途 | 推荐频率 |
|------|------|---------|
| `status` | 查看服务状态 | 每天 |
| `logs` | 查看日志 | 需要时 |
| `test` | 测试服务 | 每次重启后 |
| `train` | 训练模型（不上线） | 需要时 |
| `retrain` | 训练并部署模型 | 每周一次 |
| `restart` | 重启服务 | 需要时 |

**最常用的命令组合：**
```bash
# 每周模型更新
./manage_ai_service.sh retrain && ./manage_ai_service.sh test
```

---

**更新时间**: 2026-04-28  
**版本**: v2.1 (新增train和retrain命令)
