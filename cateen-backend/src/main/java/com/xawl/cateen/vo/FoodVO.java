package com.xawl.cateen.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.entity.FoodTag;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 美食VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class FoodVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 美食ID
     */
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
     * 分类信息
     */
    private FoodCategory category;

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
     * 状态
     */
    private String status;

    /**
     * 标签列表
     */
    private List<FoodTag> tags;

    /**
     * 评论数
     */
    private Integer commentsCount;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}

