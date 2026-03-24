package com.xawl.cateen.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子点赞返回VO
 */
@Data
public class ForumLikeVO {

    private String id;
    private String postId;
    private String userId;
    private LocalDateTime createdAt;

    /** 点赞者昵称 */
    private String userNickname;
    /** 点赞者头像 */
    private String userAvatar;
}
