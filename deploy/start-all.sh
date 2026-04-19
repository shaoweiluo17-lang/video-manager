#!/bin/bash

# ============================================================
# Video Manager 一键启动脚本
# 用途: 同时启动后端和前端服务
# ============================================================

# ------------------------------------------------------------
# 配置区域
# ------------------------------------------------------------

# 工作目录 (脚本所在目录)
WORK_DIR="$(cd "$(dirname "$0")" && pwd)"

# 后端启动脚本
BACKEND_SCRIPT="start-backend.sh"

# 前端启动脚本
FRONTEND_SCRIPT="start-frontend.py"

# 前端日志文件
FRONTEND_LOG="frontend.log"

# 前端PID文件
FRONTEND_PID_FILE="frontend.pid"

# 等待后端启动的时间(秒)
WAIT_BACKEND=3

# ------------------------------------------------------------
# 辅助函数
# ------------------------------------------------------------

# 打印标题
print_title() {
    echo ""
    echo "============================================================"
    echo "  $1"
    echo "============================================================"
}

# 打印分隔线
print_line() {
    echo "------------------------------------------------------------"
}

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "错误: 未找到 $1 命令"
        return 1
    fi
    return 0
}

# ------------------------------------------------------------
# 主逻辑
# ------------------------------------------------------------

# 进入工作目录
cd "$WORK_DIR" || exit 1

# 打印欢迎信息
print_title "Video Manager 启动脚本"

# ------------------------------------------------------------
# 环境检查
# ------------------------------------------------------------

echo ""
echo "[环境检查]"

# 检查Java
if ! check_command java; then
    echo "请安装JDK 17或更高版本"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
echo "✓ Java版本: $JAVA_VERSION"

# 检查Python
# 尝试多种方式查找Python
PYTHON_CMD=""
NEED_SUDO=false

# 首先尝试直接使用python3
if command -v python3 &> /dev/null; then
    if python3 --version &> /dev/null; then
        PYTHON_CMD="python3"
    else
        # python3存在但无执行权限，尝试sudo
        if sudo -n python3 --version &> /dev/null 2>&1; then
            PYTHON_CMD="sudo python3"
            NEED_SUDO=true
        fi
    fi
fi

# 如果python3不可用，尝试python
if [ -z "$PYTHON_CMD" ]; then
    if command -v python &> /dev/null; then
        if python --version &> /dev/null; then
            PYTHON_CMD="python"
        else
            if sudo -n python --version &> /dev/null 2>&1; then
                PYTHON_CMD="sudo python"
                NEED_SUDO=true
            fi
        fi
    fi
fi

# 如果还是找不到，报错
if [ -z "$PYTHON_CMD" ]; then
    echo "错误: 未找到可用的Python"
    echo "请确保Python已安装且有执行权限"
    echo "或使用root账号运行此脚本"
    exit 1
fi

# 获取Python版本
if [ "$NEED_SUDO" = true ]; then
    PYTHON_VERSION=$(sudo python3 --version 2>&1 || sudo python --version 2>&1 || echo "Python 3.x")
    echo "✓ $PYTHON_VERSION (使用sudo)"
else
    PYTHON_VERSION=$($PYTHON_CMD --version 2>&1)
    echo "✓ $PYTHON_VERSION"
fi

# 检查启动脚本
if [ ! -f "$BACKEND_SCRIPT" ]; then
    echo "错误: 找不到后端启动脚本 $BACKEND_SCRIPT"
    exit 1
fi

if [ ! -f "$FRONTEND_SCRIPT" ]; then
    echo "错误: 找不到前端启动脚本 $FRONTEND_SCRIPT"
    exit 1
fi

echo "✓ 启动脚本检查通过"

# ------------------------------------------------------------
# 启动后端
# ------------------------------------------------------------

echo ""
print_title "步骤 1/2: 启动后端服务"
print_line

# 执行后端启动脚本
bash "$BACKEND_SCRIPT"
BACKEND_RESULT=$?

if [ $BACKEND_RESULT -ne 0 ]; then
    echo ""
    echo "✗ 后端启动失败，请检查日志"
    exit 1
fi

# 等待后端启动
echo ""
echo "等待后端服务初始化..."
sleep $WAIT_BACKEND

# ------------------------------------------------------------
# 启动前端
# ------------------------------------------------------------

echo ""
print_title "步骤 2/2: 启动前端服务"
print_line

# 检查前端是否已运行
if [ -f "$FRONTEND_PID_FILE" ]; then
    OLD_PID=$(cat "$FRONTEND_PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "前端服务已在运行 (PID: $OLD_PID)"
    else
        rm -f "$FRONTEND_PID_FILE"
    fi
fi

# 启动前端
if [ ! -f "$FRONTEND_PID_FILE" ]; then
    echo "正在启动前端服务..."
    
    # 使用nohup在后台运行
    nohup $PYTHON_CMD "$FRONTEND_SCRIPT" > "$FRONTEND_LOG" 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > "$FRONTEND_PID_FILE"
    
    # 等待前端启动
    sleep 1
    
    # 检查是否启动成功
    if ps -p "$FRONTEND_PID" > /dev/null 2>&1; then
        echo "✓ 前端服务启动成功"
        print_line
        echo "PID: $FRONTEND_PID"
        echo "端口: 3000"
        echo "日志: $WORK_DIR/$FRONTEND_LOG"
    else
        echo "✗ 前端服务启动失败"
        echo "请查看日志: $FRONTEND_LOG"
        rm -f "$FRONTEND_PID_FILE"
        exit 1
    fi
fi

# ------------------------------------------------------------
# 启动完成
# ------------------------------------------------------------

echo ""
print_title "启动完成"
print_line

# 获取NAS IP地址 (尝试获取)
NAS_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
if [ -z "$NAS_IP" ]; then
    NAS_IP="localhost"
fi

echo ""
echo "服务地址:"
echo "  前端界面: http://$NAS_IP:3000"
echo "  后端API:  http://$NAS_IP:8080"
echo ""
echo "日志文件:"
echo "  后端: $WORK_DIR/backend.log"
echo "  前端: $WORK_DIR/$FRONTEND_LOG"
echo ""
echo "管理命令:"
echo "  查看后端日志: tail -f backend.log"
echo "  查看前端日志: tail -f $FRONTEND_LOG"
echo "  停止所有服务: ./stop-all.sh"
echo ""
print_line
echo "提示: 首次访问请在浏览器打开 http://$NAS_IP:3000"
print_line
