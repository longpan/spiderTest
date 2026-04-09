# scripts 使用说明

本目录包含在云服务器上启动/停止 VNC 桌面、打开 Chrome 登录页以及启动/停止爬虫后端服务的运维脚本。脚本均为无交互设计，适合被 OpenClaw 等机器人在聊天中直接触发。

## 目录与脚本

- vnc.sh
  - 核心控制脚本：`start|stop|restart|status|free-profile`
  - 功能：启动/停止 Xvfb(:99)、fluxbox、x11vnc，必要时自动打开 Chrome 并复用指定用户数据目录（Profile）
  - 关键行为：
    - 启动前清理 `/tmp/.X99-lock` 与 `/tmp/.X11-unix/X99`，避免残留锁导致 Xvfb 启不来
    - 若以 root 调用，会自动切换为 `ubuntu` 用户运行（需 sudo 免密）
    - 后台进程通过 `setsid` 脱离会话，避免被上游进程的 SIGHUP 影响

- vnc_start_public.sh
  - 简化启动入口：相当于执行 `vnc.sh start`
  - 默认会打开的登录页：
    - `https://xyq.cbg.163.com/cgi-bin/show_login.py?act=show_login&area_id=39&area_name=%E5%8D%8E%E5%8D%97%E5%8C%BA&server_id=625&server_name=%E9%92%93%E9%B1%BC%E5%B2%9B`
  - 可覆盖的环境变量：
    - `CHROME_URL` 指定启动后打开的 URL（脚本会自动去除反引号和多余空格）

- vnc_stop.sh / vnc_stop_public.sh
  - 停止 VNC 栈（x11vnc/fluxbox/Xvfb）。`vnc_stop_public.sh` 是 vnc.sh stop 的薄封装

- vnc_clear_profile.sh
  - 清理 Chrome Profile 的占用与锁文件：结束相关 chrome/chromedriver 进程，删除 `Singleton*`，并修复属主与权限（chown/chmod 为 ubuntu）
  - 适合在抓取/登录前或异常后执行一遍，避免因 Profile 被 root 写入导致后续以 ubuntu 运行时解析失败

- start_spider.sh
  - 启动 Java 爬虫后端（Spring Boot，端口默认 8081）
  - 行为：
    - 停掉旧进程（优雅等待后再 SIGKILL）
    - 使用 `setsid -f` 后台启动，立即返回
    - 若由 root 执行，会切换为 `ubuntu` 用户拉起
    - 启动后打印新 PID、端口监听状态与 spider.log 尾部内容

- stop_spider.sh
  - 停止 Java 爬虫后端。若存在免密 sudo，会使用 `sudo -n kill` 停掉 root/ubuntu 启动的进程

## 环境依赖

- 系统：Linux（无桌面环境）
- 基础软件：
  - `Xvfb`、`x11vnc`、`fluxbox`
  - `google-chrome`（或系统已安装的 Chrome/Chromium）
  - `chromedriver` 与 Chrome 主版本匹配
  - `sudo`（配置免密以便脚本在 OpenClaw 下运行）
- 目录与权限：
  - VNC 基础目录：`/home/ubuntu/selenium`
  - Chrome Profile：`/home/ubuntu/selenium/chrome-profile`（属主 ubuntu，权限 u+rwX,go-rwx）
  - Java 后端目录：`/home/ubuntu/java_app`

## 常用环境变量

对 vnc.sh（及其封装脚本）有效：

- `VNC_PASS`：x11vnc 密码（首次会写入 `/home/ubuntu/selenium/.vncpass`）
- `CHROME_URL`：Chrome 打开页面（默认为藏宝阁登录页）
- `CHROME_PROFILE_DIR`：Chrome 用户数据目录（默认 `/home/ubuntu/selenium/chrome-profile`）
- `DISPLAY_NUM`：Xvfb 显示号（默认 99）
- `RFB_PORT`：VNC 端口（默认 5901）
- `OPEN_CHROME`：是否自动启动 Chrome（默认 true）

对 start_spider.sh / stop_spider.sh 有效：

- `PORT`：Spring Boot 监听端口（默认 8081，仅用于状态提示）

## 使用示例

启动 VNC（并打开登录页）：

```bash
VNC_PASS='QWER@1234' /home/ubuntu/selenium/vnc/vnc_start_public.sh
```

停止 VNC：

```bash
/home/ubuntu/selenium/vnc/vnc_stop_public.sh
```

清理 Profile 锁（推荐在异常后执行）：

```bash
/home/ubuntu/selenium/vnc/vnc_clear_profile.sh
```

启动后端服务：

```bash
/home/ubuntu/java_app/start_spider.sh
```

停止后端服务：

```bash
/home/ubuntu/java_app/stop_spider.sh
```

## OpenClaw/飞书集成建议

- 在 OpenClaw 中开启 Elevated Tool Access，并在 “Elevated Tool Allow Rules” 将飞书私聊 `ou_xxx` 加入白名单
- Exec Ask 选择 `on-miss` 或 `off`，避免飞书里提示需要审批
- 推荐把上述脚本绑定为 Slash Commands（如 `/vnc_start`、`/vnc_stop`、`/vnc_clear`、`/spider_start`、`/spider_stop`）

## 故障排查

- `XOpenDisplay(":99") failed` / `Could not create server lock file: /tmp/.X99-lock`：
  - 执行 `vnc_stop_public.sh` 后再执行 `vnc_start_public.sh`
  - 或直接 `vnc.sh stop`、`vnc.sh start`；脚本已内置清理 `/tmp/.X99-lock` 与 `/tmp/.X11-unix/X99`
- Chrome 启动时报 `cannot parse internal JSON template: EOF`：
  - 执行 `vnc_clear_profile.sh` 修复 Profile 属主与锁文件（多由 root 写入导致 ubuntu 无法读取）
- 后端服务启动后飞书无返回：
  - `start_spider.sh` 已后台化并立即返回，查看输出的 PID/端口/日志信息判断是否成功

## 安全建议

- 仅在受限网络环境下暴露 VNC 端口（优先 SSH 隧道）
- 限定 OpenClaw 的执行白名单为上述固定脚本，避免任意命令执行
- 确保 Chrome/ChromeDriver 主版本匹配，并保持更新

