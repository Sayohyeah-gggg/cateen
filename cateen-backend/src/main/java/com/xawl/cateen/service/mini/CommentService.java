package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.mini.MiniCommentDTO;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.vo.mini.MiniCommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void addComment(String userId, String foodId, MiniCommentDTO commentDTO) {
        // 检查用户是否登录
        if (userId == null) {
            throw new RuntimeException("请先登录后再发表评论");
        }

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

        commentMapper.insert(comment);
        log.info("发表评论成功，userId: {}, foodId: {}, commentId: {}", userId, foodId, comment.getId());
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
