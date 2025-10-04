package com.xawl.cateen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.RankingDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.RankingService;
import com.xawl.cateen.vo.PageVO;
import com.xawl.cateen.vo.RankingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 榜单管理控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "7. 榜单管理")
@Slf4j
@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 分页查询榜单列表
     */
    @ApiOperation(value = "分页查询榜单列表", notes = "支持关键词搜索、类型筛选、状态筛选")
    @GetMapping
    public Result<PageVO<RankingVO>> getRankingPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long pageNum,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Long pageSize,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "榜单类型") @RequestParam(required = false) String type,
            @ApiParam(value = "状态(active/inactive)") @RequestParam(required = false) String status) {
        log.info("分页查询榜单列表: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<RankingVO> page = rankingService.getRankingPage(pageNum, pageSize, keyword, type, status);
        
        PageVO<RankingVO> pageVO = PageVO.<RankingVO>builder()
                .list(page.getRecords())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .build();
        
        return Result.success(pageVO);
    }

    /**
     * 获取榜单详情
     */
    @ApiOperation(value = "获取榜单详情", notes = "根据榜单ID获取榜单的详细信息")
    @GetMapping("/{id}")
    public Result<RankingVO> getRankingDetail(@ApiParam(value = "榜单ID", required = true) @PathVariable String id) {
        log.info("获取榜单详情: id={}", id);
        RankingVO rankingVO = rankingService.getRankingDetail(id);
        return Result.success(rankingVO);
    }

    /**
     * 创建榜单
     */
    @ApiOperation(value = "创建榜单", notes = "创建新的美食榜单")
    @PostMapping
    public Result<RankingVO> createRanking(@ApiParam(value = "榜单信息", required = true) @Valid @RequestBody RankingDTO dto) {
        log.info("创建榜单: title={}", dto.getTitle());
        RankingVO rankingVO = rankingService.createRanking(dto);
        return Result.success("榜单创建成功", rankingVO);
    }

    /**
     * 更新榜单
     */
    @ApiOperation(value = "更新榜单", notes = "更新指定榜单的信息")
    @PutMapping("/{id}")
    public Result<Void> updateRanking(
            @ApiParam(value = "榜单ID", required = true) @PathVariable String id, 
            @ApiParam(value = "榜单信息", required = true) @Valid @RequestBody RankingDTO dto) {
        log.info("更新榜单: id={}, title={}", id, dto.getTitle());
        rankingService.updateRanking(id, dto);
        return Result.success("榜单更新成功", null);
    }

    /**
     * 删除榜单
     */
    @ApiOperation(value = "删除榜单", notes = "删除指定的榜单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRanking(@ApiParam(value = "榜单ID", required = true) @PathVariable String id) {
        log.info("删除榜单: id={}", id);
        rankingService.deleteRanking(id);
        return Result.success("榜单删除成功", null);
    }

    /**
     * 更新榜单状态
     */
    @ApiOperation(value = "更新榜单状态", notes = "启用或禁用榜单")
    @PutMapping("/{id}/status")
    public Result<Void> updateRankingStatus(
            @ApiParam(value = "榜单ID", required = true) @PathVariable String id, 
            @ApiParam(value = "状态信息", required = true) @Valid @RequestBody StatusDTO dto) {
        log.info("更新榜单状态: id={}, status={}", id, dto.getStatus());
        rankingService.updateRankingStatus(id, dto.getStatus());
        return Result.success("状态更新成功", null);
    }

}

