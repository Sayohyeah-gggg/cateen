package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 美食ID
     */
    private String foodId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评论图片（逗号分隔）
     */
    private String images;

    /**
     * 审核状态：pending-待审核，approved-已通过，rejected-已拒绝
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}

