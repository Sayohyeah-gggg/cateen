-- ================================================
-- 评分关注点功能数据库表
-- ================================================

USE `cateen`;

-- 1. 创建评论关注点表
CREATE TABLE IF NOT EXISTS `comment_preferences` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `comment_id` VARCHAR(50) NOT NULL COMMENT '评论ID',
    `preference_type` ENUM('taste', 'price', 'environment', 'service', 'other') NOT NULL COMMENT '关注点类型',
    `score` INT NOT NULL COMMENT '该关注点的评分(1-5)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_type` (`comment_id`, `preference_type`),
    KEY `idx_comment` (`comment_id`),
    KEY `idx_type` (`preference_type`),
    KEY `idx_score` (`score`),
    FOREIGN KEY (`comment_id`) REFERENCES `comments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论关注点表';

-- 2. 创建关键词配置表（用于NLP分析）
CREATE TABLE IF NOT EXISTS `preference_keywords` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `preference_type` VARCHAR(50) NOT NULL COMMENT '关注点类型',
    `keyword` VARCHAR(100) NOT NULL COMMENT '关键词',
    `weight` DECIMAL(3,2) DEFAULT 1.0 COMMENT '权重',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_keyword` (`preference_type`, `keyword`),
    KEY `idx_type` (`preference_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关注点关键词表';

-- 3. 插入关键词数据
INSERT INTO `preference_keywords` (`id`, `preference_type`, `keyword`, `weight`) VALUES
-- 口味相关
('kw_001', 'taste', '好吃', 1.0),
('kw_002', 'taste', '美味', 1.0),
('kw_003', 'taste', '口味', 0.9),
('kw_004', 'taste', '味道', 0.9),
('kw_005', 'taste', '香', 0.8),
('kw_006', 'taste', '鲜', 0.8),
('kw_007', 'taste', '可口', 0.9),
('kw_008', 'taste', '美食', 0.8),
-- 价格相关
('kw_009', 'price', '实惠', 1.0),
('kw_010', 'price', '便宜', 0.9),
('kw_011', 'price', '性价比', 1.0),
('kw_012', 'price', '价格', 0.8),
('kw_013', 'price', '划算', 0.9),
('kw_014', 'price', '合理', 0.8),
('kw_015', 'price', '贵', 0.7),
('kw_016', 'price', '值', 0.8),
-- 环境相关
('kw_017', 'environment', '环境', 1.0),
('kw_018', 'environment', '装修', 0.8),
('kw_019', 'environment', '干净', 0.9),
('kw_020', 'environment', '舒适', 0.9),
('kw_021', 'environment', '氛围', 0.8),
('kw_022', 'environment', '卫生', 0.9),
('kw_023', 'environment', '整洁', 0.8),
('kw_024', 'environment', '优雅', 0.7),
-- 服务相关
('kw_025', 'service', '服务', 1.0),
('kw_026', 'service', '态度', 0.9),
('kw_027', 'service', '热情', 0.9),
('kw_028', 'service', '周到', 0.8),
('kw_029', 'service', '快', 0.7),
('kw_030', 'service', '礼貌', 0.8),
('kw_031', 'service', '专业', 0.8),
('kw_032', 'service', '贴心', 0.9);

-- 4. 插入测试数据（基于现有评论）
INSERT INTO `comment_preferences` (`id`, `comment_id`, `preference_type`, `score`) VALUES
-- 评论1: 味道很好，肥而不腻，推荐！
('pref_001', 'cmt_001', 'taste', 5),
('pref_002', 'cmt_001', 'service', 4),
-- 评论2: 经典美食，值得一试
('pref_003', 'cmt_002', 'taste', 5),
('pref_004', 'cmt_002', 'price', 4),
-- 评论3: 酸甜适中，很好吃
('pref_005', 'cmt_003', 'taste', 5),
-- 评论4: 炒饭很香，料很足
('pref_006', 'cmt_004', 'taste', 4),
('pref_007', 'cmt_004', 'price', 5),
-- 评论5: 非常辣，但是很过瘾
('pref_008', 'cmt_005', 'taste', 5),
('pref_009', 'cmt_005', 'environment', 4);

-- 5. 创建视图：方便查询关注点统计
CREATE OR REPLACE VIEW `v_preference_statistics` AS
SELECT
    cp.preference_type,
    CASE cp.preference_type
        WHEN 'taste' THEN '口味'
        WHEN 'price' THEN '价格'
        WHEN 'environment' THEN '环境'
        WHEN 'service' THEN '服务'
        ELSE '其他'
    END AS preference_name,
    COUNT(*) AS count,
    ROUND(AVG(cp.score), 2) AS avg_score,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM comment_preferences), 2) AS percentage,
    CASE cp.preference_type
        WHEN 'taste' THEN '#3b82f6'
        WHEN 'price' THEN '#10b981'
        WHEN 'environment' THEN '#f59e0b'
        WHEN 'service' THEN '#8b5cf6'
        ELSE '#6b7280'
    END AS color
FROM comment_preferences cp
GROUP BY cp.preference_type
ORDER BY count DESC;

