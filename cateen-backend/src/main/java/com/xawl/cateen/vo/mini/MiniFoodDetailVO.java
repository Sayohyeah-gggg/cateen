package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小程序美食详情VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniFoodDetailVO {
    
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
     * 美食图片列表
     */
    private List<String> images;
    
    /**
     * 分类ID
     */
    private String categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 平均评分
     */
    private BigDecimal rating;
    
    /**
     * 评分人数
     */
    private Integer ratingCount;
    
    /**
     * 口味评分
     */
    private BigDecimal tasteRating;
    
    /**
     * 环境评分
     */
    private BigDecimal environmentRating;
    
    /**
     * 服务评分
     */
    private BigDecimal serviceRating;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 是否已收藏
     */
    private Boolean isCollected;
    
    /**
     * 收藏数量
     */
    private Integer collectionCount;
    
    /**
     * 评论数量
     */
    private Integer commentCount;
}
