package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小程序美食列表VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniFoodVO {
    
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
     * 美食图片
     */
    private String imageUrl;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 平均评分
     */
    private BigDecimal rating;
    
    /**
     * 评分人数
     */
    private Integer ratingCount;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 是否已收藏
     */
    private Boolean isCollected;
    
    /**
     * 最热评论（1条）
     */
    private String hotComment;
}
