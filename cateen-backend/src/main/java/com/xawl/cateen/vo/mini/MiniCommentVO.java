package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小程序评论VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniCommentVO {
    
    /**
     * 评论ID
     */
    private String id;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 评分
     */
    private Integer rating;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论图片
     */
    private List<String> images;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 评论时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 美食名称（用于用户评论历史）
     */
    private String foodName;
    
    /**
     * 美食图片（用于用户评论历史）
     */
    private String foodImage;
}
