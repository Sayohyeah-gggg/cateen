package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美食表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@TableName("foods")
public class Food implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 美食名称
     */
    private String name;

    /**
     * 美食描述
     */
    private String description;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评价数
     */
    private Integer ratingCount;

    /**
     * 状态：active-启用，inactive-禁用
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}

