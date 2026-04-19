-- ============================================
-- 数据库表结构修复脚本
-- 修改数据库字段名以匹配Java实体类
-- ============================================

USE video_manager;

-- ============================================
-- 1. 修复 user 表
-- 将 password 改为 password_hash
-- ============================================
ALTER TABLE user CHANGE COLUMN password password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希';

-- ============================================
-- 2. 修复 scan_path 表
-- 将 type 改为 media_type
-- ============================================
ALTER TABLE scan_path CHANGE COLUMN type media_type VARCHAR(20) NOT NULL COMMENT '媒体类型: video, image, all';

-- ============================================
-- 3. 修复 folder 表
-- 添加缺失的字段，删除不需要的字段
-- ============================================
-- 删除 parent_path 列（如果存在）
ALTER TABLE folder DROP COLUMN IF EXISTS parent_path;
-- 删除 total_size 列（如果存在）
ALTER TABLE folder DROP COLUMN IF EXISTS total_size;

-- 添加缺失的字段
ALTER TABLE folder ADD COLUMN IF NOT EXISTS parent_id INT COMMENT '父文件夹ID';
ALTER TABLE folder ADD COLUMN IF NOT EXISTS media_type VARCHAR(20) DEFAULT 'all' COMMENT '媒体类型';
ALTER TABLE folder ADD COLUMN IF NOT EXISTS folder_count INT DEFAULT 0 COMMENT '子文件夹数量';
ALTER TABLE folder ADD COLUMN IF NOT EXISTS last_modified DATETIME COMMENT '最后修改时间';

-- ============================================
-- 4. 修复 image 表
-- 添加 mime_type 字段
-- ============================================
ALTER TABLE image ADD COLUMN IF NOT EXISTS mime_type VARCHAR(100) COMMENT 'MIME类型';

-- ============================================
-- 验证修复结果
-- ============================================
SELECT '========== user表结构 ==========' AS info;
DESCRIBE user;

SELECT '========== scan_path表结构 ==========' AS info;
DESCRIBE scan_path;

SELECT '========== folder表结构 ==========' AS info;
DESCRIBE folder;

SELECT '========== image表结构 ==========' AS info;
DESCRIBE image;

SELECT '数据库修复完成!' AS message;
