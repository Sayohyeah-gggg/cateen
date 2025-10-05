# 美食评估管理系统 - 补充缺少的表和测试数据

## 📋 缺少的表建表语句

### 1. 关注点关键词表 (preference_keywords)
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

## 📊 补充测试数据

### 1. 用户表补充数据
```sql
-- 补充用户测试数据
INSERT INTO `profiles` (`id`, `user_id`, `username`, `password_hash`, `phone`, `role`, `status`, `wechat_openid`, `nickname`, `avatar`, `created_at`, `updated_at`) VALUES
('user004', 'user_004', 'testuser4', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138004', 'user', 'active', 'test_openid_004', '美食探索者', 'https://example.com/user4-avatar.jpg', NOW(), NOW()),
('user005', 'user_005', 'testuser5', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138005', 'user', 'active', 'test_openid_005', '吃货小张', 'https://example.com/user5-avatar.jpg', NOW(), NOW()),
('user006', 'user_006', 'testuser6', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138006', 'user', 'active', 'test_openid_006', '美食评论家', 'https://example.com/user6-avatar.jpg', NOW(), NOW());
```

### 2. 美食分类补充数据
```sql
-- 补充美食分类测试数据
INSERT INTO `food_categories` (`id`, `name`, `description`, `icon`, `sort_order`, `created_at`) VALUES
('cat011', '东北菜', '东北地区特色菜系，口味偏重', '🥘', 11, NOW()),
('cat012', '西北菜', '西北地区特色菜系，面食丰富', '🍜', 12, NOW()),
('cat013', '云南菜', '云南地区特色菜系，口味独特', '🌶️', 13, NOW()),
('cat014', '新疆菜', '新疆地区特色菜系，羊肉为主', '🐑', 14, NOW()),
('cat015', '西藏菜', '西藏地区特色菜系，高原特色', '🏔️', 15, NOW());
```

### 3. 美食补充数据
```sql
-- 补充美食测试数据
INSERT INTO `foods` (`id`, `name`, `description`, `category_id`, `image_url`, `price`, `rating`, `rating_count`, `status`, `created_by`, `created_at`, `updated_at`) VALUES
('food011', '锅包肉', '东北名菜，外酥内嫩，酸甜可口', 'cat011', 'https://example.com/images/guobao-rou.jpg', 32.00, 4.40, 89, 'active', 'admin001', NOW(), NOW()),
('food012', '羊肉泡馍', '陕西名吃，汤浓肉香，馍片劲道', 'cat012', 'https://example.com/images/yangrou-paomo.jpg', 25.00, 4.50, 156, 'active', 'admin001', NOW(), NOW()),
('food013', '过桥米线', '云南名吃，汤鲜味美，米线爽滑', 'cat013', 'https://example.com/images/guoqiao-mixian.jpg', 28.00, 4.60, 134, 'active', 'admin001', NOW(), NOW()),
('food014', '大盘鸡', '新疆名菜，鸡肉嫩滑，土豆软糯', 'cat014', 'https://example.com/images/dapan-ji.jpg', 45.00, 4.70, 98, 'active', 'admin001', NOW(), NOW()),
('food015', '青稞酒', '西藏特产，酒香浓郁，高原特色', 'cat015', 'https://example.com/images/qingke-jiu.jpg', 35.00, 4.30, 67, 'active', 'admin001', NOW(), NOW());
```

### 4. 评论补充数据
```sql
-- 补充评论测试数据
INSERT INTO `comments` (`id`, `food_id`, `user_id`, `content`, `rating`, `images`, `status`, `created_at`) VALUES
('comment011', 'food011', 'user004', '锅包肉真的很棒！外酥内嫩，酸甜味刚好，不会太腻。', 5, 'https://example.com/images/comment11.jpg', 'approved', NOW()),
('comment012', 'food012', 'user005', '羊肉泡馍汤很浓，羊肉很香，馍片很劲道，推荐！', 5, NULL, 'approved', NOW()),
('comment013', 'food013', 'user006', '过桥米线汤很鲜，米线很爽滑，配菜很丰富。', 4, 'https://example.com/images/comment12.jpg', 'approved', NOW()),
('comment014', 'food014', 'user004', '大盘鸡鸡肉很嫩，土豆很软糯，分量很足。', 5, NULL, 'approved', NOW()),
('comment015', 'food015', 'user005', '青稞酒很香，有高原特色，但是度数有点高。', 4, 'https://example.com/images/comment13.jpg', 'approved', NOW());
```

### 5. 用户收藏补充数据
```sql
-- 补充用户收藏测试数据
INSERT INTO `collection` (`id`, `user_id`, `food_id`, `created_at`) VALUES
('coll011', 'user004', 'food011', NOW()),
('coll012', 'user004', 'food012', NOW()),
('coll013', 'user004', 'food013', NOW()),
('coll014', 'user005', 'food012', NOW()),
('coll015', 'user005', 'food014', NOW()),
('coll016', 'user005', 'food015', NOW()),
('coll017', 'user006', 'food013', NOW()),
('coll018', 'user006', 'food014', NOW()),
('coll019', 'user006', 'food015', NOW()),
('coll020', 'user001', 'food011', NOW());
```

### 6. 评论点赞补充数据
```sql
-- 补充评论点赞测试数据
INSERT INTO `comment_like` (`id`, `user_id`, `comment_id`, `created_at`) VALUES
('like011', 'user005', 'comment011', NOW()),
('like012', 'user006', 'comment011', NOW()),
('like013', 'user004', 'comment012', NOW()),
('like014', 'user006', 'comment012', NOW()),
('like015', 'user004', 'comment013', NOW()),
('like016', 'user005', 'comment013', NOW()),
('like017', 'user005', 'comment014', NOW()),
('like018', 'user006', 'comment014', NOW()),
('like019', 'user004', 'comment015', NOW()),
('like020', 'user006', 'comment015', NOW());
```

### 7. 美食标签补充数据
```sql
-- 补充美食标签测试数据
INSERT INTO `food_tags` (`id`, `name`, `color`, `created_at`) VALUES
('tag011', '酸甜', '#fa541c', NOW()),
('tag012', '汤浓', '#13c2c2', NOW()),
('tag013', '爽滑', '#52c41a', NOW()),
('tag014', '嫩滑', '#1890ff', NOW()),
('tag015', '香浓', '#722ed1', NOW()),
('tag016', '劲道', '#fa8c16', NOW()),
('tag017', '软糯', '#a0d911', NOW()),
('tag018', '分量足', '#2f54eb', NOW()),
('tag019', '高原特色', '#eb2f96', NOW()),
('tag020', '度数高', '#f5222d', NOW());
```

### 8. 美食标签关联补充数据
```sql
-- 补充美食标签关联测试数据
INSERT INTO `food_tag_relations` (`id`, `food_id`, `tag_id`, `created_at`) VALUES
('ftr011', 'food011', 'tag011', NOW()),
('ftr012', 'food011', 'tag008', NOW()),
('ftr013', 'food012', 'tag012', NOW()),
('ftr014', 'food012', 'tag016', NOW()),
('ftr015', 'food013', 'tag013', NOW()),
('ftr016', 'food013', 'tag012', NOW()),
('ftr017', 'food014', 'tag014', NOW()),
('ftr018', 'food014', 'tag017', NOW()),
('ftr019', 'food015', 'tag015', NOW()),
('ftr020', 'food015', 'tag019', NOW());
```

### 9. 榜单补充数据
```sql
-- 补充榜单测试数据
INSERT INTO `rankings` (`id`, `title`, `description`, `type`, `status`, `created_by`, `created_at`, `updated_at`) VALUES
('rank004', '地方特色美食', '各地特色美食推荐榜单', 'regional', 'active', 'admin001', NOW(), NOW()),
('rank005', '价格实惠美食', '价格在30元以下的高性价比美食', 'budget', 'active', 'admin001', NOW(), NOW()),
('rank006', '新用户推荐', '适合新用户尝试的经典美食', 'newbie', 'active', 'admin001', NOW(), NOW());
```

### 10. 榜单美食关联补充数据
```sql
-- 补充榜单美食关联测试数据
INSERT INTO `ranking_foods` (`id`, `ranking_id`, `food_id`, `rank_position`, `created_at`) VALUES
('rf009', 'rank004', 'food011', 1, NOW()),
('rf010', 'rank004', 'food012', 2, NOW()),
('rf011', 'rank004', 'food013', 3, NOW()),
('rf012', 'rank005', 'food004', 1, NOW()),
('rf013', 'rank005', 'food010', 2, NOW()),
('rf014', 'rank005', 'food009', 3, NOW()),
('rf015', 'rank006', 'food001', 1, NOW()),
('rf016', 'rank006', 'food002', 2, NOW()),
('rf017', 'rank006', 'food003', 3, NOW());
```

### 11. 搜索历史补充数据
```sql
-- 补充搜索历史测试数据
INSERT INTO `search_history` (`id`, `user_id`, `keyword`, `search_count`, `last_search_time`, `created_at`, `updated_at`) VALUES
('sh007', 'user004', '锅包肉', 2, NOW(), NOW(), NOW()),
('sh008', 'user004', '东北菜', 1, NOW(), NOW(), NOW()),
('sh009', 'user005', '羊肉泡馍', 3, NOW(), NOW(), NOW()),
('sh010', 'user005', '陕西菜', 1, NOW(), NOW(), NOW()),
('sh011', 'user006', '过桥米线', 2, NOW(), NOW(), NOW()),
('sh012', 'user006', '云南菜', 1, NOW(), NOW(), NOW()),
('sh013', 'user001', '大盘鸡', 1, NOW(), NOW(), NOW()),
('sh014', 'user002', '新疆菜', 1, NOW(), NOW(), NOW()),
('sh015', 'user003', '青稞酒', 1, NOW(), NOW(), NOW());
```

### 12. 评论关注点补充数据
```sql
-- 补充评论关注点测试数据
INSERT INTO `comment_preferences` (`id`, `comment_id`, `preference_type`, `score`, `created_at`) VALUES
('cp011', 'comment011', 'taste', 5, NOW()),
('cp012', 'comment011', 'price', 4, NOW()),
('cp013', 'comment011', 'environment', 4, NOW()),
('cp014', 'comment011', 'service', 5, NOW()),
('cp015', 'comment012', 'taste', 5, NOW()),
('cp016', 'comment012', 'price', 4, NOW()),
('cp017', 'comment012', 'environment', 5, NOW()),
('cp018', 'comment012', 'service', 4, NOW()),
('cp019', 'comment013', 'taste', 4, NOW()),
('cp020', 'comment013', 'price', 5, NOW());
```

### 13. 关注点关键词测试数据
```sql
-- 插入关注点关键词测试数据
INSERT INTO `preference_keywords` (`id`, `keyword`, `preference_type`, `weight`, `usage_count`, `status`, `created_at`, `updated_at`) VALUES
('pk001', '好吃', 'taste', 1.00, 25, 'active', NOW(), NOW()),
('pk002', '美味', 'taste', 1.00, 20, 'active', NOW(), NOW()),
('pk003', '香', 'taste', 0.90, 18, 'active', NOW(), NOW()),
('pk004', '甜', 'taste', 0.80, 15, 'active', NOW(), NOW()),
('pk005', '辣', 'taste', 0.85, 16, 'active', NOW(), NOW()),
('pk006', '咸', 'taste', 0.75, 12, 'active', NOW(), NOW()),
('pk007', '酸', 'taste', 0.70, 10, 'active', NOW(), NOW()),
('pk008', '便宜', 'price', 1.00, 22, 'active', NOW(), NOW()),
('pk009', '实惠', 'price', 0.95, 19, 'active', NOW(), NOW()),
('pk010', '性价比高', 'price', 0.90, 17, 'active', NOW(), NOW()),
('pk011', '贵', 'price', 0.80, 14, 'active', NOW(), NOW()),
('pk012', '划算', 'price', 0.85, 13, 'active', NOW(), NOW()),
('pk013', '干净', 'environment', 1.00, 21, 'active', NOW(), NOW()),
('pk014', '整洁', 'environment', 0.95, 18, 'active', NOW(), NOW()),
('pk015', '舒适', 'environment', 0.90, 16, 'active', NOW(), NOW()),
('pk016', '温馨', 'environment', 0.85, 14, 'active', NOW(), NOW()),
('pk017', '安静', 'environment', 0.80, 12, 'active', NOW(), NOW()),
('pk018', '服务好', 'service', 1.00, 23, 'active', NOW(), NOW()),
('pk019', '态度好', 'service', 0.95, 20, 'active', NOW(), NOW()),
('pk020', '热情', 'service', 0.90, 17, 'active', NOW(), NOW()),
('pk021', '专业', 'service', 0.85, 15, 'active', NOW(), NOW()),
('pk022', '耐心', 'service', 0.80, 13, 'active', NOW(), NOW()),
('pk023', '推荐', 'other', 1.00, 24, 'active', NOW(), NOW()),
('pk024', '值得', 'other', 0.95, 21, 'active', NOW(), NOW()),
('pk025', '不错', 'other', 0.90, 19, 'active', NOW(), NOW()),
('pk026', '满意', 'other', 0.85, 17, 'active', NOW(), NOW()),
('pk027', '喜欢', 'other', 0.80, 15, 'active', NOW(), NOW()),
('pk028', '下次还来', 'other', 0.75, 13, 'active', NOW(), NOW()),
('pk029', '分量足', 'other', 0.70, 11, 'active', NOW(), NOW()),
('pk030', '新鲜', 'other', 0.65, 9, 'active', NOW(), NOW()),
('pk031', '正宗', 'other', 0.60, 8, 'active', NOW(), NOW()),
('pk032', '特色', 'other', 0.55, 7, 'active', NOW(), NOW());
```

### 14. 设备统计补充数据
```sql
-- 补充设备统计测试数据
INSERT INTO `device_stats` (`id`, `stat_date`, `device_type`, `visit_count`, `created_at`, `updated_at`) VALUES
('ds001', '2024-01-01', 'mobile', 120, NOW(), NOW()),
('ds002', '2024-01-01', 'desktop', 30, NOW(), NOW()),
('ds003', '2024-01-02', 'mobile', 140, NOW(), NOW()),
('ds004', '2024-01-02', 'desktop', 40, NOW(), NOW()),
('ds005', '2024-01-03', 'mobile', 160, NOW(), NOW()),
('ds006', '2024-01-03', 'desktop', 40, NOW(), NOW()),
('ds007', '2024-01-04', 'mobile', 180, NOW(), NOW()),
('ds008', '2024-01-04', 'desktop', 40, NOW(), NOW()),
('ds009', '2024-01-05', 'mobile', 200, NOW(), NOW()),
('ds010', '2024-01-05', 'desktop', 50, NOW(), NOW());
```

### 15. 来源统计补充数据
```sql
-- 补充来源统计测试数据
INSERT INTO `source_stats` (`id`, `stat_date`, `source_type`, `visit_count`, `created_at`, `updated_at`) VALUES
('ss001', '2024-01-01', 'direct', 80, NOW(), NOW()),
('ss002', '2024-01-01', 'search', 50, NOW(), NOW()),
('ss003', '2024-01-01', 'social', 20, NOW(), NOW()),
('ss004', '2024-01-02', 'direct', 90, NOW(), NOW()),
('ss005', '2024-01-02', 'search', 60, NOW(), NOW()),
('ss006', '2024-01-02', 'social', 30, NOW(), NOW()),
('ss007', '2024-01-03', 'direct', 100, NOW(), NOW()),
('ss008', '2024-01-03', 'search', 70, NOW(), NOW()),
('ss009', '2024-01-03', 'social', 30, NOW(), NOW()),
('ss010', '2024-01-04', 'direct', 110, NOW(), NOW()),
('ss011', '2024-01-04', 'search', 80, NOW(), NOW()),
('ss012', '2024-01-04', 'social', 30, NOW(), NOW()),
('ss013', '2024-01-05', 'direct', 120, NOW(), NOW()),
('ss014', '2024-01-05', 'search', 90, NOW(), NOW()),
('ss015', '2024-01-05', 'social', 40, NOW(), NOW());
```

### 16. 访问统计补充数据
```sql
-- 补充访问统计测试数据
INSERT INTO `visit_stats` (`id`, `stat_date`, `visits`, `unique_visitors`, `page_views`, `bounce_rate`, `avg_stay_time`, `created_at`, `updated_at`) VALUES
('vs006', '2024-01-06', 280, 220, 560, 12, 280, NOW(), NOW()),
('vs007', '2024-01-07', 300, 240, 600, 10, 300, NOW(), NOW()),
('vs008', '2024-01-08', 320, 260, 640, 8, 320, NOW(), NOW()),
('vs009', '2024-01-09', 350, 280, 700, 6, 350, NOW(), NOW()),
('vs010', '2024-01-10', 380, 300, 760, 5, 380, NOW(), NOW()),
('vs011', '2024-01-11', 400, 320, 800, 4, 400, NOW(), NOW()),
('vs012', '2024-01-12', 420, 340, 840, 3, 420, NOW(), NOW()),
('vs013', '2024-01-13', 450, 360, 900, 2, 450, NOW(), NOW()),
('vs014', '2024-01-14', 480, 380, 960, 1, 480, NOW(), NOW()),
('vs015', '2024-01-15', 500, 400, 1000, 0, 500, NOW(), NOW());
```

## 🚀 执行说明

### 1. 创建缺少的表
```sql
-- 如果preference_keywords表不存在，执行以下语句
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

### 2. 插入测试数据
按照上述顺序执行所有测试数据插入语句

### 3. 验证数据
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
SELECT 'preference_keywords', COUNT(*) FROM preference_keywords
UNION ALL
SELECT 'device_stats', COUNT(*) FROM device_stats
UNION ALL
SELECT 'source_stats', COUNT(*) FROM source_stats
UNION ALL
SELECT 'visit_stats', COUNT(*) FROM visit_stats;
```

## 📋 数据说明

- **用户数据**: 新增3个用户，总共6个用户
- **美食数据**: 新增5道地方特色美食，总共15道美食
- **评论数据**: 新增5条评论，总共15条评论
- **收藏数据**: 新增10条收藏记录
- **标签数据**: 新增10个美食标签
- **榜单数据**: 新增3个榜单
- **关键词数据**: 新增32个关注点关键词
- **统计数据**: 补充各统计表的数据

**现在你的16张表都有完整的测试数据了！**
