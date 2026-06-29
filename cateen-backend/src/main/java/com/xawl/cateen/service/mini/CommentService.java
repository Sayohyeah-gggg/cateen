package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.mini.MiniCommentDTO;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.vo.mini.MiniCommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小程序评论服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final ProfileMapper profileMapper;
    private final CommentLikeService commentLikeService;
    private final FoodMapper foodMapper;
    
    /**
     * 获取美食评论列表
     */
    public Page<MiniCommentVO> getFoodComments(String userId, String foodId, Integer page, Integer limit) {
        Page<Comment> commentPage = new Page<>(page, limit);
        
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFoodId, foodId)
                .orderByDesc(Comment::getCreatedAt);
        
        commentMapper.selectPage(commentPage, wrapper);
        
        // 获取评论ID列表，用于批量查询点赞状态
        List<String> commentIds = commentPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        Map<String, Boolean> likedMap = commentLikeService.batchCheckLiked(userId, commentIds);
        
        // 转换为VO
        List<MiniCommentVO> commentVOs = commentPage.getRecords().stream()
                .map(comment -> {
                    // 查询用户信息
                    Profile profile = profileMapper.selectById(comment.getUserId());
                    
                    // 统计点赞数
                    Integer likeCount = commentLikeService.countCommentLikes(comment.getId());
                    
                    return MiniCommentVO.builder()
                            .id(comment.getId())
                            .userNickname(profile != null ? profile.getNickname() : "")
                            .userAvatar(profile != null ? profile.getAvatar() : "")
                            .rating(comment.getRating())
                            .content(comment.getContent())
                            .images(comment.getImages() != null ? 
                                    List.of(comment.getImages().split(",")) : List.of())
                            .likeCount(likeCount)
                            .isLiked(likedMap.getOrDefault(comment.getId(), false))
                            .createdAt(comment.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        
        Page<MiniCommentVO> result = new Page<>(page, limit, commentPage.getTotal());
        result.setRecords(commentVOs);
        
        return result;
    }
    
    /**
     * 发表评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void addComment(String userId, String foodId, MiniCommentDTO commentDTO) {
        log.info("开始发表评论，userId: {}, foodId: {}, rating: {}, content: {}",
                userId, foodId, commentDTO.getRating(), commentDTO.getContent());

        // 检查用户是否登录
        if (userId == null || userId.trim().isEmpty()) {
            log.error("发表评论失败：用户未登录，userId为空");
            throw new RuntimeException("请先登录后再发表评论");
        }

        // 检查评分是否有效
        if (commentDTO.getRating() == null || commentDTO.getRating() < 1 || commentDTO.getRating() > 5) {
            log.error("发表评论失败：评分无效，userId: {}, rating: {}", userId, commentDTO.getRating());
            throw new RuntimeException("评分必须在1-5之间");
        }

        // 检查内容是否为空
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            log.error("发表评论失败：评论内容为空，userId: {}", userId);
            throw new RuntimeException("评论内容不能为空");
        }

        try {
            // 插入评论
            Comment comment = Comment.builder()
                    .userId(userId)
                    .foodId(foodId)
                    .rating(commentDTO.getRating())
                    .content(commentDTO.getContent())
                    .images(commentDTO.getImages() != null ?
                            String.join(",", commentDTO.getImages()) : null)
                    .status("approved") // 小程序评论默认审核通过
                    .createdAt(LocalDateTime.now())
                    .build();

            int result = commentMapper.insert(comment);

            if (result <= 0) {
                log.error("发表评论失败：插入数据库失败，userId: {}, foodId: {}", userId, foodId);
                throw new RuntimeException("发表评论失败，请重试");
            }

            log.info("发表评论成功，userId: {}, foodId: {}, commentId: {}, 影响行数: {}",
                    userId, foodId, comment.getId(), result);

            // 更新美食表的评分和评论数
            updateFoodRating(foodId);

        } catch (Exception e) {
            log.error("发表评论失败：userId: {}, foodId: {}, 异常信息: {}",
                    userId, foodId, e.getMessage(), e);
            throw new RuntimeException("发表评论失败：" + e.getMessage(), e);
        }
    }

    /**
     * 更新美食表的评分和评论数
     */
    private void updateFoodRating(String foodId) {
        try {
            // 查询该美食的所有有效评论（有评分的）
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(Comment::getFoodId, foodId)
                    .isNotNull(Comment::getRating)
                    .gt(Comment::getRating, 0);

            List<Comment> comments = commentMapper.selectList(commentWrapper);

            if (comments.isEmpty()) {
                log.warn("美食没有有效评分，不更新评分，foodId: {}", foodId);
                return;
            }

            // 计算平均评分
            double totalRating = comments.stream()
                    .mapToDouble(Comment::getRating)
                    .sum();
            BigDecimal avgRating = BigDecimal.valueOf(totalRating / comments.size())
                    .setScale(1, RoundingMode.HALF_UP);

            // 更新美食表
            Food food = foodMapper.selectById(foodId);
            if (food != null) {
                food.setRating(avgRating);
                food.setRatingCount(comments.size());
                foodMapper.updateById(food);
                log.info("更新美食评分成功，foodId: {}, rating: {}, ratingCount: {}",
                        foodId, avgRating, comments.size());
            } else {
                log.warn("美食不存在，无法更新评分，foodId: {}", foodId);
            }
        } catch (Exception e) {
            log.error("更新美食评分失败，foodId: {}, 异常信息: {}", foodId, e.getMessage(), e);
            // 不抛出异常，避免影响评论插入
        }
    }
    
    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(String userId, String commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在，commentId: {}", commentId);
            return;
        }
        
        // 验证是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            log.warn("无权删除评论，userId: {}, commentId: {}", userId, commentId);
            throw new RuntimeException("无权删除该评论");
        }
        
        commentMapper.deleteById(commentId);
        log.info("删除评论成功，commentId: {}", commentId);
    }
}
