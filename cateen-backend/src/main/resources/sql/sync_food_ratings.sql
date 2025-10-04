-- 同步美食评分统计
-- 根据 comments 表中的实际评论更新 foods 表的评分和评论数

UPDATE foods f
SET 
    rating_count = (
        SELECT COUNT(*) 
        FROM comments c 
        WHERE c.food_id = f.id 
        AND c.status = 'approved'
    ),
    rating = (
        SELECT ROUND(AVG(c.rating), 1)
        FROM comments c 
        WHERE c.food_id = f.id 
        AND c.status = 'approved'
    )
WHERE EXISTS (
    SELECT 1 
    FROM comments c 
    WHERE c.food_id = f.id
);

-- 对于没有评论的美食，设置为0
UPDATE foods 
SET rating_count = 0, rating = 0
WHERE NOT EXISTS (
    SELECT 1 
    FROM comments c 
    WHERE c.food_id = foods.id
    AND c.status = 'approved'
);

