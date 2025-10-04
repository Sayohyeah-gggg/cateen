-- 访问统计表
CREATE TABLE IF NOT EXISTS `visit_stats` (
  `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `visits` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
  `unique_visitors` INT NOT NULL DEFAULT 0 COMMENT '独立访客数',
  `page_views` INT NOT NULL DEFAULT 0 COMMENT '页面浏览量',
  `bounce_rate` INT NOT NULL DEFAULT 0 COMMENT '跳出率(%)',
  `avg_stay_time` INT NOT NULL DEFAULT 0 COMMENT '平均停留时间(秒)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问统计表';

-- 设备统计表
CREATE TABLE IF NOT EXISTS `device_stats` (
  `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型: desktop/mobile/tablet',
  `visit_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_device` (`stat_date`, `device_type`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备统计表';

-- 来源统计表
CREATE TABLE IF NOT EXISTS `source_stats` (
  `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `source_type` VARCHAR(50) NOT NULL COMMENT '来源类型: direct/search/social/referral/other',
  `visit_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_source` (`stat_date`, `source_type`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来源统计表';

-- 插入近30天的模拟数据
INSERT INTO `visit_stats` (`id`, `stat_date`, `visits`, `unique_visitors`, `page_views`, `bounce_rate`, `avg_stay_time`)
SELECT 
    CONCAT('vs_', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL n DAY), '%Y%m%d')),
    DATE_SUB(CURDATE(), INTERVAL n DAY),
    FLOOR(500 + RAND() * 500),
    FLOOR(200 + RAND() * 300),
    FLOOR(1000 + RAND() * 2000),
    FLOOR(20 + RAND() * 30),
    FLOOR(120 + RAND() * 180)
FROM (
    SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION
    SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION
    SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION
    SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION
    SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION
    SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) AS numbers
ON DUPLICATE KEY UPDATE 
    visits = VALUES(visits),
    unique_visitors = VALUES(unique_visitors),
    page_views = VALUES(page_views);

-- 插入设备统计数据
INSERT INTO `device_stats` (`id`, `stat_date`, `device_type`, `visit_count`)
SELECT 
    CONCAT('ds_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_desktop'),
    CURDATE(),
    'desktop',
    FLOOR(400 + RAND() * 200)
ON DUPLICATE KEY UPDATE visit_count = VALUES(visit_count);

INSERT INTO `device_stats` (`id`, `stat_date`, `device_type`, `visit_count`)
SELECT 
    CONCAT('ds_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_mobile'),
    CURDATE(),
    'mobile',
    FLOOR(300 + RAND() * 200)
ON DUPLICATE KEY UPDATE visit_count = VALUES(visit_count);

INSERT INTO `device_stats` (`id`, `stat_date`, `device_type`, `visit_count`)
SELECT 
    CONCAT('ds_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_tablet'),
    CURDATE(),
    'tablet',
    FLOOR(100 + RAND() * 100)
ON DUPLICATE KEY UPDATE visit_count = VALUES(visit_count);

-- 插入来源统计数据
INSERT INTO `source_stats` (`id`, `stat_date`, `source_type`, `visit_count`)
VALUES
    (CONCAT('ss_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_direct'), CURDATE(), 'direct', 1200),
    (CONCAT('ss_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_search'), CURDATE(), 'search', 980),
    (CONCAT('ss_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_social'), CURDATE(), 'social', 650),
    (CONCAT('ss_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_referral'), CURDATE(), 'referral', 420),
    (CONCAT('ss_', DATE_FORMAT(CURDATE(), '%Y%m%d'), '_other'), CURDATE(), 'other', 210)
ON DUPLICATE KEY UPDATE visit_count = VALUES(visit_count);

