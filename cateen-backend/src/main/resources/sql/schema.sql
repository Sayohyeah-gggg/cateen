-- 创建数据库
CREATE DATABASE IF NOT EXISTS `cateen` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `cateen`;

-- 用户表
CREATE TABLE IF NOT EXISTS `profiles` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户唯一标识',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` ENUM('admin', 'user') DEFAULT 'user' COMMENT '角色',
    `status` ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_username` (`username`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 美食分类表
CREATE TABLE IF NOT EXISTS `food_categories` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` TEXT DEFAULT NULL COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='美食分类表';

-- 美食标签表
CREATE TABLE IF NOT EXISTS `food_tags` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `name` VARCHAR(20) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#3b82f6' COMMENT '标签颜色',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='美食标签表';

-- 美食表
CREATE TABLE IF NOT EXISTS `foods` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '美食名称',
    `description` TEXT DEFAULT NULL COMMENT '美食描述',
    `category_id` VARCHAR(50) DEFAULT NULL COMMENT '分类ID',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    `rating` DECIMAL(3,1) DEFAULT 0.0 COMMENT '评分',
    `rating_count` INT DEFAULT 0 COMMENT '评价数',
    `status` ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_rating` (`rating`),
    KEY `idx_status` (`status`),
    KEY `idx_name` (`name`),
    FOREIGN KEY (`category_id`) REFERENCES `food_categories`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='美食表';

-- 美食标签关联表
CREATE TABLE IF NOT EXISTS `food_tag_relations` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `food_id` VARCHAR(50) NOT NULL COMMENT '美食ID',
    `tag_id` VARCHAR(50) NOT NULL COMMENT '标签ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_food_tag` (`food_id`, `tag_id`),
    KEY `idx_food` (`food_id`),
    KEY `idx_tag` (`tag_id`),
    FOREIGN KEY (`food_id`) REFERENCES `foods`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `food_tags`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='美食标签关联表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comments` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `food_id` VARCHAR(50) NOT NULL COMMENT '美食ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `rating` INT NOT NULL COMMENT '评分(1-5)',
    `status` ENUM('pending', 'approved', 'rejected') DEFAULT 'pending' COMMENT '审核状态',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_food` (`food_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_rating` (`rating`),
    FOREIGN KEY (`food_id`) REFERENCES `foods`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `profiles`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- 榜单表
CREATE TABLE IF NOT EXISTS `rankings` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `title` VARCHAR(100) NOT NULL COMMENT '榜单标题',
    `description` TEXT DEFAULT NULL COMMENT '榜单描述',
    `type` VARCHAR(50) DEFAULT 'general' COMMENT '榜单类型',
    `status` ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='榜单表';

-- 榜单美食关联表
CREATE TABLE IF NOT EXISTS `ranking_foods` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `ranking_id` VARCHAR(50) NOT NULL COMMENT '榜单ID',
    `food_id` VARCHAR(50) NOT NULL COMMENT '美食ID',
    `rank_position` INT NOT NULL COMMENT '排名位置',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ranking_food` (`ranking_id`, `food_id`),
    KEY `idx_ranking` (`ranking_id`),
    KEY `idx_position` (`rank_position`),
    FOREIGN KEY (`ranking_id`) REFERENCES `rankings`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`food_id`) REFERENCES `foods`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='榜单美食关联表';

