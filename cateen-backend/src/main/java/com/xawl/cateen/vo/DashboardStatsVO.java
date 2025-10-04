package com.xawl.cateen.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 仪表盘统计VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@Builder
public class DashboardStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 美食总数
     */
    private Long totalFoods;

    /**
     * 用户总数
     */
    private Long totalUsers;

    /**
     * 评论总数
     */
    private Long totalComments;

    /**
     * 待审核评论数
     */
    private Long pendingComments;

    /**
     * 榜单总数
     */
    private Long totalRankings;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;

}

