package com.xawl.cateen.controller;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.entity.ForumComment;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.service.ForumService;
import com.xawl.cateen.util.UserContext;
import com.xawl.cateen.vo.ForumCommentVO;
import com.xawl.cateen.vo.ForumPostVO;
import com.xawl.cateen.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 食友分享（论坛）接口
 */
@Api(tags = "11. 食友分享")
@RestController
@RequestMapping("/api/mini/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    /**
     * 获取帖子列表（分页）
     * GET /forum/posts?page=1&limit=20
     */
    @ApiOperation("获取帖子列表")
    @GetMapping("/posts")
    public Result<PageVO<ForumPostVO>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        String currentUserId = UserContext.getUserId();
        PageVO<ForumPostVO> result = forumService.getPostPage(page, limit, currentUserId);
        return Result.success(result);
    }

    /**
     * 发布帖子
     * POST /forum/posts
     * body: { content, images: [] }
     */
    @ApiOperation("发布帖子")
    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody Map<String, Object> body) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        String content = (String) body.get("content");
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) body.get("images");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        ForumPost post = forumService.createPost(userId, content.trim(), images);
        return Result.success("发布成功", post);
    }

    /**
     * 删除帖子（仅本人）
     * DELETE /forum/posts/{id}
     */
    @ApiOperation("删除帖子")
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable String id) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        forumService.deletePost(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取帖子评论列表
     * GET /forum/posts/{id}/comments?page=1&limit=20
     */
    @ApiOperation("获取帖子评论")
    @GetMapping("/posts/{id}/comments")
    public Result<PageVO<ForumCommentVO>> getComments(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        PageVO<ForumCommentVO> result = forumService.getCommentPage(id, page, limit);
        return Result.success(result);
    }

    /**
     * 发布评论
     * POST /forum/posts/{id}/comments
     * body: { content }
     */
    @ApiOperation("发布评论")
    @PostMapping("/posts/{id}/comments")
    public Result<ForumComment> createComment(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        ForumComment comment = forumService.createComment(id, userId, content.trim());
        return Result.success("评论成功", comment);
    }

    /**
     * 点赞/取消点赞
     * POST /forum/posts/{id}/like
     */
    @ApiOperation("点赞/取消点赞")
    @PostMapping("/posts/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable String id) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean liked = forumService.toggleLike(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        return Result.success(liked ? "点赞成功" : "已取消点赞", data);
    }
}
