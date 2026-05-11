# Docker 使用说明

本工程提供一套 Docker 运行环境，用于在单台服务器上同时运行：

- `spider.jar`（Spring Boot，默认 8081）
- `ai_service`（FastAPI/uvicorn，默认 8000）
- Chromium + chromedriver（用于 Selenium 抓取）
- VNC（Xvfb + fluxbox + x11vnc，默认 5901）

## 目录结构

- `Dockerfile.runtime`
  - 运行时镜像构建文件（推荐在服务器端使用）
  - 会把 `spider.jar`、`ai_service/`、`scripts/`、`docker/` 打进镜像
- `.dockerignore`
  - Docker 构建忽略规则，减少构建上下文体积（加速上传与构建）
- `docker-compose.runtime.yml`
  - 一键启动 `spider-suite`（使用外部 MySQL）
  - 会将宿主机 `/home/ubuntu/selenium/vnc` 挂载进容器，容器中的 VNC 由宿主机脚本控制
- `docker/entrypoint.sh`
  - 容器入口：生成 VNC 密码文件、生成 Selenium config.ini，然后启动 supervisord
- `docker/supervisord.conf`
  - 进程编排：VNC（通过宿主机脚本）、ai_service、spider
- `docker/server_build_runtime.sh`
  - 服务器侧构建脚本（可直接后台执行）
- `docker/server_up_runtime.sh`
  - 服务器侧启动脚本（默认端口映射到宿主机 8081/8000/5901）
- `docker/server_down_runtime.sh`
  - 服务器侧一键停止（`docker compose down`）
- `docker/server_status_runtime.sh`
  - 服务器侧一键查看：容器状态 / 端口映射 / 容器内进程（supervisord）/ 最新日志

## 文件关系（构建与启动链路）

构建阶段（生成镜像）：

1. 执行 `docker/server_build_runtime.sh`
2. 脚本调用 `docker compose -f docker-compose.runtime.yml build`
3. `docker-compose.runtime.yml` 指向 `Dockerfile.runtime` 作为构建文件
4. `Dockerfile.runtime` 将以下内容打包进镜像：
   - `/home/ubuntu/java_app/spider.jar`
   - `/home/ubuntu/ai_service/`（Python 服务）
   - `/home/ubuntu/scripts/`（容器内脚本）
   - `/etc/supervisor/conf.d/supervisord.conf`（来自 `docker/supervisord.conf`）
   - `/entrypoint.sh`（来自 `docker/entrypoint.sh`）
5. `.dockerignore` 用于减少构建上下文体积，避免无关文件影响构建与上传速度

启动阶段（运行容器）：

1. 执行 `docker/server_up_runtime.sh`
2. 脚本要求提供 `VNC_PASS`，并通过环境变量把宿主机端口映射为 `HOST_SPIDER_PORT/HOST_AI_PORT/HOST_VNC_PORT`
3. `docker-compose.runtime.yml` 负责：
   - 暴露端口：宿主机 `HOST_*` -> 容器 `8081/8000/5901`
   - 挂载数据卷：Chrome profile 与 supervisord 日志目录
   - 挂载宿主机目录：`/home/ubuntu/selenium/vnc`（容器内 VNC 实际调用宿主机脚本）
   - 传入外部 MySQL 连接配置：`SPRING_DATASOURCE_*`
4. 容器启动后执行 `docker/entrypoint.sh`：
   - 生成 VNC 密码文件
   - 生成/准备 Selenium 配置文件 `config.ini`
   - 启动 `supervisord`
5. `docker/supervisord.conf` 启动并守护以下进程：
   - VNC（通过挂载的宿主机脚本目录启动）
   - `ai_service`（Python/uvicorn）
   - `spider.jar`（Java，启动参数包含 `-Dselenuim.config=...`）

运维阶段（查看/停止）：

- `docker/server_status_runtime.sh`
  - `docker compose ps`：容器是否运行
  - `supervisorctl status`：容器内各进程状态
  - `docker compose port`：端口映射
  - `docker compose logs --tail 30`：最新日志
- `docker/server_down_runtime.sh`
  - `docker compose down`：停止并释放端口（默认保留数据卷）

## 已清理的历史文件

为避免混用导致误启用内部 MySQL/旧流程，已删除以下历史文件：

- `Dockerfile`
- `docker-compose.yml`

## 前置依赖（服务器）

- Docker Engine 已安装并可用
- Docker Compose v2（`docker compose version` 有输出）
- 宿主机已存在 VNC 脚本目录：`/home/ubuntu/selenium/vnc/`
  - 必须包含：`vnc_start_public.sh`、`vnc.sh`
  - 如果目录不存在或脚本不可执行，容器会启动失败（entrypoint 会检查）
- 外部 MySQL 可访问
  - 需要在启动时提供 `SPRING_DATASOURCE_URL/SPRING_DATASOURCE_USERNAME/SPRING_DATASOURCE_PASSWORD`
  - 示例：`jdbc:mysql://<mysql_host>:3306/<db>?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true`

## 构建镜像

进入服务器目录：

```bash
cd /home/ubuntu/docker/spider-suite
```

### 推荐：使用构建脚本

前台构建（方便看日志）：

```bash
PIP_INDEX_URL=https://mirrors.cloud.tencent.com/pypi/simple ./docker/server_build_runtime.sh
```

后台构建（耗时长时推荐）：

```bash
nohup env PIP_INDEX_URL=https://mirrors.cloud.tencent.com/pypi/simple \
  ./docker/server_build_runtime.sh > build_runtime.log 2>&1 & echo build_pid=$!
```

查看构建日志：

```bash
tail -f build_runtime.log
```

说明：
- 第一次构建通常耗时很长（apt/pip 下载依赖）
- 不建议日常使用 `--no-cache`（除非你希望强制全量重建）

## 启动服务

### 推荐：使用启动脚本

```bash
cd /home/ubuntu/docker/spider-suite
export VNC_PASS='QWER@1234'
export SPRING_DATASOURCE_URL='jdbc:mysql://<mysql_host>:3306/spider?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='your_password'
export SELENUIM_CONFIG='/home/ubuntu/selenium/config.ini'
./docker/server_up_runtime.sh
```

默认端口映射（宿主机）：
- Spider：`8081`
- AI：`8000`
- VNC：`5901`

启动后检查容器：

```bash
sudo docker ps
```

### 手动启动（不走脚本）

```bash
cd /home/ubuntu/docker/spider-suite
export VNC_PASS='QWER@1234'
export SPRING_DATASOURCE_URL='jdbc:mysql://<mysql_host>:3306/spider?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='your_password'
export SELENUIM_CONFIG='/home/ubuntu/selenium/config.ini'
sudo -E docker compose -f docker-compose.runtime.yml up -d
```

## 停止服务

停止并保留数据卷：

```bash
cd /home/ubuntu/docker/spider-suite
sudo docker compose -f docker-compose.runtime.yml down
```

停止并删除数据卷（会清空 Chrome profile、日志卷）：

```bash
cd /home/ubuntu/docker/spider-suite
sudo docker compose -f docker-compose.runtime.yml down -v
```

## 进入容器

```bash
sudo docker exec -it spider-suite-spider-suite-1 /bin/bash
```

如果没有 bash（极少数情况），改用：

```bash
sudo docker exec -it spider-suite-spider-suite-1 /bin/sh
```

## 如何更换 spider.jar

两种方式：

### 方式 A：替换构建上下文里的 jar 后重建镜像（推荐）

1. 将新 jar 覆盖到：`/home/ubuntu/docker/spider-suite/spider.jar`
2. 重新构建并重启：

```bash
cd /home/ubuntu/docker/spider-suite
./docker/server_build_runtime.sh
sudo docker compose -f docker-compose.runtime.yml up -d
```

### 方式 B：进入容器内替换（不推荐，重启会丢）

容器重建/重启可能覆盖文件，不建议这么做。

## 如何更换 ai_service（Python 应用）

### 推荐：替换构建上下文里的 ai_service 目录后重建

1. 更新：`/home/ubuntu/docker/spider-suite/ai_service/`
2. 重新构建并重启：

```bash
cd /home/ubuntu/docker/spider-suite
./docker/server_build_runtime.sh
sudo docker compose -f docker-compose.runtime.yml up -d
```

说明：
- Python 依赖由 `ai_service/requirements.txt` 决定，构建时会安装到 `/opt/venv`
- pip 源可通过 `PIP_INDEX_URL` 指定（例如腾讯云/清华）

## 如何更换/更新内部脚本

本工程涉及两类脚本：

### 1) 容器镜像内脚本（/home/ubuntu/scripts）

对应构建上下文：`/home/ubuntu/docker/spider-suite/scripts/`

更新后需要重建镜像：

```bash
cd /home/ubuntu/docker/spider-suite
./docker/server_build_runtime.sh
sudo docker compose -f docker-compose.runtime.yml up -d
```

### 2) 宿主机 VNC 脚本（/home/ubuntu/selenium/vnc）

容器会把宿主机目录挂载到容器同路径：

`/home/ubuntu/selenium/vnc -> /home/ubuntu/selenium/vnc`

因此你只要在宿主机更新脚本并确保可执行即可，无需重建镜像：

```bash
chmod +x /home/ubuntu/selenium/vnc/*.sh
```

VNC 的启动由 supervisord 调用：

`/home/ubuntu/selenium/vnc/vnc_start_public.sh`

## 常见问题

### 构建很慢是否正常？

正常。首次构建需要：
- apt 安装 Chromium / VNC 组件 / Java
- pip 安装 FastAPI、pandas、scikit-learn、xgboost 等依赖

建议：
- 日常构建不要加 `--no-cache`
- 通过 `PIP_INDEX_URL` 使用镜像源
- 通过 `nohup` 后台构建并写日志

### 端口冲突怎么办？

如果宿主机已经占用 `8081/8000/5901`，需要先停止宿主机同类服务，或改 compose 端口映射。
