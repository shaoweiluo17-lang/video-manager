-- ================================================================
-- 视频图片文件管理系统 - 数据库表结构
-- ================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 扫描路径配置表
CREATE TABLE IF NOT EXISTS scan_path (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    path TEXT NOT NULL,
    media_type TEXT DEFAULT 'all' CHECK(media_type IN ('video', 'image', 'all')),
    enabled INTEGER DEFAULT 1,
    last_scan_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 文件夹信息表
CREATE TABLE IF NOT EXISTS folder (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    path TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    parent_id INTEGER,
    media_type TEXT DEFAULT 'all' CHECK(media_type IN ('video', 'image', 'all')),
    file_count INTEGER DEFAULT 0,
    folder_count INTEGER DEFAULT 0,
    last_modified DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES folder(id) ON DELETE CASCADE
);

-- 视频文件索引表
CREATE TABLE IF NOT EXISTS video (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path TEXT UNIQUE NOT NULL,
    file_name TEXT NOT NULL,
    file_size INTEGER,
    duration INTEGER,
    width INTEGER,
    height INTEGER,
    thumbnail_path TEXT,
    hash TEXT,
    dislike INTEGER DEFAULT 0,
    folder_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE SET NULL
);

-- 图片文件索引表
CREATE TABLE IF NOT EXISTS image (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path TEXT UNIQUE NOT NULL,
    file_name TEXT NOT NULL,
    file_size INTEGER,
    mime_type TEXT,
    width INTEGER,
    height INTEGER,
    thumbnail_path TEXT,
    hash TEXT,
    dislike INTEGER DEFAULT 0,
    folder_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE SET NULL
);

-- ================================================================
-- 索引
-- ================================================================

-- 视频索引
CREATE INDEX IF NOT EXISTS idx_video_hash ON video(hash);
CREATE INDEX IF NOT EXISTS idx_video_dislike ON video(dislike);
CREATE INDEX IF NOT EXISTS idx_video_folder ON video(folder_id);
CREATE INDEX IF NOT EXISTS idx_video_created ON video(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_video_name ON video(file_name);

-- 图片索引
CREATE INDEX IF NOT EXISTS idx_image_hash ON image(hash);
CREATE INDEX IF NOT EXISTS idx_image_dislike ON image(dislike);
CREATE INDEX IF NOT EXISTS idx_image_folder ON image(folder_id);
CREATE INDEX IF NOT EXISTS idx_image_created ON image(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_image_name ON image(file_name);

-- 文件夹索引
CREATE INDEX IF NOT EXISTS idx_folder_parent ON folder(parent_id);
CREATE INDEX IF NOT EXISTS idx_folder_path ON folder(path);
CREATE INDEX IF NOT EXISTS idx_folder_media_type ON folder(media_type);

-- 扫描路径索引
CREATE INDEX IF NOT EXISTS idx_scan_path_media_type ON scan_path(media_type);
CREATE INDEX IF NOT EXISTS idx_scan_path_enabled ON scan_path(enabled);

-- ================================================================
-- 初始数据
-- ================================================================

-- 默认管理员账号 (密码: admin123)
-- 密码是 BCrypt 加密后的结果
INSERT OR IGNORE INTO user (username, password_hash, created_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', datetime('now'));
