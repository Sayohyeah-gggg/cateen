package com.xawl.cateen.service;

import com.xawl.cateen.vo.DashboardStatsVO;
import com.xawl.cateen.vo.DeviceStatsVO;
import com.xawl.cateen.vo.SourceStatsVO;
import com.xawl.cateen.vo.UserActivityVO;
import com.xawl.cateen.vo.VisitStatsVO;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface StatisticsService {

    /**
     * 获取仪表盘统计数据
     *
     * @return 统计数据
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 获取分类统计数据
     *
     * @return 分类统计列表
     */
    List<Map<String, Object>> getCategoryStats();

    /**
     * 获取用户活动统计数据
     *
     * @param days 天数
     * @return 用户活动统计列表
     */
    List<UserActivityVO> getUserActivityStats(Integer days);

    /**
     * 获取访问统计数据
     *
     * @param days 天数
     * @return 访问统计列表
     */
    List<VisitStatsVO> getVisitStats(Integer days);

    /**
     * 获取设备统计数据
     *
     * @return 设备统计列表
     */
    List<DeviceStatsVO> getDeviceStats();

    /**
     * 获取来源统计数据
     *
     * @return 来源统计列表
     */
    List<SourceStatsVO> getSourceStats();

    /**
     * 获取评分趋势数据
     *
     * @param days 天数
     * @return 评分趋势统计列表
     */
    List<Map<String, Object>> getRatingTrend(Integer days);

    /**
     * 获取评分分布数据
     *
     * @return 评分分布统计列表
     */
    List<Map<String, Object>> getRatingDistribution();

    /**
     * 获取评分关注点分析数据
     *
     * @return 评分关注点统计列表
     */
    List<Map<String, Object>> getRatingPreferences();

}

