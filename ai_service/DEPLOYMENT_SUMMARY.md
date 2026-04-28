# AI服务部署总结

## ✅ 部署完成

**部署时间**: 2026-04-28 16:45  
**服务器**: 150.158.130.52 (ubuntu)  
**服务目录**: /home/ubuntu/ai_service  

---

## 完成的工作

### 1. ✅ 环境配置
- [x] 创建Python虚拟环境 (venv)
- [x] 安装所有依赖包
  - fastapi 0.136.1
  - uvicorn 0.46.0
  - pydantic 2.13.3
  - pandas 3.0.2
  - scikit-learn 1.8.0
  - joblib 1.5.3
  - mysql-connector-python 9.6.0
  - matplotlib 3.10.9

### 2. ✅ 数据提取
- [x] 从数据库提取真实宠物数据
- [x] 数据量：**8986条记录**
- [x] 特征数：**14个**（9个基础 + 5个衍生）
- [x] 生成文件：
  - `train_data/real_pet_train_data.csv` - 训练数据
  - `train_data/data_dictionary.json` - 数据字典
  - `train_data/data_quality_report.json` - 数据质量报告

### 3. ✅ 模型训练
- [x] 使用随机森林训练宠物估值模型
- [x] 训练集：7188条 (80%)
- [x] 测试集：1798条 (20%)
- [x] 模型性能：
  - MAE: 548.60
  - R²: 0.3364
  - 中位数相对误差: 55.9%
- [x] 生成文件：
  - `train_model/mh_pet_model_real.pkl` - 模型
  - `train_model/scaler_x_pet.pkl` - 特征标准化器
  - `train_model/scaler_y_pet.pkl` - 目标标准化器
  - `train_model/pet_features_info.json` - 特征信息
  - `train_model/plots/` - 评估图表

### 4. ✅ 服务启动
- [x] AI预测服务已启动
- [x] 运行端口：**8000**
- [x] 服务PID: 834313
- [x] 健康检查: ✓ 正常
- [x] 预测功能: ✓ 正常

### 5. ✅ 管理工具
- [x] 创建服务管理脚本 `manage_ai_service.sh`
- [x] 创建部署文档 `DEPLOYMENT_GUIDE.md`
- [x] 创建部署脚本 `deploy_to_remote.sh`

---

## 服务访问

| 项目 | 地址 |
|------|------|
| **服务地址** | http://150.158.130.52:8000 |
| **健康检查** | http://150.158.130.52:8000/health |
| **预测接口** | http://150.158.130.52:8000/predict (POST) |

---

## 快速测试

### 测试1：健康检查
```bash
curl http://150.158.130.52:8000/health
```

预期响应：
```json
{
  "status": "ok",
  "loaded_models": ["pet"],
  "version": "2.0"
}
```

### 测试2：预测测试
```bash
curl -X POST http://150.158.130.52:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "type": "pet",
    "level": 100,
    "collect": 50,
    "skillNum": 8,
    "attackQualification": 1500,
    "defenseQualification": 1300,
    "physicalQualification": 6000,
    "manaQualification": 2600,
    "speedQualification": 1350,
    "growUp": 1.3,
    "skillScore": 45.5,
    "qualPercent": 0.85,
    "growLevelScore": 1300,
    "rareSkillCount": 2,
    "isBaby": 1
  }'
```

预期响应：
```json
{
  "estimated_price": 4672.11,
  "source": "AI_Model",
  "features_used": [...],
  "note": "Using real data trained model"
}
```

---

## 日常管理

### 查看服务状态
```bash
ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && ./manage_ai_service.sh status"
```

### 查看服务日志
```bash
ssh ubuntu@150.158.130.52 "tail -f /home/ubuntu/ai_service/ai_service.log"
```

### 重启服务
```bash
ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && ./manage_ai_service.sh restart"
```

### 停止服务
```bash
ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && ./manage_ai_service.sh stop"
```

---

## 重新训练模型

```bash
ssh ubuntu@150.158.130.52
cd /home/ubuntu/ai_service

# 提取最新数据
./venv/bin/python extract_train_data.py

# 训练模型
./venv/bin/python train_real_models.py

# 重启服务加载新模型
./manage_ai_service.sh restart
```

---

## 特征说明

### 基础特征（9个）
1. `level` - 等级
2. `skillNum` - 技能数量
3. `attackQualification` - 攻击资质
4. `defenseQualification` - 防御资质
5. `physicalQualification` - 体力资质
6. `manaQualification` - 法力资质
7. `speedQualification` - 速度资质
8. `growUp` - 成长值
9. `collect` - 收藏数

### 衍生特征（5个）
10. `rareSkillCount` - 稀有技能数量
11. `qualPercent` - 综合资质百分比
12. `skillScore` - 技能价值评分
13. `growLevelScore` - 成长等级评分
14. `isBaby` - 是否宝宝（1=是，0=否）

---

## 重要文件

| 文件 | 路径 | 说明 |
|------|------|------|
| AI服务 | `/home/ubuntu/ai_service/mh_ai_service.py` | FastAPI预测服务 |
| 管理脚本 | `/home/ubuntu/ai_service/manage_ai_service.sh` | 服务管理工具 |
| 服务日志 | `/home/ubuntu/ai_service/ai_service.log` | 运行日志 |
| 训练数据 | `/home/ubuntu/ai_service/train_data/real_pet_train_data.csv` | 8986条宠物数据 |
| 模型文件 | `/home/ubuntu/ai_service/train_model/mh_pet_model_real.pkl` | 训练好的模型 |
| 部署文档 | `/home/ubuntu/ai_service/DEPLOYMENT_GUIDE.md` | 详细使用指南 |

---

## 注意事项

⚠️ **重要提醒**：

1. **防火墙设置**：如果无法从本地访问服务，需要在服务器上开放8000端口
   ```bash
   ssh ubuntu@150.158.130.52
   sudo ufw allow 8000/tcp
   ```

2. **安全组规则**：如果是云服务器，需要在云平台控制台开放8000端口

3. **服务自启动**：当前服务不会开机自启，如需自启可添加systemd服务或cron任务

4. **数据库密码**：当前使用明文密码，生产环境建议使用环境变量

5. **API鉴权**：当前无鉴权机制，生产环境建议添加API Key

---

## 下一步建议

### 短期优化（1-2周）
- [ ] 添加服务开机自启动
- [ ] 配置日志轮转（logrotate）
- [ ] 添加API鉴权
- [ ] 监控服务运行状态

### 中期优化（1个月）
- [ ] 装备和灵饰模型训练
- [ ] 模型性能优化（超参数调优）
- [ ] 自动化训练流水线
- [ ] 添加缓存层（Redis）

### 长期优化（3个月）
- [ ] 深度学习模型探索
- [ ] 多模型集成
- [ ] 实时在线学习
- [ ] 模型A/B测试框架

---

## 问题排查

如果遇到问题，请按以下步骤排查：

1. **检查服务状态**
   ```bash
   ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && ./manage_ai_service.sh status"
   ```

2. **查看服务日志**
   ```bash
   ssh ubuntu@150.158.130.52 "tail -50 /home/ubuntu/ai_service/ai_service.log"
   ```

3. **测试服务功能**
   ```bash
   ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && ./manage_ai_service.sh test"
   ```

4. **查看详细文档**
   - 部署指南：`/home/ubuntu/ai_service/DEPLOYMENT_GUIDE.md`
   - 训练指南：`/home/ubuntu/ai_service/README_REAL_DATA_TRAINING.md`

---

**部署状态**: ✅ 成功  
**服务状态**: ✅ 运行中  
**模型状态**: ✅ 已加载  
**测试状态**: ✅ 通过  

🎉 **AI估值模型已成功部署并运行！**
