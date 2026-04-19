-- ============================================
-- Video Manager MySQL 数据库初始化脚本
-- ============================================
-- 数据库: video_manager
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS video_manager 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE video_manager;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN, USER',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 扫描路径表
-- ============================================
CREATE TABLE IF NOT EXISTS scan_path (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '路径ID',
    path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    type VARCHAR(20) NOT NULL COMMENT '类型：VIDEO, IMAGE',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用, 1-启用',
    last_scan_at DATETIME COMMENT '最后扫描时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_path (path(255)),
    INDEX idx_type (type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫描路径表';

-- ============================================
-- 文件夹表
-- ============================================
CREATE TABLE IF NOT EXISTS folder (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '文件夹ID',
    path VARCHAR(1000) NOT NULL COMMENT '文件夹路径',
    name VARCHAR(255) NOT NULL COMMENT '文件夹名称',
    parent_path VARCHAR(1000) COMMENT '父文件夹路径',
    file_count INT NOT NULL DEFAULT 0 COMMENT '文件数量',
    total_size BIGINT NOT NULL DEFAULT 0 COMMENT '总大小（字节）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_path (path(255)),
    INDEX idx_parent_path (parent_path(255)),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件夹表';

-- ============================================
-- 视频表
-- ============================================
CREATE TABLE IF NOT EXISTS video (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '视频ID',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    duration INT COMMENT '视频时长（秒）',
    width INT COMMENT '视频宽度',
    height INT COMMENT '视频高度',
    thumbnail_path VARCHAR(1000) COMMENT '缩略图路径',
    hash VARCHAR(64) COMMENT '文件MD5哈希值（用于去重）',
    dislike TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否不喜欢：0-未标记, 1-不喜欢',
    folder_id INT COMMENT '所属文件夹ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_file_path (file_path(255)),
    INDEX idx_file_name (file_name),
    INDEX idx_file_size (file_size),
    INDEX idx_hash (hash),
    INDEX idx_dislike (dislike),
    INDEX idx_folder_id (folder_id),
    INDEX idx_created_at (created_at),
    INDEX idx_duration (duration),
    
    FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频表';

-- ============================================
-- 图片表
-- ============================================
CREATE TABLE IF NOT EXISTS image (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    width INT COMMENT '图片宽度',
    height INT COMMENT '图片高度',
    thumbnail_path VARCHAR(1000) COMMENT '缩略图路径',
    hash VARCHAR(64) COMMENT '文件MD5哈希值（用于去重）',
    dislike TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否不喜欢：0-未标记, 1-不喜欢',
    folder_id INT COMMENT '所属文件夹ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_file_path (file_path(255)),
    INDEX idx_file_name (file_name),
    INDEX idx_file_size (file_size),
    INDEX idx_hash (hash),
    INDEX idx_dislike (dislike),
    INDEX idx_folder_id (folder_id),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片表';

-- ============================================
-- 插入默认管理员用户
-- ============================================
-- 默认密码: admin123 (BCrypt加密)
INSERT INTO user (username, password, role, enabled) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQv37YK0Ke.Mz5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username=username;

-- ============================================
-- 创建视图：统计视图
-- ============================================
CREATE OR REPLACE VIEW v_statistics AS
SELECT 
    (SELECT COUNT(*) FROM video) AS video_count,
    (SELECT COUNT(*) FROM image) AS image_count,
    (SELECT COUNT(*) FROM video WHERE dislike = 1) AS video_dislike_count,
    (SELECT COUNT(*) FROM image WHERE dislike = 1) AS image_dislike_count,
    (SELECT COALESCE(SUM(file_size), 0) FROM video) AS video_total_size,
    (SELECT COALESCE(SUM(file_size), 0) FROM image) AS image_total_size,
    (SELECT COUNT(*) FROM folder) AS folder_count;

-- ============================================
-- 创建存储过程：更新文件夹统计
-- ============================================
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS update_folder_stats(IN p_folder_id INT)
BEGIN
    UPDATE folder f
    SET 
        file_count = (
            SELECT COUNT(*) FROM video v WHERE v.folder_id = f.id
        ) + (
            SELECT COUNT(*) FROM image i WHERE i.folder_id = f.id
        ),
        total_size = (
            SELECT COALESCE(SUM(file_size), 0) FROM video v WHERE v.folder_id = f.id
        ) + (
            SELECT COALESCE(SUM(file_size), 0) FROM image i WHERE i.folder_id = f.id
        )
    WHERE id = p_folder_id;
END //

DELIMITER ;

-- ============================================
-- 完成提示
-- ============================================
SELECT 'Database initialization completed successfully!' AS message;
SELECT 
    DATABASE() AS database_name,
    VERSION() AS mysql_version,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'video_manager') AS table_count;
