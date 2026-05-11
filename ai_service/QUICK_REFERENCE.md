# AI服务管理 - 快速参考

## 常用命令

### 服务管理
```bash
ssh ubuntu@150.158.130.52
cd /home/ubuntu/ai_service

# 查看状态
./manage_ai_service.sh status

# 重启服务
./manage_ai_service.sh restart

# 查看日志
./manage_ai_service.sh logs
```

### 模型训练 ⭐

```bash
# 方式1：一键训练并部署（推荐）
./manage_ai_service.sh retrain

# 方式2：仅训练，稍后手动部署
./manage_ai_service.sh train
# 测试满意后再重启
./manage_ai_service.sh restart
```

### 测试服务
```bash
# 健康检查 + 预测测试
./manage_ai_service.sh test
```

---

## 完整命令列表

| 命令 | 功能 | 影响服务 |
|------|------|---------|
| `start` | 启动服务 | - |
| `stop` | 停止服务 | ❌ 停止 |
| `restart` | 重启服务 | ⚠️ 短暂中断(3-5秒) |
| `status` | 查看状态 | ✅ 无影响 |
| `logs` | 查看日志 | ✅ 无影响 |
| `test` | 测试服务 | ✅ 无影响 |
| `train` | 训练模型 | ✅ 无影响 |
| `retrain` | 训练+部署 | ⚠️ 短暂中断(3-5秒) |

---

## 典型工作流

### 📅 每周模型更新
```bash
./manage_ai_service.sh retrain && ./manage_ai_service.sh test
```

### 🔍 先测试再上线
```bash
# 1. 训练新模型
./manage_ai_service.sh train

# 2. 查看模型性能
cat train_model/pet_features_info.json | python3 -m json.tool

# 3. 如果满意，重启服务
./manage_ai_service.sh restart

# 4. 测试服务
./manage_ai_service.sh test
```

### 🚨 服务异常处理
```bash
# 1. 查看状态
./manage_ai_service.sh status

# 2. 查看日志
./manage_ai_service.sh logs

# 3. 重启服务
./manage_ai_service.sh restart

# 4. 验证恢复
./manage_ai_service.sh test
```

---

## 服务信息

- **地址**: http://150.158.130.52:8000
- **健康检查**: http://150.158.130.52:8000/health
- **预测接口**: POST http://150.158.130.52:8000/predict
- **目录**: /home/ubuntu/ai_service

---

## 详细文档

- 部署指南: `DEPLOYMENT_GUIDE.md`
- 使用手册: `MANAGE_SCRIPT_USAGE.md`
- 训练说明: `README_REAL_DATA_TRAINING.md`

---

**快速帮助**: `./manage_ai_service.sh` （不带参数）
