-- ============================================
-- 迁移脚本: user 表字段补充
-- 适用场景: 在老库上执行，补齐新增字段
-- 执行方式: mysql -u root -p cat_cafe < migrate_add_userphone.sql
-- 最后更新: 2026-05-19
-- ============================================

-- v1: 2026-05-16 — 新增 userPhone
SET @has_user_phone := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'cat_cafe'
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'userPhone'
);
SET @sql := IF(
    @has_user_phone = 0,
    'ALTER TABLE `user` ADD COLUMN `userPhone` VARCHAR(20) DEFAULT NULL COMMENT ''手机号'' AFTER `birthday`',
    'SELECT ''userPhone already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- v2: 2026-05-19 — 新增 userAvatar
SET @has_user_avatar := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'cat_cafe'
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'userAvatar'
);
SET @sql := IF(
    @has_user_avatar = 0,
    'ALTER TABLE `user` ADD COLUMN `userAvatar` VARCHAR(255) DEFAULT NULL COMMENT ''头像URL'' AFTER `userPhone`',
    'SELECT ''userAvatar already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查结果
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'user' AND TABLE_SCHEMA = 'cat_cafe'
ORDER BY ORDINAL_POSITION;
