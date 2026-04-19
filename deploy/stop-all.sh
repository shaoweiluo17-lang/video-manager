#!/bin/bash

# ============================================================
# Video Manager 停止脚本
# 用途: 停止所有服务
# ============================================================

# 工作目录
WORK_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$WORK_DIR" || exit 1

# PID文件
BACKEND_PID_FILE="backend.pid"
FRONTEND_PID_FILE="frontend.pid"

echo "============================================================"
echo "  Video Manager 停止脚本"
echo "============================================================"
echo ""

# 停止后端
if [ -f "$BACKEND_PID_FILE" ]; then
    PID=$(cat "$BACKEND_PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "正在停止后端服务 (PID: $PID)..."
        kill "$PID"
        sleep 1
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "强制停止后端服务..."
            kill -9 "$PID"
        fi
        echo "✓ 后端服务已停止"
    else
        echo "后端服务未运行"
    fi
    rm -f "$BACKEND_PID_FILE"
else
    echo "后端服务未运行 (未找到PID文件)"
fi

echo ""

# 停止前端
if [ -f "$FRONTEND_PID_FILE" ]; then
    PID=$(cat "$FRONTEND_PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "正在停止前端服务 (PID: $PID)..."
        kill "$PID"
        sleep 1
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "强制停止前端服务..."
            kill -9 "$PID"
        fi
        echo "✓ 前端服务已停止"
    else
        echo "前端服务未运行"
    fi
    rm -f "$FRONTEND_PID_FILE"
else
    echo "前端服务未运行 (未找到PID文件)"
fi

echo ""
echo "============================================================"
echo "  所有服务已停止"
echo "============================================================"
