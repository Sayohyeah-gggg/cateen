package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 小程序排行榜VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniRankingVO {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 美食ID
     */
    private String foodId;
    
    /**
     * 美食名称
     */
    private String foodName;
    
    /**
     * 美食图片
     */
    private String foodImage;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 评分人数
     */
    private Integer ratingCount;
    
    /**
     * 排名趋势（up/down/same/new）
     */
    private String trend;
}
