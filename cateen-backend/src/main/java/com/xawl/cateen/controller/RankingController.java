package com.xawl.cateen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.RankingDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.RankingService;
import com.xawl.cateen.vo.PageVO;
import com.xawl.cateen.vo.RankingVO;
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
@Slf4j
@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 分页查询榜单列表
     */
    @GetMapping
    public Result<PageVO<RankingVO>> getRankingPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
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
    @GetMapping("/{id}")
    public Result<RankingVO> getRankingDetail(@PathVariable String id) {
        log.info("获取榜单详情: id={}", id);
        RankingVO rankingVO = rankingService.getRankingDetail(id);
        return Result.success(rankingVO);
    }

    /**
     * 创建榜单
     */
    @PostMapping
    public Result<RankingVO> createRanking(@Valid @RequestBody RankingDTO dto) {
        log.info("创建榜单: title={}", dto.getTitle());
        RankingVO rankingVO = rankingService.createRanking(dto);
        return Result.success("榜单创建成功", rankingVO);
    }

    /**
     * 更新榜单
     */
    @PutMapping("/{id}")
    public Result<Void> updateRanking(@PathVariable String id, @Valid @RequestBody RankingDTO dto) {
        log.info("更新榜单: id={}, title={}", id, dto.getTitle());
        rankingService.updateRanking(id, dto);
        return Result.success("榜单更新成功", null);
    }

    /**
     * 删除榜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRanking(@PathVariable String id) {
        log.info("删除榜单: id={}", id);
        rankingService.deleteRanking(id);
        return Result.success("榜单删除成功", null);
    }

    /**
     * 更新榜单状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateRankingStatus(@PathVariable String id, @Valid @RequestBody StatusDTO dto) {
        log.info("更新榜单状态: id={}, status={}", id, dto.getStatus());
        rankingService.updateRankingStatus(id, dto.getStatus());
        return Result.success("状态更新成功", null);
    }

}

