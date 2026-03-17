package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 口味偏好标签统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniTasteTagVO {
    private String id;
    private String name;
    private String color;
    private Integer count;
}

