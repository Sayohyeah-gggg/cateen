package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.mini.UserProfileDTO;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.vo.mini.MiniCommentVO;
import com.xawl.cateen.vo.mini.MiniUserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小程序用户服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniUserService {
    
    private final ProfileMapper profileMapper;
    private final CommentMapper commentMapper;
    private final FoodMapper foodMapper;
    private final CollectionService collectionService;
    private final CommentLikeService commentLikeService;
    
    /**
     * 获取用户资料
     */
    public MiniUserProfileVO getProfile(String userId) {
        Profile profile = profileMapper.selectById(userId);
        if (profile == null) {
            return null;
        }
        
        // 统计收藏数
        Integer collectionCount = collectionService.countUserCollections(userId);
        
        // 统计评论数
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId);
        Long commentCount = commentMapper.selectCount(commentWrapper);
        
        // 统计获得的点赞数
        Integer likeCount = commentLikeService.countUserLikes(userId);
        
        return MiniUserProfileVO.builder()
                .id(profile.getId())
                .nickname(profile.getNickname())
                .avatar(profile.getAvatar())
                .collectionCount(collectionCount)
                .commentCount(commentCount.intValue())
                .likeCount(likeCount)
                .build();
    }
    
    /**
     * 更新用户资料
     */
    @Transactional
    public void updateProfile(String userId, UserProfileDTO profileDTO) {
        Profile profile = profileMapper.selectById(userId);
        if (profile == null) {
            log.warn("用户不存在，userId: {}", userId);
            return;
        }
        
        // 更新昵称
        if (profileDTO.getNickName() != null) {
            profile.setNickname(profileDTO.getNickName());
        }
        
        // 更新头像
        if (profileDTO.getAvatar() != null) {
            profile.setAvatar(profileDTO.getAvatar());
        }
        
        profileMapper.updateById(profile);
        log.info("更新用户资料成功，userId: {}", userId);
    }
    
    /**
     * 获取用户评论历史
     */
    public Page<MiniCommentVO> getComments(String userId, Integer page, Integer limit) {
        Page<Comment> commentPage = new Page<>(page, limit);
        
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
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
                    // 查询美食信息
                    Food food = foodMapper.selectById(comment.getFoodId());
                    
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
                            .foodName(food != null ? food.getName() : "")
                            .foodImage(food != null ? food.getImageUrl() : "")
                            .build();
                })
                .collect(Collectors.toList());
        
        Page<MiniCommentVO> result = new Page<>(page, limit, commentPage.getTotal());
        result.setRecords(commentVOs);
        
        return result;
    }
}
