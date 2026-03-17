package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.ForumService;
import com.xawl.cateen.vo.ForumCommentVO;
import com.xawl.cateen.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端 - 帖子评论管理
 */
@Api(tags = "管理端-帖子评论管理")
@Slf4j
@RestController
@RequestMapping("/api/admin/forum/comments")
@RequiredArgsConstructor
public class AdminForumCommentController {

    private final ForumService forumService;

    /**
     * 分页查询帖子评论
     */
    @ApiOperation("分页查询帖子评论")
    @GetMapping
    public Result<PageVO<ForumCommentVO>> getComments(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam("关键字") @RequestParam(required = false) String keyword,
            @ApiParam("帖子ID") @RequestParam(required = false) String postId,
            @ApiParam("用户ID") @RequestParam(required = false) String userId,
            @ApiParam("状态 pending/approved/rejected") @RequestParam(required = false) String status) {
        log.info("管理端查询帖子评论: page={}, keyword={}, postId={}, userId={}, status={}", page, keyword, postId, userId, status);
        PageVO<ForumCommentVO> result = forumService.adminGetCommentPage(page, pageSize, keyword, postId, userId, status);
        return Result.success(result);
    }

    /**
     * 更新帖子评论状态
     */
    @ApiOperation("更新帖子评论状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateCommentStatus(
            @ApiParam("评论ID") @PathVariable String id,
            @Valid @RequestBody StatusDTO dto) {
        log.info("更新帖子评论状态: id={}, status={}", id, dto.getStatus());
        forumService.adminUpdateCommentStatus(id, dto.getStatus());
        return Result.success("操作成功", null);
    }

    /**
     * 删除帖子评论
     */
    @ApiOperation("删除帖子评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@ApiParam("评论ID") @PathVariable String id) {
        log.info("删除帖子评论: id={}", id);
        forumService.adminDeleteComment(id);
        return Result.success("删除成功", null);
    }
}
