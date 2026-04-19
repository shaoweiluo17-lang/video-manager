# ================================================================
# 视频图片文件管理系统 - 后端 Dockerfile
# ================================================================

# 1. 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 复制 pom.xml
COPY pom.xml .

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests -B

# 2. 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建数据目录
RUN mkdir -p /app/data /app/data/thumbnails

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 设置环境变量默认值
ENV JWT_SECRET=video-manager-jwt-secret-key-2026
ENV JWT_EXPIRATION=86400000
ENV DB_PATH=/app/data/video-manager.db
ENV THUMBNAIL_DIR=/app/data/thumbnails
ENV SCAN_VIDEO_EXTENSIONS=.mp4,.avi,.mkv,.mov,.wmv,.flv,.webm,.m4v
ENV SCAN_IMAGE_EXTENSIONS=.jpg,.jpeg,.png,.gif,.bmp,.webp,.svg,.heic

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/check || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
