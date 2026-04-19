# MySQL 数据库配置指南

> 本项目已配置为使用MySQL数据库，以下是完整的配置和部署指南。

---

## 📋 前置条件

- ✅ MySQL 8.0+ 已安装并运行（您已在NAS上使用Docker安装）
- ✅ MySQL服务可访问
- ✅ 项目无历史数据，无需迁移

---

## 🚀 快速开始

### 步骤1: 创建数据库和用户

连接到MySQL（使用root用户）：

```bash
# 方式1: 使用MySQL客户端
mysql -h <NAS_IP> -P 3306 -u root -p

# 方式2: 使用Docker exec（如果MySQL在Docker中）
docker exec -it <mysql-container-name> mysql -u root -p
```

执行创建脚本：

```sql
-- 方式1: 手动执行
CREATE DATABASE IF NOT EXISTS video_manager 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'videomanager'@'%' IDENTIFIED BY 'your-secure-password';

GRANT ALL PRIVILEGES ON video_manager.* TO 'videomanager'@'%';

FLUSH PRIVILEGES;
```

或使用提供的脚本：

```bash
mysql -h <NAS_IP> -P 3306 -u root -p < backend/src/main/resources/create-database-and-user.sql
```

### 步骤2: 初始化数据库表结构

```bash
mysql -h <NAS_IP> -P 3306 -u videomanager -p video_manager < backend/src/main/resources/schema-mysql.sql
```

### 步骤3: 配置应用连接

修改 `backend/src/main/resources/application.yml` 或使用环境变量：

**方式1: 修改配置文件**

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://<NAS_IP>:3306/video_manager?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: videomanager
    password: your-secure-password
```

**方式2: 使用环境变量（推荐）**

```bash
export MYSQL_HOST=<NAS_IP>
export MYSQL_PORT=3306
export MYSQL_DATABASE=video_manager
export MYSQL_USERNAME=videomanager
export MYSQL_PASSWORD=your-secure-password
```

### 步骤4: 启动应用

```bash
# 编译项目
cd backend
mvn clean package

# 启动应用
java -jar target/video-manager-1.0.0.jar
```

---

## 🔧 详细配置说明

### MySQL连接配置

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|---------|--------|------|
| 主机地址 | MYSQL_HOST | localhost | MySQL服务器地址 |
| 端口 | MYSQL_PORT | 3306 | MySQL端口 |
| 数据库名 | MYSQL_DATABASE | video_manager | 数据库名称 |
| 用户名 | MYSQL_USERNAME | videomanager | 数据库用户名 |
| 密码 | MYSQL_PASSWORD | your-password | 数据库密码 |

### JDBC URL参数说明

```
jdbc:mysql://host:port/database?
  useUnicode=true              # 使用Unicode编码
  &characterEncoding=utf8mb4   # 字符集utf8mb4
  &serverTimezone=Asia/Shanghai # 时区设置
  &allowPublicKeyRetrieval=true # 允许公钥检索
  &useSSL=false                # 禁用SSL（内网可禁用）
```

---

## 🐳 Docker环境配置

如果MySQL运行在Docker中，需要确保网络连通：

### 方式1: 同一Docker网络

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    container_name: mysql
    networks:
      - app-network
    
  video-manager:
    build: ./backend
    environment:
      - MYSQL_HOST=mysql  # 使用容器名
      - MYSQL_PORT=3306
    networks:
      - app-network
    depends_on:
      - mysql

networks:
  app-network:
    driver: bridge
```

### 方式2: 使用宿主机IP

```bash
# 获取宿主机IP（Linux）
ip addr show docker0 | grep inet

# 获取宿主机IP（Windows Docker Desktop）
# 使用 host.docker.internal

export MYSQL_HOST=host.docker.internal  # Docker Desktop
# 或
export MYSQL_HOST=192.168.1.100        # 宿主机实际IP
```

### 方式3: 端口映射

确保MySQL容器端口已映射：

```yaml
services:
  mysql:
    ports:
      - "3306:3306"  # 映射到宿主机
```

然后使用宿主机IP连接：

```bash
export MYSQL_HOST=<NAS_IP或宿主机IP>
```

---

## 📊 数据库表结构

### 表清单

| 表名 | 说明 | 预估数据量 |
|------|------|-----------|
| user | 用户表 | 极少（1-10条） |
| scan_path | 扫描路径配置 | 极少（5-20条） |
| folder | 文件夹信息 | 少量（数十~数百条） |
| video | 视频文件元数据 | 大量（数千~百万条） |
| image | 图片文件元数据 | 大量（数千~百万条） |

### 索引策略

每个表都配置了以下索引：

- **主键索引**: `id`（自增）
- **唯一索引**: `file_path`（文件路径唯一）
- **查询索引**: `hash`, `dislike`, `folder_id`, `created_at`
- **外键约束**: `folder_id` 关联 `folder`表

---

## 🔐 安全建议

### 生产环境配置

1. **修改默认密码**
   ```sql
   ALTER USER 'videomanager'@'%' IDENTIFIED BY 'strong-secure-password';
   FLUSH PRIVILEGES;
   ```

2. **限制访问IP**
   ```sql
   -- 只允许特定IP访问
   CREATE USER 'videomanager'@'192.168.1.%' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON video_manager.* TO 'videomanager'@'192.168.1.%';
   ```

3. **启用SSL（可选）**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://host:3306/db?useSSL=true&requireSSL=true
   ```

4. **使用配置中心**
   - 不要在代码中硬编码密码
   - 使用环境变量或配置中心（如Spring Cloud Config）

---

## 📈 性能优化建议

### MySQL配置优化

编辑MySQL配置文件 `my.cnf` 或 `my.ini`：

```ini
[mysqld]
# InnoDB配置
innodb_buffer_pool_size = 1G        # 缓冲池大小（建议物理内存的50-70%）
innodb_log_file_size = 256M         # 日志文件大小
innodb_flush_log_at_trx_commit = 2  # 日志刷新策略

# 连接配置
max_connections = 200               # 最大连接数
wait_timeout = 600                  # 连接超时时间

# 查询缓存（MySQL 8.0已移除，使用应用层缓存）
# query_cache_size = 64M

# 临时表
tmp_table_size = 64M
max_heap_table_size = 64M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

### 应用层优化

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时
      idle-timeout: 600000         # 空闲超时
      max-lifetime: 1800000        # 连接最大生命周期
```

---

## 💾 备份与恢复

### 备份脚本

**backup-mysql.sh**:

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="./backup"
BACKUP_FILE="$BACKUP_DIR/video-manager-$DATE.sql"

mkdir -p $BACKUP_DIR

mysqldump -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USERNAME -p$MYSQL_PASSWORD \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  video_manager > $BACKUP_FILE

echo "Backup completed: $BACKUP_FILE"
```

### 恢复脚本

**restore-mysql.sh**:

```bash
#!/bin/bash
BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: ./restore-mysql.sh <backup-file>"
  exit 1
fi

mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USERNAME -p$MYSQL_PASSWORD \
  video_manager < $BACKUP_FILE

echo "Restore completed from: $BACKUP_FILE"
```

### 定时备份（Crontab）

```bash
# 每天凌晨2点备份
0 2 * * * /path/to/backup-mysql.sh
```

---

## 🔍 监控与诊断

### 常用查询

```sql
-- 查看数据库大小
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'video_manager'
GROUP BY table_schema;

-- 查看表大小
SELECT 
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND((data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'video_manager'
ORDER BY (data_length + index_length) DESC;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';
SHOW VARIABLES LIKE 'max_connections';
```

### 使用MySQL Workbench

1. 下载安装 [MySQL Workbench](https://www.mysql.com/products/workbench/)
2. 创建新连接：
   - Connection Name: Video Manager
   - Hostname: `<NAS_IP>`
   - Port: `3306`
   - Username: `videomanager`
3. 测试连接并保存

---

## ❓ 常见问题

### Q1: 连接被拒绝

**错误**: `Access denied for user 'videomanager'@'...'`

**解决**:
```sql
-- 检查用户权限
SELECT User, Host FROM mysql.user WHERE User = 'videomanager';

-- 重新授权
GRANT ALL PRIVILEGES ON video_manager.* TO 'videomanager'@'%';
FLUSH PRIVILEGES;
```

### Q2: 无法连接到MySQL

**检查清单**:
- [ ] MySQL服务是否运行
- [ ] 防火墙是否开放3306端口
- [ ] MySQL是否允许远程连接
- [ ] 网络是否连通（ping测试）

**Docker环境检查**:
```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 检查端口映射
docker port <mysql-container> 3306

# 测试连接
docker exec -it <mysql-container> mysql -u videomanager -p
```

### Q3: 字符集问题

**错误**: 中文乱码

**解决**:
```sql
-- 检查数据库字符集
SHOW CREATE DATABASE video_manager;

-- 检查表字符集
SHOW CREATE TABLE video;

-- 确保都是utf8mb4
```

### Q4: 性能慢

**诊断步骤**:
1. 开启慢查询日志
2. 使用EXPLAIN分析慢查询
3. 检查索引是否生效
4. 调整MySQL配置参数

---

## 📝 配置检查清单

部署前请确认：

- [ ] MySQL服务已启动
- [ ] 数据库 `video_manager` 已创建
- [ ] 用户 `videomanager` 已创建并授权
- [ ] 表结构已初始化
- [ ] application.yml配置正确
- [ ] 网络连通（可ping通MySQL服务器）
- [ ] 防火墙已开放3306端口
- [ ] 密码已修改为安全密码
- [ ] 备份策略已配置

---

## 🎯 下一步

配置完成后：

1. 启动应用并测试
2. 配置NAS文件路径
3. 开始扫描文件
4. 监控数据库性能
5. 配置定期备份

---

**文档版本**: v1.0  
**更新日期**: 2026-04-19
