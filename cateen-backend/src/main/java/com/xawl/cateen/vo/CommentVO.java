package com.xawl.cateen.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private String id;

    /**
     * 美食ID
     */
    private String foodId;

    /**
     * 美食名称
     */
    private String foodName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}

