package com.xawl.cateen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 来源统计VO
 *
 * @author xawl
 * @date 2025-10-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 来源名称
     */
    private String name;

    /**
     * 访问数量
     */
    private Integer visits;

    /**
     * 百分比
     */
    private Double percentage;
}

