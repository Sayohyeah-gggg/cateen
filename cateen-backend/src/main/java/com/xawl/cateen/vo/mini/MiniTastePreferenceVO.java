package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 口味偏好维度统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniTastePreferenceVO {
    private String type;
    private String label;
    private Integer count;
    private Double avgScore;
    private Double percentage;
    private String color;
}

