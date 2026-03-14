package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 食友分享点赞实体
 */
@Data
@TableName("forum_likes")
public class ForumLike implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String postId;

    private String userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
