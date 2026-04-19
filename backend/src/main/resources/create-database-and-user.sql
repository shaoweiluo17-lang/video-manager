-- ============================================
-- Video Manager MySQL 数据库和用户创建脚本
-- ============================================
-- 请在MySQL root用户下执行此脚本
-- mysql -u root -p < create-database-and-user.sql
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS video_manager 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 创建用户（请修改密码）
CREATE USER IF NOT EXISTS 'videomanager'@'%' IDENTIFIED BY 'your-password';

-- 授予权限
GRANT ALL PRIVILEGES ON video_manager.* TO 'videomanager'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建结果
SELECT 'Database and user created successfully!' AS message;
SHOW DATABASES LIKE 'video_manager';
SELECT User, Host FROM mysql.user WHERE User = 'videomanager';
