USE `cateen`;

-- 插入测试用户数据（密码都是BCrypt加密后的）
-- admin/admin123 和 testuser/123456
INSERT INTO `profiles` (`id`, `user_id`, `username`, `password_hash`, `phone`, `role`, `status`) VALUES
('1', 'user_admin_001', 'admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyE3xLuPBG1y', '13800138000', 'admin', 'active'),
('2', 'user_test_001', 'testuser', '$2a$12$vgZvVw3qvPxFpd0gKfPCduFJe8hW7eUjYzZ5HxVXf8ov3t5Pu0TNC', '13800138001', 'user', 'active');

-- 插入美食分类数据
INSERT INTO `food_categories` (`id`, `name`, `description`, `sort_order`) VALUES
('cat_001', '主食', '米饭、面条等主食类', 1),
('cat_002', '菜肴', '各类炒菜、炖菜', 2),
('cat_003', '汤羹', '各类汤品和羹类', 3),
('cat_004', '小吃', '特色小吃和零食', 4),
('cat_005', '饮品', '饮料和甜品', 5);

-- 插入美食标签数据
INSERT INTO `food_tags` (`id`, `name`, `color`) VALUES
('tag_001', '正宗', '#ff6b35'),
('tag_002', '好吃', '#f7931e'),
('tag_003', '实惠', '#10b981'),
('tag_004', '辣味', '#ef4444'),
('tag_005', '清淡', '#3b82f6'),
('tag_006', '推荐', '#8b5cf6');

-- 插入美食数据
INSERT INTO `foods` (`id`, `name`, `description`, `category_id`, `image_url`, `price`, `rating`, `rating_count`, `status`, `created_by`) VALUES
('food_001', '红烧肉', '经典红烧肉，肥而不腻，色泽红亮', 'cat_002', 'https://picsum.photos/400/300?random=1', 38.00, 4.8, 128, 'active', '1'),
('food_002', '糖醋排骨', '酸甜可口的糖醋排骨', 'cat_002', 'https://picsum.photos/400/300?random=2', 45.00, 4.7, 95, 'active', '1'),
('food_003', '扬州炒饭', '粒粒分明的扬州炒饭', 'cat_001', 'https://picsum.photos/400/300?random=3', 18.00, 4.5, 156, 'active', '1'),
('food_004', '麻辣香锅', '麻辣鲜香的麻辣香锅', 'cat_002', 'https://picsum.photos/400/300?random=4', 52.00, 4.6, 89, 'active', '1'),
('food_005', '老火靓汤', '营养滋补的老火汤', 'cat_003', 'https://picsum.photos/400/300?random=5', 28.00, 4.4, 72, 'active', '1');

-- 插入美食标签关联
INSERT INTO `food_tag_relations` (`id`, `food_id`, `tag_id`) VALUES
('ftr_001', 'food_001', 'tag_001'),
('ftr_002', 'food_001', 'tag_002'),
('ftr_003', 'food_002', 'tag_002'),
('ftr_004', 'food_002', 'tag_006'),
('ftr_005', 'food_003', 'tag_003'),
('ftr_006', 'food_004', 'tag_004'),
('ftr_007', 'food_004', 'tag_006'),
('ftr_008', 'food_005', 'tag_005');

-- 插入评论数据
INSERT INTO `comments` (`id`, `food_id`, `user_id`, `content`, `rating`, `status`) VALUES
('cmt_001', 'food_001', '2', '味道很好，肥而不腻，推荐！', 5, 'approved'),
('cmt_002', 'food_001', '2', '经典美食，值得一试', 5, 'approved'),
('cmt_003', 'food_002', '2', '酸甜适中，很好吃', 5, 'approved'),
('cmt_004', 'food_003', '2', '炒饭很香，料很足', 4, 'approved'),
('cmt_005', 'food_004', '2', '非常辣，但是很过瘾', 5, 'approved');

-- 插入榜单数据
INSERT INTO `rankings` (`id`, `title`, `description`, `type`, `status`, `created_by`) VALUES
('rank_001', '本周热门美食榜', '本周最受欢迎的美食排行', 'weekly', 'active', '1'),
('rank_002', '经典菜品推荐', '传统经典美食推荐', 'recommend', 'active', '1');

-- 插入榜单美食关联
INSERT INTO `ranking_foods` (`id`, `ranking_id`, `food_id`, `rank_position`) VALUES
('rf_001', 'rank_001', 'food_001', 1),
('rf_002', 'rank_001', 'food_002', 2),
('rf_003', 'rank_001', 'food_003', 3),
('rf_004', 'rank_002', 'food_001', 1),
('rf_005', 'rank_002', 'food_005', 2');

