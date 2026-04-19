# Video Manager 飞牛NAS部署方案

## 目录
- [环境要求](#环境要求)
- [本地打包](#本地打包)
- [上传到NAS](#上传到nas)
- [NAS部署步骤](#nas部署步骤)
- [启动服务](#启动服务)
- [常见问题](#常见问题)

---

## 环境要求

### 本地开发环境
- Node.js 18+ (用于前端打包)
- JDK 17+ (用于后端打包)
- Maven 3.6+ (用于后端打包)

### 飞牛NAS环境
- JDK 17 (已安装)
- MySQL 8.0+
- Python 3.x (用于启动前端静态服务)

---

## 本地打包

### 1. 打包后端 (Spring Boot JAR)

在项目根目录执行：

```bash
# 进入后端目录
cd backend

# Maven打包（跳过测试）
mvn clean package -DskipTests

# 打包完成后，JAR文件位于：
# backend/target/video-manager-1.0.0.jar
```

### 2. 打包前端 (Vue静态文件)

```bash
# 进入前端目录
cd frontend

# 安装依赖（如果还没安装）
npm install

# 生产环境打包
npm run build

# 打包完成后，静态文件位于：
# frontend/dist/
```

### 3. 准备部署文件

创建部署目录结构：

```
deploy/
├── video-manager-1.0.0.jar    # 后端JAR
├── dist/                       # 前端静态文件
│   ├── index.html
│   ├── assets/
│   └── ...
├── config/
│   └── application.yml         # 配置文件(可选)
├── start-backend.sh            # 后端启动脚本
├── start-frontend.py           # 前端启动脚本(Python)
└── start-all.sh                # 一键启动脚本
```

---

## 上传到NAS

### 方式一：使用SCP上传

```bash
# 在本地执行，将整个deploy目录上传到NAS
scp -r deploy/ user@nas-ip:/home/user/video-manager/
```

### 方式二：使用SFTP工具

使用FileZilla、WinSCP等工具，将deploy目录上传到NAS的 `/home/user/video-manager/` 目录。

### 方式三：使用rsync (推荐)

```bash
rsync -avz --progress deploy/ user@nas-ip:/home/user/video-manager/
```

---

## NAS部署步骤

### 1. 数据库准备

登录MySQL，创建数据库和用户：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS video_manager 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选，如果使用root可跳过）
CREATE USER 'video_manager'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON video_manager.* TO 'video_manager'@'%';
FLUSH PRIVILEGES;
```

执行初始化SQL脚本（如果需要）：

```bash
mysql -u root -p video_manager < schema-mysql.sql
```

### 2. 配置后端

编辑 `application.yml` 或使用环境变量：

```yaml
# 修改数据库连接信息
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/video_manager?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: your_mysql_password

# 修改JWT密钥（重要！）
jwt:
  secret: your-secure-jwt-secret-key-at-least-64-characters-long-for-production

# 修改缩略图存储路径
scan:
  thumbnail-dir: /home/user/video-manager/data/thumbnails
```

或使用环境变量启动：

```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=video_manager
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_password
export JWT_SECRET=your-secure-jwt-secret-key
export THUMBNAIL_DIR=/home/user/video-manager/data/thumbnails
```

### 3. 创建启动脚本

#### 后端启动脚本 `start-backend.sh`

```bash
#!/bin/bash

# 工作目录
WORK_DIR="/home/user/video-manager"
JAR_FILE="video-manager-1.0.0.jar"
LOG_FILE="backend.log"
PID_FILE="backend.pid"

cd $WORK_DIR

# 检查是否已运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null 2>&1; then
        echo "后端服务已在运行 (PID: $PID)"
        exit 0
    fi
fi

# 启动后端
echo "正在启动后端服务..."
nohup java -Xms256m -Xmx512m \
    -Dspring.profiles.active=prod \
    -jar $JAR_FILE \
    > $LOG_FILE 2>&1 &

echo $! > $PID_FILE
echo "后端服务已启动 (PID: $(cat $PID_FILE))"
echo "日志文件: $WORK_DIR/$LOG_FILE"
```

#### 前端启动脚本 `start-frontend.py`

```python
#!/usr/bin/env python3
"""
使用Python HTTP服务器启动前端静态文件
支持SPA路由（Vue Router history模式）
"""

import http.server
import socketserver
import os
import sys
from urllib.parse import urlparse

PORT = 3000
DIRECTORY = "dist"

class SPAHandler(http.server.SimpleHTTPRequestHandler):
    """支持SPA路由的HTTP请求处理器"""
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def do_GET(self):
        """处理GET请求，支持SPA路由"""
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        # 静态资源直接返回
        if path.startswith('/assets/') or path.startswith('/favicon'):
            return super().do_GET()
        
        # 检查文件是否存在
        file_path = os.path.join(DIRECTORY, path.lstrip('/'))
        if os.path.isfile(file_path):
            return super().do_GET()
        
        # SPA路由：返回index.html
        self.path = '/index.html'
        return super().do_GET()
    
    def end_headers(self):
        # 添加CORS头
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', '*')
        super().end_headers()

def main():
    # 切换到脚本所在目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)
    
    # 检查dist目录
    if not os.path.exists(DIRECTORY):
        print(f"错误: 找不到 {DIRECTORY} 目录")
        print("请确保前端已打包并上传")
        sys.exit(1)
    
    with socketserver.TCPServer(("", PORT), SPAHandler) as httpd:
        print(f"前端服务已启动")
        print(f"访问地址: http://localhost:{PORT}")
        print(f"静态文件目录: {os.path.abspath(DIRECTORY)}")
        print("按 Ctrl+C 停止服务")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n服务已停止")

if __name__ == "__main__":
    main()
```

#### 一键启动脚本 `start-all.sh`

```bash
#!/bin/bash

WORK_DIR="/home/user/video-manager"
cd $WORK_DIR

echo "=========================================="
echo "  Video Manager 启动脚本"
echo "=========================================="

# 启动后端
echo ""
echo "[1/2] 启动后端服务..."
./start-backend.sh

# 等待后端启动
sleep 3

# 启动前端
echo ""
echo "[2/2] 启动前端服务..."
nohup python3 start-frontend.py > frontend.log 2>&1 &
echo $! > frontend.pid
echo "前端服务已启动 (PID: $(cat frontend.pid))"

echo ""
echo "=========================================="
echo "  服务启动完成!"
echo "=========================================="
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:3000"
echo ""
echo "日志文件:"
echo "  - 后端: $WORK_DIR/backend.log"
echo "  - 前端: $WORK_DIR/frontend.log"
echo ""
echo "停止服务: ./stop-all.sh"
```

#### 停止脚本 `stop-all.sh`

```bash
#!/bin/bash

WORK_DIR="/home/user/video-manager"
cd $WORK_DIR

echo "正在停止服务..."

# 停止后端
if [ -f "backend.pid" ]; then
    PID=$(cat backend.pid)
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        echo "后端服务已停止 (PID: $PID)"
    fi
    rm -f backend.pid
fi

# 停止前端
if [ -f "frontend.pid" ]; then
    PID=$(cat frontend.pid)
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        echo "前端服务已停止 (PID: $PID)"
    fi
    rm -f frontend.pid
fi

echo "所有服务已停止"
```

### 4. 设置脚本权限

```bash
chmod +x start-backend.sh
chmod +x start-all.sh
chmod +x stop-all.sh
chmod +x start-frontend.py
```

---

## 启动服务

### 方式一：一键启动

```bash
cd /home/user/video-manager
./start-all.sh
```

### 方式二：分别启动

```bash
cd /home/user/video-manager

# 启动后端
./start-backend.sh

# 启动前端（新终端）
python3 start-frontend.py
```

### 访问应用

- 前端界面: `http://nas-ip:3000`
- 后端API: `http://nas-ip:8080`

---

## 前端API代理配置（可选）

如果需要前端请求自动代理到后端，可以修改 `start-frontend.py`：

```python
#!/usr/bin/env python3
"""
带API代理的前端服务器
"""

import http.server
import socketserver
import os
import sys
import urllib.request
from urllib.parse import urlparse

FRONTEND_PORT = 3000
BACKEND_PORT = 8080
DIRECTORY = "dist"

class ProxyHandler(http.server.SimpleHTTPRequestHandler):
    """支持API代理和SPA路由"""
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def do_GET(self):
        self.handle_request()
    
    def do_POST(self):
        self.handle_request()
    
    def do_PUT(self):
        self.handle_request()
    
    def do_DELETE(self):
        self.handle_request()
    
    def handle_request(self):
        path = urlparse(self.path).path
        
        # API请求代理到后端
        if path.startswith('/api/'):
            self.proxy_request()
            return
        
        # 静态资源
        if path.startswith('/assets/') or path == '/favicon.ico':
            return super().do_GET()
        
        # SPA路由
        file_path = os.path.join(DIRECTORY, path.lstrip('/'))
        if os.path.isfile(file_path):
            return super().do_GET()
        
        self.path = '/index.html'
        return super().do_GET()
    
    def proxy_request(self):
        """代理请求到后端"""
        backend_url = f"http://localhost:{BACKEND_PORT}{self.path}"
        
        try:
            # 转发请求
            req = urllib.request.Request(backend_url, method=self.command)
            
            # 复制请求头
            for header in self.headers:
                if header.lower() not in ['host', 'connection']:
                    req.add_header(header, self.headers[header])
            
            # 发送请求
            with urllib.request.urlopen(req) as response:
                self.send_response(response.status)
                for header in response.headers:
                    self.send_header(header, response.headers[header])
                self.end_headers()
                self.wfile.write(response.read())
        except Exception as e:
            self.send_error(502, f"Backend error: {str(e)}")

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)
    
    if not os.path.exists(DIRECTORY):
        print(f"错误: 找不到 {DIRECTORY} 目录")
        sys.exit(1)
    
    with socketserver.TCPServer(("", FRONTEND_PORT), ProxyHandler) as httpd:
        print(f"前端服务: http://localhost:{FRONTEND_PORT}")
        print(f"API代理到: http://localhost:{BACKEND_PORT}")
        httpd.serve_forever()

if __name__ == "__main__":
    main()
```

---

## 开机自启动（可选）

### 使用systemd服务

创建后端服务文件 `/etc/systemd/system/video-manager-backend.service`:

```ini
[Unit]
Description=Video Manager Backend
After=network.target mysql.service

[Service]
Type=simple
User=your-user
WorkingDirectory=/home/your-user/video-manager
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar video-manager-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

创建前端服务文件 `/etc/systemd/system/video-manager-frontend.service`:

```ini
[Unit]
Description=Video Manager Frontend
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/home/your-user/video-manager
ExecStart=/usr/bin/python3 start-frontend.py
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable video-manager-backend
sudo systemctl enable video-manager-frontend
sudo systemctl start video-manager-backend
sudo systemctl start video-manager-frontend
```

---

## 常见问题

### 1. 端口被占用

```bash
# 查看端口占用
netstat -tlnp | grep 8080
netstat -tlnp | grep 3000

# 杀掉占用进程
kill -9 <PID>
```

### 2. 数据库连接失败

- 检查MySQL服务是否启动
- 检查数据库用户名密码是否正确
- 检查数据库是否存在
- 检查防火墙是否开放3306端口

### 3. 前端无法访问后端API

- 检查后端是否正常启动
- 检查CORS配置
- 使用带代理的Python服务器版本

### 4. 缩略图无法生成

- 检查 `thumbnail-dir` 目录权限
- 确保目录存在且有写入权限

```bash
mkdir -p /home/user/video-manager/data/thumbnails
chmod 755 /home/user/video-manager/data/thumbnails
```

### 5. 查看日志

```bash
# 后端日志
tail -f /home/user/video-manager/backend.log

# 前端日志
tail -f /home/user/video-manager/frontend.log
```

---

## 部署文件清单

上传到NAS的完整文件列表：

```
/home/user/video-manager/
├── video-manager-1.0.0.jar     # 后端JAR包
├── dist/                        # 前端静态文件
│   ├── index.html
│   ├── assets/
│   │   ├── index-xxx.js
│   │   ├── index-xxx.css
│   │   └── ...
│   └── favicon.ico
├── data/
│   └── thumbnails/              # 缩略图目录
├── start-backend.sh             # 后端启动脚本
├── start-frontend.py            # 前端启动脚本
├── start-all.sh                 # 一键启动
├── stop-all.sh                  # 停止脚本
├── backend.log                  # 后端日志
├── frontend.log                 # 前端日志
├── backend.pid                  # 后端PID文件
└── frontend.pid                 # 前端PID文件
```

---

## 快速部署命令汇总

```bash
# 1. 本地打包
cd backend && mvn clean package -DskipTests
cd ../frontend && npm install && npm run build

# 2. 创建部署目录
mkdir -p deploy/dist deploy/data/thumbnails
cp backend/target/video-manager-1.0.0.jar deploy/
cp -r frontend/dist/* deploy/dist/

# 3. 上传到NAS
rsync -avz deploy/ user@nas-ip:/home/user/video-manager/

# 4. NAS上执行
cd /home/user/video-manager
chmod +x *.sh *.py
./start-all.sh
```

部署完成！访问 `http://nas-ip:3000` 即可使用。
