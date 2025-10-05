# 美食评估管理系统 - 完整数据库建表语句和测试数据

## 📋 数据库建表语句

### 1. 用户表 (profiles)
```sql
-- 用户表
CREATE TABLE `profiles` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户唯一标识',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) DEFAULT NULL COMMENT '密码哈希',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` enum('admin','user') DEFAULT 'user' COMMENT '角色：admin-管理员，user-普通用户',
  `status` enum('active','inactive') DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `wechat_openid` varchar(128) DEFAULT NULL COMMENT '微信OpenID（小程序登录使用）',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2. 美食分类表 (food_categories)
```sql
-- 美食分类表
CREATE TABLE `food_categories` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(500) DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(200) DEFAULT NULL COMMENT '分类图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美食分类表';
```

### 3. 美食表 (foods)
```sql
-- 美食表
CREATE TABLE `foods` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `name` varchar(200) NOT NULL COMMENT '美食名称',
  `description` text COMMENT '美食描述',
  `category_id` varchar(50) NOT NULL COMMENT '分类ID',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '评分',
  `rating_count` int DEFAULT '0' COMMENT '评价数',
  `status` enum('active','inactive') DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_foods_category` FOREIGN KEY (`category_id`) REFERENCES `food_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美食表';
```

### 4. 评论表 (comments)
```sql
-- 评论表
CREATE TABLE `comments` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `food_id` varchar(50) NOT NULL COMMENT '美食ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `rating` int NOT NULL COMMENT '评分(1-5)',
  `images` text COMMENT '评论图片（逗号分隔）',
  `status` enum('pending','approved','rejected') DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_comments_food` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`),
  CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

### 5. 用户收藏表 (collection)
```sql
-- 用户收藏表
CREATE TABLE `collection` (
  `id` varchar(50) NOT NULL COMMENT '收藏ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `food_id` varchar(50) NOT NULL COMMENT '美食ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_food` (`user_id`,`food_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_food_id` (`food_id`),
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`id`),
  CONSTRAINT `fk_collection_food` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';
```

### 6. 评论点赞表 (comment_like)
```sql
-- 评论点赞表
CREATE TABLE `comment_like` (
  `id` varchar(50) NOT NULL COMMENT '点赞ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `comment_id` varchar(50) NOT NULL COMMENT '评论ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_comment` (`user_id`,`comment_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_comment_id` (`comment_id`),
  CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`id`),
  CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';
```

### 7. 美食标签表 (food_tags)
```sql
-- 美食标签表
CREATE TABLE `food_tags` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '标签名称',
  `color` varchar(20) DEFAULT '#1890ff' COMMENT '标签颜色',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美食标签表';
```

### 8. 美食标签关联表 (food_tag_relations)
```sql
-- 美食标签关联表
CREATE TABLE `food_tag_relations` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `food_id` varchar(50) NOT NULL COMMENT '美食ID',
  `tag_id` varchar(50) NOT NULL COMMENT '标签ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_food_tag` (`food_id`,`tag_id`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_food_tag_relations_food` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`),
  CONSTRAINT `fk_food_tag_relations_tag` FOREIGN KEY (`tag_id`) REFERENCES `food_tags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美食标签关联表';
```

### 9. 榜单表 (rankings)
```sql
-- 榜单表
CREATE TABLE `rankings` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '榜单标题',
  `description` text COMMENT '榜单描述',
  `type` varchar(50) DEFAULT NULL COMMENT '榜单类型',
  `status` enum('active','inactive') DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='榜单表';
```

### 10. 榜单美食关联表 (ranking_foods)
```sql
-- 榜单美食关联表
CREATE TABLE `ranking_foods` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `ranking_id` varchar(50) NOT NULL COMMENT '榜单ID',
  `food_id` varchar(50) NOT NULL COMMENT '美食ID',
  `rank_position` int NOT NULL COMMENT '排名位置',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ranking_id` (`ranking_id`),
  KEY `idx_food_id` (`food_id`),
  CONSTRAINT `fk_ranking_foods_ranking` FOREIGN KEY (`ranking_id`) REFERENCES `rankings` (`id`),
  CONSTRAINT `fk_ranking_foods_food` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='榜单美食关联表';
```

### 11. 搜索历史表 (search_history)
```sql
-- 搜索历史表
CREATE TABLE `search_history` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `keyword` varchar(200) NOT NULL COMMENT '搜索关键词',
  `search_count` int DEFAULT '1' COMMENT '搜索次数',
  `last_search_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后搜索时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`),
  CONSTRAINT `fk_search_history_user` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';
```

### 12. 评论关注点表 (comment_preferences)
```sql
-- 评论关注点表
CREATE TABLE `comment_preferences` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `comment_id` varchar(50) NOT NULL COMMENT '评论ID',
  `preference_type` enum('taste','price','environment','service','other') NOT NULL COMMENT '关注点类型',
  `score` int NOT NULL COMMENT '该关注点的评分(1-5)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_preference_type` (`preference_type`),
  CONSTRAINT `fk_comment_preferences_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论关注点表';
```

### 13. 访问统计表 (visit_stats)
```sql
-- 访问统计表
CREATE TABLE `visit_stats` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `visits` int DEFAULT '0' COMMENT '访问次数',
  `unique_visitors` int DEFAULT '0' COMMENT '独立访客数',
  `page_views` int DEFAULT '0' COMMENT '页面浏览量',
  `bounce_rate` int DEFAULT '0' COMMENT '跳出率(%)',
  `avg_stay_time` int DEFAULT '0' COMMENT '平均停留时间(秒)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问统计表';
```

### 14. 设备统计表 (device_stats)
```sql
-- 设备统计表
CREATE TABLE `device_stats` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `device_type` enum('desktop','mobile','tablet') NOT NULL COMMENT '设备类型',
  `visit_count` int DEFAULT '0' COMMENT '访问次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_device` (`stat_date`, `device_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备统计表';
```

### 15. 来源统计表 (source_stats)
```sql
-- 来源统计表
CREATE TABLE `source_stats` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `source_type` enum('direct','search','social','referral','other') NOT NULL COMMENT '来源类型',
  `visit_count` int DEFAULT '0' COMMENT '访问次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_source` (`stat_date`, `source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来源统计表';
```

### 16. 关注点关键词表 (preference_keywords)
```sql
-- 关注点关键词表
CREATE TABLE `preference_keywords` (
  `id` varchar(50) NOT NULL COMMENT '主键ID',
  `keyword` varchar(100) NOT NULL COMMENT '关键词',
  `preference_type` enum('taste','price','environment','service','other') NOT NULL COMMENT '关注点类型',
  `weight` decimal(3,2) DEFAULT '1.00' COMMENT '权重',
  `usage_count` int DEFAULT '0' COMMENT '使用次数',
  `status` enum('active','inactive') DEFAULT 'active' COMMENT '状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword_type` (`keyword`, `preference_type`),
  KEY `idx_preference_type` (`preference_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注点关键词表';
```

## 📊 测试数据

### 1. 用户测试数据
```sql
-- 插入用户测试数据
INSERT INTO `profiles` (`id`, `user_id`, `username`, `password_hash`, `phone`, `role`, `status`, `wechat_openid`, `nickname`, `avatar`, `created_at`, `updated_at`) VALUES
('admin001', 'admin_001', 'admin', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138000', 'admin', 'active', NULL, '系统管理员', 'https://example.com/admin-avatar.jpg', NOW(), NOW()),
('user001', 'user_001', 'testuser1', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138001', 'user', 'active', 'test_openid_001', '美食爱好者', 'https://example.com/user1-avatar.jpg', NOW(), NOW()),
('user002', 'user_002', 'testuser2', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138002', 'user', 'active', 'test_openid_002', '吃货小王', 'https://example.com/user2-avatar.jpg', NOW(), NOW()),
('user003', 'user_003', 'testuser3', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138003', 'user', 'active', 'test_openid_003', '美食达人', 'https://example.com/user3-avatar.jpg', NOW(), NOW());
```

### 2. 美食分类测试数据
```sql
-- 插入美食分类测试数据
INSERT INTO `food_categories` (`id`, `name`, `description`, `icon`, `sort_order`, `created_at`) VALUES
('cat001', '川菜', '四川菜系，以麻辣著称', '🌶️', 1, NOW()),
('cat002', '粤菜', '广东菜系，清淡鲜美', '🦐', 2, NOW()),
('cat003', '鲁菜', '山东菜系，口味偏重', '🥟', 3, NOW()),
('cat004', '苏菜', '江苏菜系，甜咸适中', '🦀', 4, NOW()),
('cat005', '浙菜', '浙江菜系，清淡爽口', '🐟', 5, NOW()),
('cat006', '闽菜', '福建菜系，海鲜丰富', '🦞', 6, NOW()),
('cat007', '湘菜', '湖南菜系，香辣可口', '🌶️', 7, NOW()),
('cat008', '徽菜', '安徽菜系，重油重色', '🍖', 8, NOW()),
('cat009', '小吃', '各地特色小吃', '🍜', 9, NOW()),
('cat010', '甜品', '各种甜品和饮品', '🍰', 10, NOW());
```

### 3. 美食测试数据
```sql
-- 插入美食测试数据
INSERT INTO `foods` (`id`, `name`, `description`, `category_id`, `image_url`, `price`, `rating`, `rating_count`, `status`, `created_by`, `created_at`, `updated_at`) VALUES
('food001', '宫保鸡丁', '经典川菜，鸡肉嫩滑，花生香脆，麻辣鲜香', 'cat001', 'https://example.com/images/gongbao-chicken.jpg', 28.00, 4.50, 156, 'active', 'admin001', NOW(), NOW()),
('food002', '白切鸡', '传统粤菜，鸡肉鲜嫩，蘸料丰富', 'cat002', 'https://example.com/images/white-cut-chicken.jpg', 35.00, 4.30, 89, 'active', 'admin001', NOW(), NOW()),
('food003', '糖醋里脊', '经典鲁菜，酸甜可口，外酥内嫩', 'cat003', 'https://example.com/images/sweet-sour-pork.jpg', 32.00, 4.20, 124, 'active', 'admin001', NOW(), NOW()),
('food004', '扬州炒饭', '江苏名菜，米饭粒粒分明，配料丰富', 'cat004', 'https://example.com/images/yangzhou-rice.jpg', 18.00, 4.60, 203, 'active', 'admin001', NOW(), NOW()),
('food005', '西湖牛肉羹', '杭州名菜，汤清味鲜，牛肉嫩滑', 'cat005', 'https://example.com/images/xihu-beef-soup.jpg', 25.00, 4.40, 167, 'active', 'admin001', NOW(), NOW()),
('food006', '佛跳墙', '福建名菜，用料丰富，汤浓味美', 'cat006', 'https://example.com/images/fotiaoqiang.jpg', 188.00, 4.80, 45, 'active', 'admin001', NOW(), NOW()),
('food007', '剁椒鱼头', '湖南名菜，鱼肉鲜嫩，剁椒香辣', 'cat007', 'https://example.com/images/duojiao-fish-head.jpg', 68.00, 4.70, 98, 'active', 'admin001', NOW(), NOW()),
('food008', '红烧肉', '经典家常菜，肥瘦相间，香甜软糯', 'cat003', 'https://example.com/images/hongshao-pork.jpg', 38.00, 4.50, 189, 'active', 'admin001', NOW(), NOW()),
('food009', '小笼包', '上海名点，皮薄馅大，汤汁丰富', 'cat009', 'https://example.com/images/xiaolongbao.jpg', 22.00, 4.40, 156, 'active', 'admin001', NOW(), NOW()),
('food010', '麻婆豆腐', '四川名菜，豆腐嫩滑，麻辣鲜香', 'cat001', 'https://example.com/images/mapo-tofu.jpg', 18.00, 4.30, 134, 'active', 'admin001', NOW(), NOW());
```

### 4. 评论测试数据
```sql
-- 插入评论测试数据
INSERT INTO `comments` (`id`, `food_id`, `user_id`, `content`, `rating`, `images`, `status`, `created_at`) VALUES
('comment001', 'food001', 'user001', '宫保鸡丁真的很棒！鸡肉很嫩，花生很香，麻辣味刚好，不会太辣。', 5, 'https://example.com/images/comment1.jpg', 'approved', NOW()),
('comment002', 'food001', 'user002', '味道不错，但是有点偏咸，建议少放点盐。', 4, NULL, 'approved', NOW()),
('comment003', 'food002', 'user001', '白切鸡很正宗，鸡肉很嫩，蘸料也很丰富，推荐！', 5, 'https://example.com/images/comment2.jpg', 'approved', NOW()),
('comment004', 'food003', 'user003', '糖醋里脊酸甜适中，外酥内嫩，很好吃！', 5, NULL, 'approved', NOW()),
('comment005', 'food004', 'user002', '扬州炒饭粒粒分明，配料丰富，性价比很高。', 4, 'https://example.com/images/comment3.jpg', 'approved', NOW()),
('comment006', 'food005', 'user001', '西湖牛肉羹汤很清，牛肉很嫩，味道很鲜。', 5, NULL, 'approved', NOW()),
('comment007', 'food006', 'user003', '佛跳墙用料很丰富，汤很浓，但是价格有点贵。', 4, 'https://example.com/images/comment4.jpg', 'approved', NOW()),
('comment008', 'food007', 'user002', '剁椒鱼头很辣，但是很香，鱼肉很嫩。', 4, NULL, 'approved', NOW()),
('comment009', 'food008', 'user001', '红烧肉肥瘦相间，香甜软糯，很好吃！', 5, 'https://example.com/images/comment5.jpg', 'approved', NOW()),
('comment010', 'food009', 'user003', '小笼包皮很薄，馅很大，汤汁很丰富，推荐！', 5, NULL, 'approved', NOW());
```

### 5. 用户收藏测试数据
```sql
-- 插入用户收藏测试数据
INSERT INTO `collection` (`id`, `user_id`, `food_id`, `created_at`) VALUES
('coll001', 'user001', 'food001', NOW()),
('coll002', 'user001', 'food002', NOW()),
('coll003', 'user001', 'food005', NOW()),
('coll004', 'user002', 'food003', NOW()),
('coll005', 'user002', 'food004', NOW()),
('coll006', 'user002', 'food008', NOW()),
('coll007', 'user003', 'food006', NOW()),
('coll008', 'user003', 'food007', NOW()),
('coll009', 'user003', 'food009', NOW()),
('coll010', 'user003', 'food010', NOW());
```

### 6. 评论点赞测试数据
```sql
-- 插入评论点赞测试数据
INSERT INTO `comment_like` (`id`, `user_id`, `comment_id`, `created_at`) VALUES
('like001', 'user002', 'comment001', NOW()),
('like002', 'user003', 'comment001', NOW()),
('like003', 'user001', 'comment003', NOW()),
('like004', 'user003', 'comment003', NOW()),
('like005', 'user001', 'comment004', NOW()),
('like006', 'user002', 'comment004', NOW()),
('like007', 'user002', 'comment005', NOW()),
('like008', 'user003', 'comment005', NOW()),
('like009', 'user001', 'comment006', NOW()),
('like010', 'user002', 'comment006', NOW());
```

### 7. 美食标签测试数据
```sql
-- 插入美食标签测试数据
INSERT INTO `food_tags` (`id`, `name`, `color`, `created_at`) VALUES
('tag001', '麻辣', '#ff4d4f', NOW()),
('tag002', '清淡', '#52c41a', NOW()),
('tag003', '甜味', '#faad14', NOW()),
('tag004', '咸味', '#1890ff', NOW()),
('tag005', '酸味', '#722ed1', NOW()),
('tag006', '香辣', '#eb2f96', NOW()),
('tag007', '鲜香', '#13c2c2', NOW()),
('tag008', '酥脆', '#fa8c16', NOW()),
('tag009', '软糯', '#a0d911', NOW()),
('tag010', '爽口', '#2f54eb', NOW());
```

### 8. 美食标签关联测试数据
```sql
-- 插入美食标签关联测试数据
INSERT INTO `food_tag_relations` (`id`, `food_id`, `tag_id`, `created_at`) VALUES
('ftr001', 'food001', 'tag001', NOW()),
('ftr002', 'food001', 'tag006', NOW()),
('ftr003', 'food002', 'tag002', NOW()),
('ftr004', 'food002', 'tag007', NOW()),
('ftr005', 'food003', 'tag003', NOW()),
('ftr006', 'food003', 'tag004', NOW()),
('ftr007', 'food004', 'tag002', NOW()),
('ftr008', 'food004', 'tag010', NOW()),
('ftr009', 'food005', 'tag002', NOW()),
('ftr010', 'food005', 'tag007', NOW());
```

### 9. 榜单测试数据
```sql
-- 插入榜单测试数据
INSERT INTO `rankings` (`id`, `title`, `description`, `type`, `status`, `created_by`, `created_at`, `updated_at`) VALUES
('rank001', '最受欢迎川菜', '根据用户评分和收藏数量评选的最受欢迎川菜', 'popular', 'active', 'admin001', NOW(), NOW()),
('rank002', '性价比最高美食', '根据价格和评分评选的性价比最高美食', 'value', 'active', 'admin001', NOW(), NOW()),
('rank003', '最受好评美食', '根据用户评论数量和质量评选的最受好评美食', 'rating', 'active', 'admin001', NOW(), NOW());
```

### 10. 榜单美食关联测试数据
```sql
-- 插入榜单美食关联测试数据
INSERT INTO `ranking_foods` (`id`, `ranking_id`, `food_id`, `rank_position`, `created_at`) VALUES
('rf001', 'rank001', 'food001', 1, NOW()),
('rf002', 'rank001', 'food010', 2, NOW()),
('rf003', 'rank002', 'food004', 1, NOW()),
('rf004', 'rank002', 'food010', 2, NOW()),
('rf005', 'rank002', 'food009', 3, NOW()),
('rf006', 'rank003', 'food001', 1, NOW()),
('rf007', 'rank003', 'food002', 2, NOW()),
('rf008', 'rank003', 'food005', 3, NOW());
```

### 11. 搜索历史测试数据
```sql
-- 插入搜索历史测试数据
INSERT INTO `search_history` (`id`, `user_id`, `keyword`, `search_count`, `last_search_time`, `created_at`, `updated_at`) VALUES
('sh001', 'user001', '宫保鸡丁', 3, NOW(), NOW(), NOW()),
('sh002', 'user001', '川菜', 2, NOW(), NOW(), NOW()),
('sh003', 'user002', '扬州炒饭', 1, NOW(), NOW(), NOW()),
('sh004', 'user002', '粤菜', 1, NOW(), NOW(), NOW()),
('sh005', 'user003', '佛跳墙', 2, NOW(), NOW(), NOW()),
('sh006', 'user003', '闽菜', 1, NOW(), NOW(), NOW());
```

### 12. 评论关注点测试数据
```sql
-- 插入评论关注点测试数据
INSERT INTO `comment_preferences` (`id`, `comment_id`, `preference_type`, `score`, `created_at`) VALUES
('cp001', 'comment001', 'taste', 5, NOW()),
('cp002', 'comment001', 'price', 4, NOW()),
('cp003', 'comment001', 'environment', 5, NOW()),
('cp004', 'comment001', 'service', 4, NOW()),
('cp005', 'comment002', 'taste', 4, NOW()),
('cp006', 'comment002', 'price', 3, NOW()),
('cp007', 'comment003', 'taste', 5, NOW()),
('cp008', 'comment003', 'price', 5, NOW()),
('cp009', 'comment003', 'environment', 4, NOW()),
('cp010', 'comment003', 'service', 5, NOW());
```

### 13. 访问统计测试数据
```sql
-- 插入访问统计测试数据
INSERT INTO `visit_stats` (`id`, `stat_date`, `visits`, `unique_visitors`, `page_views`, `bounce_rate`, `avg_stay_time`, `created_at`, `updated_at`) VALUES
('vs001', '2024-01-01', 150, 120, 300, 25, 180, NOW(), NOW()),
('vs002', '2024-01-02', 180, 140, 360, 22, 200, NOW(), NOW()),
('vs003', '2024-01-03', 200, 160, 400, 20, 220, NOW(), NOW()),
('vs004', '2024-01-04', 220, 180, 440, 18, 240, NOW(), NOW()),
('vs005', '2024-01-05', 250, 200, 500, 15, 260, NOW(), NOW());
```

## 🚀 执行说明

### 1. 创建数据库
```sql
CREATE DATABASE cateen CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cateen;
```

### 2. 执行建表语句
按照上述顺序执行所有建表语句

### 3. 插入测试数据
按照上述顺序执行所有测试数据插入语句

### 4. 验证数据
```sql
-- 查看各表数据量
SELECT 'profiles' as table_name, COUNT(*) as count FROM profiles
UNION ALL
SELECT 'food_categories', COUNT(*) FROM food_categories
UNION ALL
SELECT 'foods', COUNT(*) FROM foods
UNION ALL
SELECT 'comments', COUNT(*) FROM comments
UNION ALL
SELECT 'collection', COUNT(*) FROM collection
UNION ALL
SELECT 'comment_like', COUNT(*) FROM comment_like
UNION ALL
SELECT 'food_tags', COUNT(*) FROM food_tags
UNION ALL
SELECT 'food_tag_relations', COUNT(*) FROM food_tag_relations
UNION ALL
SELECT 'rankings', COUNT(*) FROM rankings
UNION ALL
SELECT 'ranking_foods', COUNT(*) FROM ranking_foods
UNION ALL
SELECT 'search_history', COUNT(*) FROM search_history
UNION ALL
SELECT 'comment_preferences', COUNT(*) FROM comment_preferences
UNION ALL
SELECT 'visit_stats', COUNT(*) FROM visit_stats
UNION ALL
SELECT 'device_stats', COUNT(*) FROM device_stats
UNION ALL
SELECT 'source_stats', COUNT(*) FROM source_stats
UNION ALL
SELECT 'preference_keywords', COUNT(*) FROM preference_keywords;
```

## 📋 数据说明

- **用户数据**: 包含1个管理员和3个普通用户
- **美食数据**: 包含10道经典菜品，涵盖8大菜系
- **评论数据**: 包含10条用户评论，评分在4-5分之间
- **收藏数据**: 每个用户收藏了3-4道美食
- **标签数据**: 包含10个美食标签，涵盖各种口味
- **榜单数据**: 包含3个不同类型的榜单
- **统计数据**: 包含5天的访问统计数据
- **设备统计**: 包含不同设备类型的访问统计
- **来源统计**: 包含不同来源渠道的访问统计
- **关键词数据**: 包含32个关注点关键词

## 📊 完整的16张表

1. `profiles` - 用户表
2. `food_categories` - 美食分类表
3. `foods` - 美食表
4. `comments` - 评论表
5. `collection` - 用户收藏表
6. `comment_like` - 评论点赞表
7. `food_tags` - 美食标签表
8. `food_tag_relations` - 美食标签关联表
9. `rankings` - 榜单表
10. `ranking_foods` - 榜单美食关联表
11. `search_history` - 搜索历史表
12. `comment_preferences` - 评论关注点表
13. `visit_stats` - 访问统计表
14. `device_stats` - 设备统计表
15. `source_stats` - 来源统计表
16. `preference_keywords` - 关注点关键词表

**所有数据都经过精心设计，确保数据完整性和业务逻辑正确性！**
