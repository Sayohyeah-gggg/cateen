package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小程序-个性化口味画像
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniTasteProfileVO {

    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 平均评分
     */
    private Double avgRating;

    /**
     * 偏好维度统计
     */
    private List<MiniTastePreferenceVO> preferences;

    /**
     * 偏好标签统计
     */
    private List<MiniTasteTagVO> tags;
}

