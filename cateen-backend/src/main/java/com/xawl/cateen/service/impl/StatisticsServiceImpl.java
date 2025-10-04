package com.xawl.cateen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.entity.DeviceStat;
import com.xawl.cateen.entity.SourceStat;
import com.xawl.cateen.entity.VisitStat;
import com.xawl.cateen.mapper.DeviceStatMapper;
import com.xawl.cateen.mapper.SourceStatMapper;
import com.xawl.cateen.mapper.StatisticsMapper;
import com.xawl.cateen.mapper.VisitStatMapper;
import com.xawl.cateen.service.StatisticsService;
import com.xawl.cateen.vo.DashboardStatsVO;
import com.xawl.cateen.vo.DeviceStatsVO;
import com.xawl.cateen.vo.SourceStatsVO;
import com.xawl.cateen.vo.UserActivityVO;
import com.xawl.cateen.vo.VisitStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final VisitStatMapper visitStatMapper;
    private final DeviceStatMapper deviceStatMapper;
    private final SourceStatMapper sourceStatMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardStatsVO getDashboardStats() {
        Long totalFoods = statisticsMapper.countTotalFoods();
        Long totalUsers = statisticsMapper.countTotalUsers();
        Long totalComments = statisticsMapper.countTotalComments();
        Long pendingComments = statisticsMapper.countPendingComments();
        Long totalRankings = statisticsMapper.countTotalRankings();
        BigDecimal avgRating = statisticsMapper.calculateAvgRating();

        return DashboardStatsVO.builder()
                .totalFoods(totalFoods)
                .totalUsers(totalUsers)
                .totalComments(totalComments)
                .pendingComments(pendingComments)
                .totalRankings(totalRankings)
                .avgRating(avgRating != null ? avgRating : BigDecimal.ZERO)
                .build();
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return statisticsMapper.statisticsCategoryData();
    }

    @Override
    public List<UserActivityVO> getUserActivityStats(Integer days) {
        // 计算日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        String startDateStr = startDate.format(DATE_FORMATTER);
        String endDateStr = endDate.format(DATE_FORMATTER);

        // 查询数据
        List<Map<String, Object>> dataList = statisticsMapper.statisticsUserActivity(startDateStr, endDateStr);

        // 转换为VO
        return dataList.stream().map(data -> UserActivityVO.builder()
                .date(data.get("date").toString())
                .newUsers(((Number) data.get("newUsers")).intValue())
                .newComments(((Number) data.get("newComments")).intValue())
                .newFoods(((Number) data.get("newFoods")).intValue())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<VisitStatsVO> getVisitStats(Integer days) {
        // 计算日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 从数据库查询真实的访问统计数据
        LambdaQueryWrapper<VisitStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(VisitStat::getStatDate, startDate, endDate)
               .orderByAsc(VisitStat::getStatDate);
        
        List<VisitStat> visitStats = visitStatMapper.selectList(wrapper);

        // 转换为VO
        return visitStats.stream().map(stat -> VisitStatsVO.builder()
                .date(stat.getStatDate().format(DATE_FORMATTER))
                .visits(stat.getVisits())
                .uniqueVisitors(stat.getUniqueVisitors())
                .pageViews(stat.getPageViews())
                .bounceRate(stat.getBounceRate())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<DeviceStatsVO> getDeviceStats() {
        // 查询最新一天的设备统计数据
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DeviceStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceStat::getStatDate, today);
        
        List<DeviceStat> deviceStats = deviceStatMapper.selectList(wrapper);
        
        // 如果没有今天的数据，返回空列表
        if (deviceStats.isEmpty()) {
            return List.of();
        }
        
        // 计算总访问量
        int totalVisits = deviceStats.stream()
                .mapToInt(DeviceStat::getVisitCount)
                .sum();
        
        // 设备类型的中文名称和颜色映射
        Map<String, String> deviceNames = new HashMap<>();
        deviceNames.put("desktop", "桌面端");
        deviceNames.put("mobile", "移动端");
        deviceNames.put("tablet", "平板端");
        
        Map<String, String> deviceColors = new HashMap<>();
        deviceColors.put("desktop", "#3b82f6");
        deviceColors.put("mobile", "#10b981");
        deviceColors.put("tablet", "#f59e0b");
        
        // 转换为VO
        return deviceStats.stream().map(stat -> {
            double percentage = totalVisits > 0 ? 
                (stat.getVisitCount() * 100.0 / totalVisits) : 0.0;
            
            return DeviceStatsVO.builder()
                    .name(deviceNames.getOrDefault(stat.getDeviceType(), stat.getDeviceType()))
                    .value(stat.getVisitCount())
                    .percentage(BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP).doubleValue())
                    .color(deviceColors.getOrDefault(stat.getDeviceType(), "#6b7280"))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<SourceStatsVO> getSourceStats() {
        // 查询最新一天的来源统计数据
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<SourceStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SourceStat::getStatDate, today);
        
        List<SourceStat> sourceStats = sourceStatMapper.selectList(wrapper);
        
        // 如果没有今天的数据，返回空列表
        if (sourceStats.isEmpty()) {
            return List.of();
        }
        
        // 计算总访问量
        int totalVisits = sourceStats.stream()
                .mapToInt(SourceStat::getVisitCount)
                .sum();
        
        // 来源类型的中文名称映射
        Map<String, String> sourceNames = new HashMap<>();
        sourceNames.put("direct", "直接访问");
        sourceNames.put("search", "搜索引擎");
        sourceNames.put("social", "社交媒体");
        sourceNames.put("referral", "推荐链接");
        sourceNames.put("other", "其他");
        
        // 转换为VO
        return sourceStats.stream().map(stat -> {
            double percentage = totalVisits > 0 ? 
                (stat.getVisitCount() * 100.0 / totalVisits) : 0.0;
            
            return SourceStatsVO.builder()
                    .name(sourceNames.getOrDefault(stat.getSourceType(), stat.getSourceType()))
                    .visits(stat.getVisitCount())
                    .percentage(BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP).doubleValue())
                    .build();
        }).collect(Collectors.toList());
    }

}

