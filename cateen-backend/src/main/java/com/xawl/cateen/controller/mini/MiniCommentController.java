package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.mini.CommentLikeService;
import com.xawl.cateen.service.mini.CommentService;
import com.xawl.cateen.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序端 - 评论交互控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-评论交互")
@Slf4j
@RestController
@RequestMapping("/api/mini/comments")
@RequiredArgsConstructor
public class MiniCommentController {

    private final CommentLikeService commentLikeService;
    private final CommentService commentService;

    /**
     * 点赞/取消点赞
     */
    @ApiOperation(value = "点赞/取消点赞", notes = "切换评论的点赞状态")
    @PostMapping("/{commentId}/like")
    public Result<Map<String, Object>> toggleLike(
            @ApiParam(value = "评论ID", required = true) @PathVariable String commentId
    ) {
        log.info("切换评论点赞状态，commentId: {}", commentId);
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isLiked = commentLikeService.toggleLike(userId, commentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("message", isLiked ? "点赞成功" : "已取消点赞");
        
        return Result.success(result);
    }

    /**
     * 删除评论
     */
    @ApiOperation(value = "删除评论", notes = "删除指定评论（仅作者可删除）")
    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(
            @ApiParam(value = "评论ID", required = true) @PathVariable String commentId
    ) {
        log.info("删除评论，commentId: {}", commentId);
        
        String userId = SecurityUtils.getCurrentUserId();
        commentService.deleteComment(userId, commentId);
        
        return Result.success("删除成功");
    }
}