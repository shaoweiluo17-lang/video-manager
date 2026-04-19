#!/bin/bash

# ============================================================
# Video Manager 后端启动脚本
# 用途: 启动Spring Boot后端服务
# ============================================================

# ------------------------------------------------------------
# 配置区域 - 请根据实际环境修改
# ------------------------------------------------------------

# 工作目录 (脚本所在目录)
WORK_DIR="$(cd "$(dirname "$0")" && pwd)"

# JAR文件名
JAR_FILE="video-manager-1.0.0.jar"

# 日志文件
LOG_FILE="backend.log"

# PID文件 (用于记录进程ID)
PID_FILE="backend.pid"

# JVM参数
# -Xms: 初始堆内存大小
# -Xmx: 最大堆内存大小
# 硬件环境: 飞牛NAS N305+ 48G内存 空余30G
# 配置策略: 为应用分配2G堆内存，剩余内存留给系统和其他服务
# 2G堆内存足够处理大量媒体文件扫描和缩略图生成
JVM_OPTS="-Xms2g -Xmx2g"

# Spring Profile (生产环境使用prod)
SPRING_PROFILE="prod"

# ------------------------------------------------------------
# 脚本逻辑 - 通常无需修改
# ------------------------------------------------------------

# 进入工作目录
cd "$WORK_DIR" || exit 1

# 打印分隔线
print_line() {
    echo "------------------------------------------------------------"
}

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 找不到JAR文件 $JAR_FILE"
    echo "请确保已将 video-manager-1.0.0.jar 放在当前目录"
    exit 1
fi

# 检查服务是否已运行
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "后端服务已在运行"
        print_line
        echo "PID: $OLD_PID"
        echo "日志: $WORK_DIR/$LOG_FILE"
        echo ""
        echo "如需重启，请先执行: ./stop-all.sh"
        exit 0
    else
        # PID文件存在但进程不存在，清理PID文件
        rm -f "$PID_FILE"
    fi
fi

# 创建日志目录
mkdir -p logs

# ------------------------------------------------------------
# 启动服务
# ------------------------------------------------------------

echo "正在启动 Video Manager 后端服务..."
print_line

# 构建启动命令
# 使用nohup在后台运行，输出重定向到日志文件
START_CMD="nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILE \
    -Dfile.encoding=UTF-8 \
    -jar $JAR_FILE \
    > $LOG_FILE 2>&1 &"

# 执行启动命令
eval $START_CMD

# 获取并保存PID
BACKEND_PID=$!
echo $BACKEND_PID > "$PID_FILE"

# 等待服务启动
sleep 2

# 检查服务是否启动成功
if ps -p "$BACKEND_PID" > /dev/null 2>&1; then
    echo "✓ 后端服务启动成功"
    print_line
    echo "PID: $BACKEND_PID"
    echo "端口: 8080"
    echo "日志: $WORK_DIR/$LOG_FILE"
    echo "配置: $WORK_DIR/config/application.yml"
    echo ""
    echo "查看实时日志: tail -f $LOG_FILE"
    echo "停止服务: kill $BACKEND_PID 或 ./stop-all.sh"
else
    echo "✗ 后端服务启动失败"
    echo "请查看日志文件: $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
