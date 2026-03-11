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
    private String food_id;
    
    /**
     * 美食名称
     */
    private String food_name;
    
    /**
     * 美食图片
     */
    private String food_image;
    
    /**
     * 分类名称
     */
    private String category_name;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 评分人数
     */
    private Integer rating_count;
    
    /**
     * 排名趋势（up/down/same/new）
     */
    private String trend;
}
