# AI估值模型远程部署指南

## 部署概览

已成功在远程服务器上完成以下工作：

✅ **数据提取**：从数据库提取8986条真实宠物数据  
✅ **模型训练**：使用随机森林训练宠物估值模型（14个特征）  
✅ **服务启动**：AI预测服务已启动并运行在8000端口  
✅ **服务测试**：健康检查和预测功能均正常  

---

## 服务信息

| 项目 | 值 |
|------|-----|
| **服务器IP** | 150.158.130.52 |
| **用户** | ubuntu |
| **服务目录** | /home/ubuntu/ai_service |
| **服务地址** | http://150.158.130.52:8000 |
| **健康检查** | http://150.158.130.52:8000/health |
| **预测接口** | http://150.158.130.52:8000/predict (POST) |
| **Python虚拟环境** | /home/ubuntu/ai_service/venv |

---

## 模型性能指标

基于8986条真实数据训练：

| 指标 | 值 | 说明 |
|------|-----|------|
| **训练集** | 7188条 | 80%数据 |
| **测试集** | 1798条 | 20%数据 |
| **MAE** | 548.60 | 平均绝对误差（金币） |
| **RMSE** | 2745.90 | 均方根误差 |
| **R²** | 0.3364 | 决定系数 |
| **中位数相对误差** | 55.9% | 典型预测误差率 |

### 特征重要性（前5名）

1. **skillNum** (技能数量): 0.2307
2. **growLevelScore** (成长等级评分): 0.1026
3. **growUp** (成长值): 0.0949
4. **attackQualification** (攻击资质): 0.0910
5. **collect** (收藏数): 0.0690

---

## 服务管理

### 方式1：使用管理脚本（推荐）

```bash
ssh ubuntu@150.158.130.52

# 查看服务状态
cd /home/ubuntu/ai_service
./manage_ai_service.sh status

# 查看日志
./manage_ai_service.sh logs

# 重启服务
./manage_ai_service.sh restart

# 测试服务
./manage_ai_service.sh test
```

### 方式2：手动管理

```bash
# 查看服务状态
ssh ubuntu@150.158.130.52 "ps aux | grep mh_ai_service | grep -v grep"

# 查看日志
ssh ubuntu@150.158.130.52 "tail -f /home/ubuntu/ai_service/ai_service.log"

# 停止服务
ssh ubuntu@150.158.130.52 "pkill -f mh_ai_service.py"

# 启动服务
ssh ubuntu@150.158.130.52 "cd /home/ubuntu/ai_service && nohup ./venv/bin/python mh_ai_service.py > ai_service.log 2>&1 &"
```

---

## API使用指南

### 1. 健康检查

```bash
curl http://150.158.130.52:8000/health
```

**响应示例：**
```json
{
  "status": "ok",
  "loaded_models": ["pet"],
  "version": "2.0"
}
```

### 2. 宠物估值预测

**接口：** `POST http://150.158.130.52:8000/predict`

**请求参数：**
```json
{
  "type": "pet",
  "level": 100,                    // 等级
  "collect": 50,                   // 收藏数
  "skillNum": 8,                   // 技能数量
  "attackQualification": 1500,     // 攻击资质
  "defenseQualification": 1300,    // 防御资质
  "physicalQualification": 6000,   // 体力资质
  "manaQualification": 2600,       // 法力资质
  "speedQualification": 1350,      // 速度资质
  "growUp": 1.3,                   // 成长值
  "skillScore": 45.5,              // 技能评分（衍生特征）
  "qualPercent": 0.85,             // 资质百分比（衍生特征）
  "growLevelScore": 1300,          // 成长等级评分（衍生特征）
  "rareSkillCount": 2,             // 稀有技能数量（衍生特征）
  "isBaby": 1                      // 是否宝宝（1=是，0=否）
}
```

**响应示例：**
```json
{
  "estimated_price": 4672.11,
  "source": "AI_Model",
  "features_used": [
    "type", "level", "collect", "skillNum",
    "attackQualification", "defenseQualification",
    "physicalQualification", "manaQualification",
    "speedQualification", "growUp", "skillScore",
    "qualPercent", "growLevelScore", "rareSkillCount", "isBaby"
  ],
  "note": "Using real data trained model"
}
```

### 3. Java端调用示例

```java
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class AIClient {
    
    private static final String AI_SERVICE_URL = "http://150.158.130.52:8000/predict";
    private RestTemplate restTemplate = new RestTemplate();
    
    public Double predictPetPrice(PetItem pet) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "pet");
        request.put("level", pet.getLevel());
        request.put("collect", pet.getCollect());
        request.put("skillNum", pet.getSkillNum());
        request.put("attackQualification", pet.getAttackQualification());
        request.put("defenseQualification", pet.getDefenseQualification());
        request.put("physicalQualification", pet.getPhysicalQualification());
        request.put("manaQualification", pet.getManaQualification());
        request.put("speedQualification", pet.getSpeedQualification());
        request.put("growUp", pet.getGrowUp());
        
        // 衍生特征（如果Java端已计算）
        request.put("skillScore", pet.getSkillScore());
        request.put("qualPercent", pet.getQualPercent());
        request.put("growLevelScore", pet.getGrowLevelScore());
        request.put("rareSkillCount", pet.getRareSkillCount());
        request.put("isBaby", pet.getIsBaby() ? 1 : 0);
        
        // 调用AI服务
        Map<String, Object> response = restTemplate.postForObject(
            AI_SERVICE_URL, request, Map.class
        );
        
        return (Double) response.get("estimated_price");
    }
}
```

---

## 文件结构

```
/home/ubuntu/ai_service/
├── venv/                          # Python虚拟环境
├── extract_train_data.py          # 数据提取脚本
├── train_real_models.py           # 模型训练脚本
├── mh_ai_service.py               # AI预测服务
├── manage_ai_service.sh           # 服务管理脚本
├── ai_service.log                 # 服务日志
├── ai_service.pid                 # 服务PID文件
├── requirements.txt               # Python依赖
├── train_data/                    # 训练数据
│   ├── real_pet_train_data.csv    # 宠物训练数据（8986条）
│   ├── data_dictionary.json       # 数据字典
│   └── data_quality_report.json   # 数据质量报告
└── train_model/                   # 模型文件
    ├── mh_pet_model_real.pkl      # 宠物估值模型
    ├── scaler_x_pet.pkl           # 特征标准化器
    ├── scaler_y_pet.pkl           # 目标标准化器
    ├── pet_features_info.json     # 特征信息
    └── plots/                     # 评估图表
        ├── pet_scatter.png        # 实际值vs预测值散点图
        └── pet_residuals.png      # 残差分布图
```

---

## 重新训练模型

如果需要使用最新数据重新训练模型：

```bash
ssh ubuntu@150.158.130.52
cd /home/ubuntu/ai_service

# 步骤1：提取最新数据
./venv/bin/python extract_train_data.py

# 步骤2：训练模型
./venv/bin/python train_real_models.py

# 步骤3：重启服务（加载新模型）
./manage_ai_service.sh restart
```

---

## 常见问题

### Q1：服务无法访问

**检查步骤：**
```bash
# 1. 检查服务是否运行
ssh ubuntu@150.158.130.52 "ps aux | grep mh_ai_service | grep -v grep"

# 2. 检查端口监听
ssh ubuntu@150.158.130.52 "netstat -tlnp | grep 8000"

# 3. 查看服务日志
ssh ubuntu@150.158.130.52 "tail -50 /home/ubuntu/ai_service/ai_service.log"

# 4. 在服务器上直接测试
ssh ubuntu@150.158.130.52 "curl http://localhost:8000/health"
```

**可能原因：**
- 防火墙阻止8000端口访问
- 服务未启动或已崩溃
- 安全组规则未开放8000端口

**解决方案（防火墙）：**
```bash
ssh ubuntu@150.158.130.52
sudo ufw allow 8000/tcp
```

### Q2：模型预测不准确

**可能原因：**
- 训练数据不足或数据质量差
- 特征工程需要优化
- 宠物类型特殊（稀有宠物）

**解决方案：**
```bash
# 1. 检查数据质量
ssh ubuntu@150.158.130.52 "cat /home/ubuntu/ai_service/train_data/data_quality_report.json"

# 2. 检查模型性能
ssh ubuntu@150.158.130.52 "cat /home/ubuntu/ai_service/train_model/pet_features_info.json"

# 3. 重新训练模型（使用更多数据）
./venv/bin/python extract_train_data.py
./venv/bin/python train_real_models.py
./manage_ai_service.sh restart
```

### Q3：如何添加装备/灵饰模型

**步骤：**
1. 创建装备/灵饰数据提取脚本（参考extract_train_data.py）
2. 提取数据到 `train_data/real_equip_train_data.csv`
3. 修改 `train_real_models.py` 添加装备特征定义
4. 运行训练脚本
5. 修改 `mh_ai_service.py` 加载新模型

---

## 性能优化建议

### 1. 模型优化
- 增加训练数据量（目前8986条）
- 尝试其他算法（XGBoost, LightGBM）
- 超参数调优
- 特征工程优化

### 2. 服务优化
- 使用Gunicorn替代Uvicorn（多进程）
- 添加缓存层（Redis）
- 负载均衡（多实例）

### 3. 数据更新
- 定期重新训练模型（建议每周）
- 监控模型性能下降
- A/B测试新旧模型

---

## 监控和日志

### 查看服务日志
```bash
# 实时查看日志
ssh ubuntu@150.158.130.52 "tail -f /home/ubuntu/ai_service/ai_service.log"

# 查看错误日志
ssh ubuntu@150.158.130.52 "grep ERROR /home/ubuntu/ai_service/ai_service.log"
```

### 监控服务状态
```bash
# 创建定时任务检查服务
crontab -e

# 添加以下行（每5分钟检查一次）
*/5 * * * * /home/ubuntu/ai_service/manage_ai_service.sh status > /dev/null 2>&1 || /home/ubuntu/ai_service/manage_ai_service.sh restart
```

---

## 安全注意事项

1. **数据库密码**：当前使用明文密码，建议改为环境变量或密钥管理
2. **API鉴权**：当前无鉴权，建议添加API Key或Token
3. **防火墙**：仅开放必要端口（8000）
4. **HTTPS**：生产环境建议使用HTTPS

---

## 联系支持

如有问题，请检查：
1. 服务日志：`/home/ubuntu/ai_service/ai_service.log`
2. 数据质量报告：`/home/ubuntu/ai_service/train_data/data_quality_report.json`
3. 模型性能报告：`/home/ubuntu/ai_service/train_model/pet_features_info.json`

---

**部署时间**：2026-04-28  
**部署版本**：v2.0  
**数据量**：8986条宠物记录  
**模型类型**：RandomForestRegressor (100 trees)
