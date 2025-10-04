package com.xawl.cateen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 访问统计VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String date;

    /**
     * 访问次数
     */
    private Integer visits;

    /**
     * 独立访客数
     */
    private Integer uniqueVisitors;

    /**
     * 页面浏览量
     */
    private Integer pageViews;

    /**
     * 跳出率（百分比）
     */
    private Integer bounceRate;

}

