package com.xawl.cateen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.constant.CommentStatusConstants;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 管理服务实现类
 *
 * @author xawl
 * @date 2025-10-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final FoodMapper foodMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFoodRatings() {
        log.info("开始同步美食评分统计...");
        
        // 获取所有美食
        List<Food> foods = foodMapper.selectList(null);
        int updatedCount = 0;
        
        for (Food food : foods) {
            // 统计该美食的所有已通过评论
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getFoodId, food.getId())
                   .eq(Comment::getStatus, CommentStatusConstants.APPROVED);
            
            List<Comment> comments = commentMapper.selectList(wrapper);
            
            int totalComments = comments.size();
            food.setRatingCount(totalComments);
            
            if (totalComments > 0) {
                // 计算平均评分
                double avgRating = comments.stream()
                    .mapToInt(Comment::getRating)
                    .average()
                    .orElse(0.0);
                food.setRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
            } else {
                food.setRating(BigDecimal.ZERO);
            }
            
            foodMapper.updateById(food);
            updatedCount++;
            
            log.debug("更新美食评分统计: id={}, name={}, ratingCount={}, rating={}", 
                     food.getId(), food.getName(), food.getRatingCount(), food.getRating());
        }
        
        log.info("完成同步美食评分统计，共更新 {} 个美食", updatedCount);
        return updatedCount;
    }
}

