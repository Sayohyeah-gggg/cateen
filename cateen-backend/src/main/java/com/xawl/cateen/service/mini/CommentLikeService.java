package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.entity.CommentLike;
import com.xawl.cateen.mapper.CommentLikeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论点赞服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeService {
    
    private final CommentLikeMapper commentLikeMapper;
    
    /**
     * 切换点赞状态
     * @return true-已点赞，false-已取消点赞
     */
    @Transactional
    public boolean toggleLike(String userId, String commentId) {
        // 检查是否已点赞
        Integer exists = commentLikeMapper.existsByUserIdAndCommentId(userId, commentId);
        
        if (exists > 0) {
            // 已点赞，取消点赞
            LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommentLike::getUserId, userId)
                    .eq(CommentLike::getCommentId, commentId);
            
            commentLikeMapper.delete(wrapper);
            log.info("取消点赞，userId: {}, commentId: {}", userId, commentId);
            return false;
        } else {
            // 未点赞，添加点赞
            CommentLike commentLike = CommentLike.builder()
                    .userId(userId)
                    .commentId(commentId)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            commentLikeMapper.insert(commentLike);
            log.info("点赞成功，userId: {}, commentId: {}", userId, commentId);
            return true;
        }
    }
    
    /**
     * 检查是否已点赞
     */
    public boolean isLiked(String userId, String commentId) {
        Integer exists = commentLikeMapper.existsByUserIdAndCommentId(userId, commentId);
        return exists > 0;
    }
    
    /**
     * 批量检查是否已点赞
     */
    public Map<String, Boolean> batchCheckLiked(String userId, List<String> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashMap<>();
        }
        
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getUserId, userId)
                .in(CommentLike::getCommentId, commentIds);
        
        List<CommentLike> likes = commentLikeMapper.selectList(wrapper);
        Set<String> likedCommentIds = likes.stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toSet());
        
        return commentIds.stream()
                .collect(Collectors.toMap(
                        commentId -> commentId,
                        likedCommentIds::contains
                ));
    }
    
    /**
     * 统计评论点赞数
     */
    public Integer countCommentLikes(String commentId) {
        return commentLikeMapper.countByCommentId(commentId);
    }
    
    /**
     * 统计用户获得的点赞数
     */
    public Integer countUserLikes(String userId) {
        return commentLikeMapper.countByUserId(userId);
    }
}
