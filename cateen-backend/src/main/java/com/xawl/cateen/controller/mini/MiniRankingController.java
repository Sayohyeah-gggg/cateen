package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.RankingService;
import com.xawl.cateen.vo.mini.MiniRankingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端 - 排行榜控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-排行榜")
@Slf4j
@RestController
@RequestMapping("/api/mini/ranking")
@RequiredArgsConstructor
public class MiniRankingController {

    private final RankingService rankingService;

    /**
     * 获取排行榜
     */
    @ApiOperation(value = "获取排行榜", notes = "获取美食排行榜")
    @GetMapping
    public Result<List<MiniRankingVO>> getRanking(
            @ApiParam(value = "排行榜类型", example = "rating") 
            @RequestParam(defaultValue = "rating") String type,
            @ApiParam(value = "分类ID") 
            @RequestParam(required = false) String category,
            @ApiParam(value = "时间范围", example = "week") 
            @RequestParam(defaultValue = "week") String timeRange,
            @ApiParam(value = "返回数量", example = "10") 
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("获取排行榜，type: {}, category: {}, timeRange: {}, limit: {}", 
                type, category, timeRange, limit);
        
        // 使用现有的排行榜服务
        // 这里简化实现，返回固定排行榜
        // TODO: 根据type、category、timeRange参数动态查询
        List<MiniRankingVO> rankings = rankingService.getMiniRanking(type, category, timeRange, limit);
        
        return Result.success(rankings);
    }
}