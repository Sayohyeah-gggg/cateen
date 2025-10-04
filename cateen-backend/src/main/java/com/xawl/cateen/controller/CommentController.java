package com.xawl.cateen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.CommentDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.CommentService;
import com.xawl.cateen.vo.CommentVO;
import com.xawl.cateen.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 评论管理控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "4. 评论管理")
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询评论列表
     */
    @ApiOperation(value = "分页查询评论列表", notes = "支持关键词搜索、美食筛选、用户筛选、状态筛选、评分筛选")
    @GetMapping
    public Result<PageVO<CommentVO>> getCommentPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long pageNum,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Long pageSize,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "美食ID") @RequestParam(required = false) String foodId,
            @ApiParam(value = "用户ID") @RequestParam(required = false) String userId,
            @ApiParam(value = "状态(pending/approved/rejected)") @RequestParam(required = false) String status,
            @ApiParam(value = "评分(1-5)") @RequestParam(required = false) Integer rating) {
        log.info("分页查询评论列表: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<CommentVO> page = commentService.getCommentPage(pageNum, pageSize, keyword, foodId, userId, status, rating);
        
        PageVO<CommentVO> pageVO = PageVO.<CommentVO>builder()
                .list(page.getRecords())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .build();
        
        return Result.success(pageVO);
    }

    /**
     * 获取评论详情
     */
    @ApiOperation(value = "获取评论详情", notes = "根据评论ID获取评论的详细信息")
    @GetMapping("/{id}")
    public Result<CommentVO> getCommentDetail(@ApiParam(value = "评论ID", required = true) @PathVariable String id) {
        log.info("获取评论详情: id={}", id);
        CommentVO commentVO = commentService.getCommentDetail(id);
        return Result.success(commentVO);
    }

    /**
     * 创建评论
     */
    @ApiOperation(value = "创建评论", notes = "用户对美食发表评论，支持多维度评分")
    @PostMapping
    public Result<CommentVO> createComment(@ApiParam(value = "评论信息", required = true) @Valid @RequestBody CommentDTO dto) {
        log.info("创建评论: foodId={}", dto.getFoodId());
        CommentVO commentVO = commentService.createComment(dto);
        return Result.success("评论发表成功，等待审核", commentVO);
    }

    /**
     * 更新评论状态
     */
    @ApiOperation(value = "更新评论状态", notes = "管理员审核评论，设置为通过、拒绝等状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateCommentStatus(
            @ApiParam(value = "评论ID", required = true) @PathVariable String id, 
            @ApiParam(value = "状态信息", required = true) @Valid @RequestBody StatusDTO dto) {
        log.info("更新评论状态: id={}, status={}", id, dto.getStatus());
        commentService.updateCommentStatus(id, dto.getStatus());
        return Result.success("评论审核成功", null);
    }

    /**
     * 批量更新评论状态
     */
    @ApiOperation(value = "批量更新评论状态", notes = "批量审核多条评论")
    @PutMapping("/batch-status")
    public Result<Void> batchUpdateCommentStatus(@ApiParam(value = "批量操作参数(ids:评论ID列表, status:状态)", required = true) @RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) params.get("ids");
        String status = (String) params.get("status");
        
        log.info("批量更新评论状态: ids={}, status={}", ids, status);
        commentService.batchUpdateCommentStatus(ids, status);
        return Result.success("批量审核成功", null);
    }

    /**
     * 删除评论
     */
    @ApiOperation(value = "删除评论", notes = "删除指定的评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@ApiParam(value = "评论ID", required = true) @PathVariable String id) {
        log.info("删除评论: id={}", id);
        commentService.deleteComment(id);
        return Result.success("评论删除成功", null);
    }

}

