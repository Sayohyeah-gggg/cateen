package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.ForumService;
import com.xawl.cateen.vo.ForumPostVO;
import com.xawl.cateen.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 - 食友分享管理
 */
@Api(tags = "管理端-食友分享管理")
@Slf4j
@RestController
@RequestMapping("/api/admin/forum")
@RequiredArgsConstructor
public class AdminForumController {

    private final ForumService forumService;

    /**
     * 分页查询帖子列表
     */
    @ApiOperation("分页查询帖子列表")
    @GetMapping("/posts")
    public Result<PageVO<ForumPostVO>> getPosts(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("状态(approved/rejected)") @RequestParam(required = false) String status) {
        log.info("管理端查询帖子列表: page={}, keyword={}, status={}", page, keyword, status);
        PageVO<ForumPostVO> result = forumService.adminGetPostPage(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 更新帖子状态（屏蔽/恢复）
     */
    @ApiOperation("更新帖子状态")
    @PutMapping("/posts/{id}/status")
    public Result<Void> updatePostStatus(
            @ApiParam("帖子ID") @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        log.info("更新帖子状态: id={}, status={}", id, newStatus);
        forumService.adminUpdatePostStatus(id, newStatus);
        return Result.success("操作成功", null);
    }

    /**
     * 删除帖子（管理员强制删除）
     */
    @ApiOperation("删除帖子")
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@ApiParam("帖子ID") @PathVariable String id) {
        log.info("管理员删除帖子: id={}", id);
        forumService.adminDeletePost(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取帖子评论列表
     */
    @ApiOperation("获取帖子评论列表")
    @GetMapping("/posts/{postId}/comments")
    public Result<PageVO<com.xawl.cateen.vo.ForumCommentVO>> getPostComments(
            @ApiParam("帖子ID") @PathVariable String postId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "100") int pageSize) {
        log.info("获取帖子评论列表: postId={}, page={}", postId, page);
        com.xawl.cateen.vo.PageVO<com.xawl.cateen.vo.ForumCommentVO> result = forumService.getCommentPage(postId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取帖子点赞列表
     */
    @ApiOperation("获取帖子点赞列表")
    @GetMapping("/posts/{postId}/likes")
    public Result<com.xawl.cateen.vo.PageVO<com.xawl.cateen.vo.ForumLikeVO>> getPostLikes(
            @ApiParam("帖子ID") @PathVariable String postId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "100") int pageSize) {
        log.info("获取帖子点赞列表: postId={}, page={}", postId, page);
        com.xawl.cateen.vo.PageVO<com.xawl.cateen.vo.ForumLikeVO> result = forumService.getLikePage(postId, page, pageSize);
        return Result.success(result);
    }
}
