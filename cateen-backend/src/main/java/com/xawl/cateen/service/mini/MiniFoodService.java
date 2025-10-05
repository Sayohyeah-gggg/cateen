package com.xawl.cateen.service.mini;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodCategoryMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.service.SearchHistoryService;
import com.xawl.cateen.vo.mini.MiniFoodVO;
import com.xawl.cateen.vo.mini.MiniFoodDetailVO;
import com.xawl.cateen.vo.mini.MiniCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小程序美食服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniFoodService {
    
    private final FoodMapper foodMapper;
    private final FoodCategoryMapper foodCategoryMapper;
    private final CommentMapper commentMapper;
    private final CollectionService collectionService;
    private final SearchHistoryService searchHistoryService;
    
    /**
     * 获取美食列表
     */
    public Page<MiniFoodVO> getFoods(String userId, Integer page, Integer limit, 
                                      String category, String keyword, String sortBy) {
        // 记录搜索历史
        if (userId != null && StrUtil.isNotBlank(keyword)) {
            searchHistoryService.recordSearch(userId, keyword);
        }
        
        Page<Food> foodPage = new Page<>(page, limit);
        
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        
        // 分类筛选
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Food::getCategoryId, category);
        }
        
        // 关键词搜索
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Food::getName, keyword)
                    .or()
                    .like(Food::getDescription, keyword);
        }
        
        // 排序
        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Food::getRating);
        } else if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(Food::getPrice);
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc(Food::getPrice);
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc(Food::getCreatedAt);
        }
        
        foodMapper.selectPage(foodPage, wrapper);
        
        // 获取美食ID列表
        List<String> foodIds = foodPage.getRecords().stream()
                .map(Food::getId)
                .collect(Collectors.toList());
        
        // 批量检查收藏状态
        Map<String, Boolean> collectedMap = collectionService.batchCheckCollected(userId, foodIds);
        
        // 转换为VO
        List<MiniFoodVO> foodVOs = foodPage.getRecords().stream()
                .map(food -> {
                    // 获取最热评论
                    String hotComment = getHotComment(food.getId());
                    
                    return MiniFoodVO.builder()
                            .id(food.getId())
                            .name(food.getName())
                            .imageUrl(food.getImageUrl())
                            .categoryName(getCategoryName(food.getCategoryId()))
                            .rating(food.getRating())
                            .ratingCount(food.getRatingCount())
                            .price(food.getPrice())
                            .isCollected(collectedMap.getOrDefault(food.getId(), false))
                            .hotComment(hotComment)
                            .build();
                })
                .collect(Collectors.toList());
        
        Page<MiniFoodVO> result = new Page<>(page, limit, foodPage.getTotal());
        result.setRecords(foodVOs);
        
        return result;
    }
    
    /**
     * 获取美食详情
     */
    public MiniFoodDetailVO getFoodDetail(String userId, String foodId) {
        Food food = foodMapper.selectById(foodId);
        if (food == null) {
            return null;
        }
        
        // 检查收藏状态
        boolean isCollected = collectionService.isCollected(userId, foodId);
        
        // 统计收藏数
        Integer collectionCount = collectionService.countFoodCollections(foodId);
        
        // 统计评论数
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getFoodId, foodId);
        Long commentCount = commentMapper.selectCount(commentWrapper);
        
        return MiniFoodDetailVO.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .images(List.of(food.getImageUrl().split(",")))
                .categoryId(food.getCategoryId())
                .categoryName(getCategoryName(food.getCategoryId()))
                .price(food.getPrice())
                .rating(food.getRating())
                .ratingCount(food.getRatingCount())
                .isCollected(isCollected)
                .collectionCount(collectionCount)
                .commentCount(commentCount.intValue())
                .build();
    }
    
    /**
     * 获取分类列表
     */
    public List<MiniCategoryVO> getCategories() {
        List<FoodCategory> categories = foodCategoryMapper.selectList(null);
        
        return categories.stream()
                .map(category -> {
                    // 统计该分类下的美食数量
                    LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Food::getCategoryId, category.getId());
                    Long count = foodMapper.selectCount(wrapper);
                    
                    return MiniCategoryVO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .icon(category.getIcon())
                            .foodCount(count.intValue())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取热门搜索词
     */
    public List<String> getHotKeywords() {
        // 从搜索历史表统计真实数据
        List<String> hotKeywords = searchHistoryService.getHotKeywords(8);
        
        // 如果数据库中没有数据，返回预设的热门关键词
        if (hotKeywords.isEmpty()) {
            return List.of("川菜", "粤菜", "火锅", "烧烤", "甜点", "快餐", "日料", "西餐");
        }
        
        return hotKeywords;
    }
    
    /**
     * 获取用户搜索历史
     */
    public List<String> getUserSearchHistory(String userId) {
        if (userId == null) {
            return List.of();
        }
        return searchHistoryService.getUserSearchHistory(userId, 10);
    }
    
    /**
     * 清除用户搜索历史
     */
    public void clearUserSearchHistory(String userId) {
        if (userId != null) {
            searchHistoryService.clearUserSearchHistory(userId);
        }
    }
    
    /**
     * 获取分类名称
     */
    private String getCategoryName(String categoryId) {
        if (StrUtil.isBlank(categoryId)) {
            return "";
        }
        FoodCategory category = foodCategoryMapper.selectById(categoryId);
        return category != null ? category.getName() : "";
    }
    
    /**
     * 获取最热评论
     */
    private String getHotComment(String foodId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFoodId, foodId)
                .orderByDesc(Comment::getRating)
                .last("LIMIT 1");
        
        Comment comment = commentMapper.selectOne(wrapper);
        return comment != null ? comment.getContent() : null;
    }
}
