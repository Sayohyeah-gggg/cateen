package com.xawl.cateen.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 统计Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 统计美食总数
     *
     * @return 美食总数
     */
    Long countTotalFoods();

    /**
     * 统计用户总数
     *
     * @return 用户总数
     */
    Long countTotalUsers();

    /**
     * 统计评论总数
     *
     * @return 评论总数
     */
    Long countTotalComments();

    /**
     * 统计待审核评论数
     *
     * @return 待审核评论数
     */
    Long countPendingComments();

    /**
     * 统计榜单总数
     *
     * @return 榜单总数
     */
    Long countTotalRankings();

    /**
     * 计算平均评分
     *
     * @return 平均评分
     */
    BigDecimal calculateAvgRating();

    /**
     * 统计分类数据
     *
     * @return 分类统计列表
     */
    java.util.List<Map<String, Object>> statisticsCategoryData();

    /**
     * 统计用户活动数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户活动统计列表
     */
    java.util.List<Map<String, Object>> statisticsUserActivity(String startDate, String endDate);

    /**
     * 统计访问数据（暂时返回模拟数据结构，实际应接入访问日志表）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 访问统计列表
     */
    java.util.List<Map<String, Object>> statisticsVisitData(String startDate, String endDate);

}

