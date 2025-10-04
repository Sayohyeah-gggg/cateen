package com.xawl.cateen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.CommentDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.CommentService;
import com.xawl.cateen.vo.CommentVO;
import com.xawl.cateen.vo.PageVO;
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
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询评论列表
     */
    @GetMapping
    public Result<PageVO<CommentVO>> getCommentPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String foodId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating) {
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
    @GetMapping("/{id}")
    public Result<CommentVO> getCommentDetail(@PathVariable String id) {
        log.info("获取评论详情: id={}", id);
        CommentVO commentVO = commentService.getCommentDetail(id);
        return Result.success(commentVO);
    }

    /**
     * 创建评论
     */
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CommentDTO dto) {
        log.info("创建评论: foodId={}", dto.getFoodId());
        CommentVO commentVO = commentService.createComment(dto);
        return Result.success("评论发表成功，等待审核", commentVO);
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateCommentStatus(@PathVariable String id, @Valid @RequestBody StatusDTO dto) {
        log.info("更新评论状态: id={}, status={}", id, dto.getStatus());
        commentService.updateCommentStatus(id, dto.getStatus());
        return Result.success("评论审核成功", null);
    }

    /**
     * 批量更新评论状态
     */
    @PutMapping("/batch-status")
    public Result<Void> batchUpdateCommentStatus(@RequestBody Map<String, Object> params) {
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
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable String id) {
        log.info("删除评论: id={}", id);
        commentService.deleteComment(id);
        return Result.success("评论删除成功", null);
    }

}

