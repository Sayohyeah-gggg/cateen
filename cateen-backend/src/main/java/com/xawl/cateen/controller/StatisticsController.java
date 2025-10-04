package com.xawl.cateen.controller;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.StatisticsService;
import com.xawl.cateen.vo.DashboardStatsVO;
import com.xawl.cateen.vo.DeviceStatsVO;
import com.xawl.cateen.vo.SourceStatsVO;
import com.xawl.cateen.vo.UserActivityVO;
import com.xawl.cateen.vo.VisitStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "8. 统计数据")
@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取仪表盘统计数据
     */
    @ApiOperation(value = "获取仪表盘统计数据", notes = "获取系统总体统计数据")
    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> getDashboardStats() {
        log.info("获取仪表盘统计数据");
        DashboardStatsVO statsVO = statisticsService.getDashboardStats();
        return Result.success(statsVO);
    }

    /**
     * 获取分类统计数据
     */
    @ApiOperation(value = "获取分类统计数据", notes = "获取各分类的美食数量、评论数、平均评分等统计")
    @GetMapping("/category")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        log.info("获取分类统计数据");
        List<Map<String, Object>> stats = statisticsService.getCategoryStats();
        return Result.success(stats);
    }

    /**
     * 获取用户活动统计数据
     */
    @ApiOperation(value = "获取用户活动统计", notes = "获取指定天数内的新增用户、评论、美食统计")
    @GetMapping("/user-activity")
    public Result<List<UserActivityVO>> getUserActivityStats(
            @ApiParam(value = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("获取用户活动统计数据: days={}", days);
        List<UserActivityVO> stats = statisticsService.getUserActivityStats(days);
        return Result.success(stats);
    }

    /**
     * 获取访问统计数据
     */
    @ApiOperation(value = "获取访问统计", notes = "获取指定天数内的访问量、独立访客、页面浏览量等统计")
    @GetMapping("/visit")
    public Result<List<VisitStatsVO>> getVisitStats(
            @ApiParam(value = "统计天数", example = "30")
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("获取访问统计数据: days={}", days);
        List<VisitStatsVO> stats = statisticsService.getVisitStats(days);
        return Result.success(stats);
    }

    /**
     * 获取设备统计数据
     */
    @ApiOperation(value = "获取设备统计", notes = "获取各设备类型的访问统计")
    @GetMapping("/device")
    public Result<List<DeviceStatsVO>> getDeviceStats() {
        log.info("获取设备统计数据");
        List<DeviceStatsVO> stats = statisticsService.getDeviceStats();
        return Result.success(stats);
    }

    /**
     * 获取来源统计数据
     */
    @ApiOperation(value = "获取来源统计", notes = "获取各来源渠道的访问统计")
    @GetMapping("/source")
    public Result<List<SourceStatsVO>> getSourceStats() {
        log.info("获取来源统计数据");
        List<SourceStatsVO> stats = statisticsService.getSourceStats();
        return Result.success(stats);
    }

}

