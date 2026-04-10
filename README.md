# spiderTest

藏宝阁爬虫系统 - 梦幻西游数据采集与AI估值

## 技术栈

- Java 1.8
- Spring Boot 2.2.6
- WebMagic 0.7.3
- Selenium 2.33.0
- MyBatis-Plus 3.3.0
- Swagger 2.9.2

## 快速开始

### 1. 配置认证信息

调用 `/auth/config` 接口保存认证配置：

```bash
curl -X POST http://localhost:8081/auth/config \
  -H "Content-Type: application/json" \
  -d '{
    "sid": "你的sid值",
    "loginId": "你的登录ID",
    "cbgQrcode": "你的二维码凭证",
    "recoSid": "你的recoSid",
    "loginMode": "cookie",
    "selenuimConfig": "/home/ubuntu/selenium/config.ini",
    "chromeDriverPath": "/usr/bin/chromedriver",
    "headlessMode": true,
    "isValid": true
  }'
```

> **重要**：必须设置 `isValid: true`，否则调度器会因为认证无效而不执行任务。

### 2. 创建爬取任务

```bash
# 创建列表页爬取任务（爬取宠物列表，最多3页）
curl -X POST "http://localhost:8081/task/create?url=https://xyq.cbg.163.com/cgi-bin/query.py?act=search_pet&maxPage=3"
```

### 3. 启动调度器

```bash
curl -X POST http://localhost:8081/task/startScheduler
```

### 4. 查询状态

```bash
# 查询调度器状态
curl http://localhost:8081/task/scheduler/status

# 查询任务列表
curl "http://localhost:8081/task/list?status=PENDING"
```

## API接口

### 认证配置管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/config` | POST | 保存全局认证配置 |
| `/auth/config` | GET | 获取当前认证配置 |
| `/auth/config/check` | POST | 检查认证是否有效 |
| `/auth/config/status` | POST | 手动设置认证状态 |

### 任务管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/task/create` | POST | 创建爬取任务 |
| `/task/list` | GET | 查询任务列表 |
| `/task/start/{taskId}` | POST | 手动启动任务 |
| `/task/stop/{taskId}` | POST | 停止任务 |
| `/task/retry/{taskId}` | POST | 重试任务 |
| `/task/startScheduler` | POST | 启动后台调度器 |
| `/task/stopScheduler` | POST | 停止后台调度器 |
| `/task/scheduler/status` | GET | 查询调度器状态 |

### 重新爬取

| 接口 | 方法 | 说明 |
|------|------|------|
| `/task/replay/itemCode/{itemCode}` | POST | 根据物品编号重新爬取 |
| `/task/replay/itemId/{itemId}` | POST | 根据物品ID重新爬取 |
| `/task/replay/batch` | POST | 批量重新爬取 |

## Swagger文档

启动服务后访问：http://localhost:8081/swagger-ui.html

## 配置说明

在 `application.yml` 中可调整以下参数：

```yaml
spider:
  task:
    max-concurrent: 3      # 最大并发任务数
    poll-interval: 5000    # 任务轮询间隔（毫秒）
```

## 任务状态说明

| 状态 | 说明 |
|------|------|
| PENDING | 待执行 |
| RUNNING | 执行中 |
| SUCCESS | 执行成功 |
| FAILED | 执行失败 |
| STOPPED | 已停止（认证失效时自动停止） |

## 认证失效处理

当认证失效时：
1. 系统自动停止所有运行中的任务
2. 所有任务状态变更为 STOPPED
3. 需要重新配置认证信息并设置 `isValid: true`
4. 重新启动调度器

## 部署

```bash
# 打包
mvn clean package -DskipTests

# 上传到服务器
scp target/spider.jar user@server:/home/ubuntu/java_app/

# 启动服务
java -jar spider.jar
```

## 数据库表

系统启动时会自动创建以下表（通过 mybatis-enhance-actable）：

- `cbg_auth_config` - 全局认证配置表
- `cbg_spider_task` - 爬虫任务表
- `cbg_item` - 爬取数据表

