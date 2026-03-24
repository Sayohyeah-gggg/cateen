package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 食友分享帖子实体
 */
@Data
@TableName("forum_posts")
public class ForumPost implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String userId;

    private String content;

    /** 图片URL列表，JSON数组格式 */
    private String images;

    /** 视频URL */
    private String video;

    private Integer likeCount;

    private Integer commentCount;

    /** approved-正常，rejected-已屏蔽 */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
