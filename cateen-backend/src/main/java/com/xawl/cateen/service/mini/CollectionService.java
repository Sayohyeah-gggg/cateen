package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.UserCollection;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.mapper.CollectionMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.FoodCategoryMapper;
import com.xawl.cateen.vo.mini.MiniFoodVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收藏服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {
    
    private final CollectionMapper collectionMapper;
    private final FoodMapper foodMapper;
    private final FoodCategoryMapper foodCategoryMapper;
    
    /**
     * 获取用户收藏列表
     */
    public Page<MiniFoodVO> getCollections(String userId, Integer page, Integer limit, String category) {
        Page<UserCollection> collectionPage = new Page<>(page, limit);
        
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
                .orderByDesc(UserCollection::getCreatedAt);
        
        collectionMapper.selectPage(collectionPage, wrapper);
        
        // 获取美食ID列表
        List<String> foodIds = collectionPage.getRecords().stream()
                .map(UserCollection::getFoodId)
                .collect(Collectors.toList());
        
        if (foodIds.isEmpty()) {
            return new Page<>(page, limit, 0);
        }
        
        // 查询美食信息
        LambdaQueryWrapper<Food> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.in(Food::getId, foodIds);
        if (category != null && !category.isEmpty()) {
            foodWrapper.eq(Food::getCategoryId, category);
        }
        
        List<Food> foods = foodMapper.selectList(foodWrapper);
        
        // 获取所有分类信息
        Set<String> categoryIds = foods.stream()
                .map(Food::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<String, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<FoodCategory> categories = foodCategoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(
                            FoodCategory::getId,
                            FoodCategory::getName,
                            (existing, replacement) -> existing
                    ));
        }
        
        // 转换为VO
        final Map<String, String> finalCategoryNameMap = categoryNameMap;
        List<MiniFoodVO> foodVOs = foods.stream()
                .map(food -> MiniFoodVO.builder()
                        .id(food.getId())
                        .name(food.getName())
                        .imageUrl(food.getImageUrl())
                        .categoryName(finalCategoryNameMap.getOrDefault(food.getCategoryId(), "未知分类"))
                        .rating(food.getRating())
                        .ratingCount(food.getRatingCount())
                        .price(food.getPrice())
                        .isCollected(true)
                        .build())
                .collect(Collectors.toList());
        
        Page<MiniFoodVO> result = new Page<>(page, limit, collectionPage.getTotal());
        result.setRecords(foodVOs);
        
        return result;
    }
    
    /**
     * 添加收藏
     */
    @Transactional
    public void addCollection(String userId, String foodId) {
        // 检查是否已收藏
        Integer exists = collectionMapper.existsByUserIdAndFoodId(userId, foodId);
        if (exists > 0) {
            log.warn("美食已收藏，userId: {}, foodId: {}", userId, foodId);
            return;
        }
        
        // 添加收藏记录
        UserCollection collection = UserCollection.builder()
                .userId(userId)
                .foodId(foodId)
                .createdAt(LocalDateTime.now())
                .build();
        
        collectionMapper.insert(collection);
        log.info("添加收藏成功，userId: {}, foodId: {}", userId, foodId);
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeCollection(String userId, String foodId) {
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getFoodId, foodId);
        
        int count = collectionMapper.delete(wrapper);
        log.info("取消收藏，userId: {}, foodId: {}, count: {}", userId, foodId, count);
    }
    
    /**
     * 检查是否已收藏
     */
    public boolean isCollected(String userId, String foodId) {
        log.info("检查收藏状态 - userId: {}, foodId: {}", userId, foodId);
        
        if (userId == null) {
            log.warn("userId为null，返回未收藏状态");
            return false; // 未登录用户默认未收藏
        }
        
        Integer exists = collectionMapper.existsByUserIdAndFoodId(userId, foodId);
        boolean result = exists > 0;
        
        log.info("收藏状态查询结果 - userId: {}, foodId: {}, exists: {}, result: {}", 
                 userId, foodId, exists, result);
        
        return result;
    }
    
    /**
     * 批量检查是否已收藏
     */
    public Map<String, Boolean> batchCheckCollected(String userId, List<String> foodIds) {
        if (foodIds == null || foodIds.isEmpty()) {
            return new HashMap<>();
        }
        
        if (userId == null) {
            // 未登录用户默认全部未收藏
            return foodIds.stream()
                    .collect(Collectors.toMap(
                            foodId -> foodId,
                            foodId -> false
                    ));
        }
        
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
                .in(UserCollection::getFoodId, foodIds);
        
        List<UserCollection> collections = collectionMapper.selectList(wrapper);
        Set<String> collectedFoodIds = collections.stream()
                .map(UserCollection::getFoodId)
                .collect(Collectors.toSet());
        
        return foodIds.stream()
                .collect(Collectors.toMap(
                        foodId -> foodId,
                        collectedFoodIds::contains
                ));
    }
    
    /**
     * 统计用户收藏数
     */
    public Integer countUserCollections(String userId) {
        return collectionMapper.countByUserId(userId);
    }
    
    /**
     * 统计美食收藏数
     */
    public Integer countFoodCollections(String foodId) {
        return collectionMapper.countByFoodId(foodId);
    }
}
