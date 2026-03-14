package com.xawl.cateen.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论返回VO
 */
@Data
public class ForumCommentVO {

    private String id;
    private String postId;
    private String userId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createdAt;

    /** 评论者昵称 */
    private String userNickname;
    /** 评论者头像 */
    private String userAvatar;
}
