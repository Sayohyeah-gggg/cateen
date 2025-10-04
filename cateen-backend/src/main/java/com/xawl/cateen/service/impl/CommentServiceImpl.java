package com.xawl.cateen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.constant.CommentStatusConstants;
import com.xawl.cateen.dto.CommentDTO;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.service.CommentService;
import com.xawl.cateen.util.UserContext;
import com.xawl.cateen.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 评论服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final FoodMapper foodMapper;
    private final com.xawl.cateen.mapper.CommentPreferenceMapper commentPreferenceMapper;

    @Override
    public Page<CommentVO> getCommentPage(Long pageNum, Long pageSize, String keyword, 
                                           String foodId, String userId, String status, Integer rating) {
        Page<CommentVO> page = new Page<>(pageNum, pageSize);
        commentMapper.selectCommentPage(page, keyword, foodId, userId, status, rating);
        return page;
    }

    @Override
    public CommentVO getCommentDetail(String id) {
        CommentVO commentVO = commentMapper.selectCommentDetailById(id);
        if (commentVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        return commentVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(CommentDTO dto) {
        Comment comment = new Comment();
        comment.setId(IdUtil.getSnowflakeNextIdStr());
        comment.setFoodId(dto.getFoodId());
        comment.setUserId(UserContext.getUserId());
        comment.setContent(dto.getContent());
        comment.setRating(dto.getRating());
        comment.setStatus(CommentStatusConstants.PENDING);

        commentMapper.insert(comment);
        
        // 保存多维度评分
        if (dto.getPreferences() != null && !dto.getPreferences().isEmpty()) {
            saveCommentPreferences(comment.getId(), dto.getPreferences());
        }
        
        // 如果评论自动通过，更新美食评分统计
        if (CommentStatusConstants.APPROVED.equals(comment.getStatus())) {
            updateFoodRatingStats(dto.getFoodId());
        }

        return getCommentDetail(comment.getId());
    }
    
    /**
     * 保存评论的多维度评分
     */
    private void saveCommentPreferences(String commentId, java.util.Map<String, Integer> preferences) {
        preferences.forEach((type, score) -> {
            if (score != null && score > 0 && score <= 5) {
                com.xawl.cateen.entity.CommentPreference preference = new com.xawl.cateen.entity.CommentPreference();
                preference.setId(IdUtil.getSnowflakeNextIdStr());
                preference.setCommentId(commentId);
                preference.setPreferenceType(type);
                preference.setScore(score);
                commentPreferenceMapper.insert(preference);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentStatus(String id, String status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }

        String oldStatus = comment.getStatus();
        comment.setStatus(status);
        commentMapper.updateById(comment);
        
        // 如果状态从非通过变为通过，或从通过变为非通过，需要更新美食评分统计
        boolean wasApproved = CommentStatusConstants.APPROVED.equals(oldStatus);
        boolean isApproved = CommentStatusConstants.APPROVED.equals(status);
        if (wasApproved != isApproved) {
            updateFoodRatingStats(comment.getFoodId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCommentStatus(List<String> ids, String status) {
        if (CollUtil.isEmpty(ids)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评论ID列表不能为空");
        }

        // 获取所有评论的美食ID
        List<Comment> comments = commentMapper.selectBatchIds(ids);
        
        LambdaUpdateWrapper<Comment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Comment::getId, ids)
                .set(Comment::getStatus, status);
        commentMapper.update(null, wrapper);
        
        // 更新所有相关美食的评分统计
        comments.stream()
                .map(Comment::getFoodId)
                .distinct()
                .forEach(this::updateFoodRatingStats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(String id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }

        String foodId = comment.getFoodId();
        commentMapper.deleteById(id);
        
        // 更新美食评分统计
        updateFoodRatingStats(foodId);
    }

    /**
     * 更新美食的评分统计
     */
    private void updateFoodRatingStats(String foodId) {
        // 统计该美食的所有已通过评论
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Comment> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFoodId, foodId)
               .eq(Comment::getStatus, CommentStatusConstants.APPROVED);
        
        List<Comment> comments = commentMapper.selectList(wrapper);
        
        Food food = foodMapper.selectById(foodId);
        if (food != null) {
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
            log.info("更新美食评分统计: foodId={}, ratingCount={}, rating={}", 
                     foodId, food.getRatingCount(), food.getRating());
        }
    }

}

