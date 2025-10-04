package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论关注点实体
 *
 * @author xawl
 * @date 2025-10-04
 */
@Data
@TableName("comment_preferences")
public class CommentPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 评论ID
     */
    private String commentId;

    /**
     * 关注点类型
     * taste: 口味, price: 价格, environment: 环境, service: 服务, other: 其他
     */
    private String preferenceType;

    /**
     * 该关注点的评分(1-5)
     */
    private Integer score;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

