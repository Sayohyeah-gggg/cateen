package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.mini.UserProfileDTO;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.CommentPreferenceMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.FoodTagRelationMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.vo.mini.MiniCommentVO;
import com.xawl.cateen.vo.mini.MiniUserProfileVO;
import com.xawl.cateen.vo.mini.MiniTasteProfileVO;
import com.xawl.cateen.vo.mini.MiniTastePreferenceVO;
import com.xawl.cateen.vo.mini.MiniTasteTagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final CommentPreferenceMapper commentPreferenceMapper;
    private final FoodMapper foodMapper;
    private final FoodTagRelationMapper foodTagRelationMapper;
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
                            .foodId(comment.getFoodId())
                            .foodName(food != null ? food.getName() : "")
                            .foodImage(food != null ? food.getImageUrl() : "")
                            .build();
                })
                .collect(Collectors.toList());
        
        Page<MiniCommentVO> result = new Page<>(page, limit, commentPage.getTotal());
        result.setRecords(commentVOs);
        
        return result;
    }

    /**
     * 获取个性化口味画像
     */
    public MiniTasteProfileVO getTasteProfile(String userId) {
        if (userId == null) {
            return MiniTasteProfileVO.builder()
                    .commentCount(0)
                    .avgRating(0.0)
                    .preferences(java.util.Collections.emptyList())
                    .tags(java.util.Collections.emptyList())
                    .build();
        }

        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId);
        List<Comment> comments = commentMapper.selectList(commentWrapper);
        int commentCount = comments.size();

        double avgRating = comments.stream()
                .map(Comment::getRating)
                .filter(r -> r != null && r > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        List<Map<String, Object>> prefStats = commentPreferenceMapper.statisticsPreferencesByUser(userId);
        List<MiniTastePreferenceVO> preferences = new ArrayList<>();
        if (prefStats != null) {
            for (Map<String, Object> row : prefStats) {
                MiniTastePreferenceVO vo = MiniTastePreferenceVO.builder()
                        .type(row.get("preference_type") != null ? row.get("preference_type").toString() : null)
                        .label(row.get("preference") != null ? row.get("preference").toString() : null)
                        .count(row.get("count") != null ? Integer.parseInt(row.get("count").toString()) : 0)
                        .avgScore(row.get("avg_score") != null ? Double.parseDouble(row.get("avg_score").toString()) : 0.0)
                        .percentage(row.get("percentage") != null ? Double.parseDouble(row.get("percentage").toString()) : 0.0)
                        .color(row.get("color") != null ? row.get("color").toString() : null)
                        .build();
                preferences.add(vo);
            }
        }

        List<Map<String, Object>> tagStats = foodTagRelationMapper.statisticsTagsByUser(userId, 8);
        List<MiniTasteTagVO> tags = new ArrayList<>();
        if (tagStats != null) {
            for (Map<String, Object> row : tagStats) {
                MiniTasteTagVO vo = MiniTasteTagVO.builder()
                        .id(row.get("tag_id") != null ? row.get("tag_id").toString() : null)
                        .name(row.get("tag_name") != null ? row.get("tag_name").toString() : null)
                        .color(row.get("tag_color") != null ? row.get("tag_color").toString() : null)
                        .count(row.get("count") != null ? Integer.parseInt(row.get("count").toString()) : 0)
                        .build();
                tags.add(vo);
            }
        }

        return MiniTasteProfileVO.builder()
                .commentCount(commentCount)
                .avgRating(Math.round(avgRating * 10.0) / 10.0)
                .preferences(preferences)
                .tags(tags)
                .build();
    }
}
