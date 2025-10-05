package com.xawl.cateen.controller.mini;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.mini.MiniCommentDTO;
import com.xawl.cateen.service.mini.CommentService;
import com.xawl.cateen.service.mini.MiniFoodService;
import com.xawl.cateen.util.SecurityUtils;
import com.xawl.cateen.vo.mini.MiniCategoryVO;
import com.xawl.cateen.vo.mini.MiniCommentVO;
import com.xawl.cateen.vo.mini.MiniFoodDetailVO;
import com.xawl.cateen.vo.mini.MiniFoodVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 小程序端 - 美食信息控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-美食信息")
@Slf4j
@RestController
@RequestMapping("/api/mini/foods")
@RequiredArgsConstructor
public class MiniFoodController {

    private final MiniFoodService miniFoodService;
    private final CommentService commentService;

    /**
     * 获取美食列表
     */
    @ApiOperation(value = "获取美食列表", notes = "获取美食列表，包含收藏状态和最热评论")
    @GetMapping
    public Result<Page<MiniFoodVO>> getFoods(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer limit,
            @ApiParam(value = "分类ID") @RequestParam(required = false) String category,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "排序方式", example = "rating") @RequestParam(defaultValue = "rating") String sortBy
    ) {
        log.info("获取美食列表，page: {}, limit: {}, category: {}, keyword: {}, sortBy: {}", 
                page, limit, category, keyword, sortBy);
        
        String userId = SecurityUtils.getCurrentUserId();
        Page<MiniFoodVO> foods = miniFoodService.getFoods(userId, page, limit, category, keyword, sortBy);
        
        return Result.success(foods);
    }

    /**
     * 获取美食详情
     */
    @ApiOperation(value = "获取美食详情", notes = "获取指定美食的详细信息")
    @GetMapping("/{id}")
    public Result<MiniFoodDetailVO> getFoodDetail(
            @ApiParam(value = "美食ID", required = true) @PathVariable String id
    ) {
        log.info("获取美食详情，id: {}", id);
        
        String userId = SecurityUtils.getCurrentUserId();
        MiniFoodDetailVO detail = miniFoodService.getFoodDetail(userId, id);
        
        if (detail == null) {
            return Result.error("美食不存在");
        }
        
        return Result.success(detail);
    }

    /**
     * 获取美食分类
     */
    @ApiOperation(value = "获取美食分类", notes = "获取所有美食分类，包含美食数量")
    @GetMapping("/categories")
    public Result<List<MiniCategoryVO>> getCategories() {
        log.info("获取美食分类");
        
        List<MiniCategoryVO> categories = miniFoodService.getCategories();
        return Result.success(categories);
    }

    /**
     * 获取热门搜索词
     */
    @ApiOperation(value = "获取热门搜索词", notes = "获取热门搜索关键词")
    @GetMapping("/hot-keywords")
    public Result<List<String>> getHotKeywords() {
        log.info("获取热门搜索词");
        
        List<String> keywords = miniFoodService.getHotKeywords();
        return Result.success(keywords);
    }

    /**
     * 获取用户搜索历史
     */
    @ApiOperation(value = "获取用户搜索历史", notes = "获取当前用户的搜索历史")
    @GetMapping("/search-history")
    public Result<List<String>> getUserSearchHistory() {
        log.info("获取用户搜索历史");
        
        String userId = SecurityUtils.getCurrentUserId();
        List<String> history = miniFoodService.getUserSearchHistory(userId);
        return Result.success(history);
    }

    /**
     * 清除用户搜索历史
     */
    @ApiOperation(value = "清除用户搜索历史", notes = "清除当前用户的搜索历史")
    @DeleteMapping("/search-history")
    public Result<?> clearUserSearchHistory() {
        log.info("清除用户搜索历史");
        
        String userId = SecurityUtils.getCurrentUserId();
        miniFoodService.clearUserSearchHistory(userId);
        return Result.success("清除成功");
    }

    /**
     * 获取美食评论
     */
    @ApiOperation(value = "获取美食评论", notes = "获取指定美食的评论列表")
    @GetMapping("/{id}/comments")
    public Result<Page<MiniCommentVO>> getComments(
            @ApiParam(value = "美食ID", required = true) @PathVariable String id,
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer limit
    ) {
        log.info("获取美食评论，foodId: {}, page: {}, limit: {}", id, page, limit);
        
        String userId = SecurityUtils.getCurrentUserId();
        Page<MiniCommentVO> comments = commentService.getFoodComments(userId, id, page, limit);
        
        return Result.success(comments);
    }

    /**
     * 发表评论
     */
    @ApiOperation(value = "发表评论", notes = "为指定美食发表评论")
    @PostMapping("/{id}/comments")
    public Result<?> addComment(
            @ApiParam(value = "美食ID", required = true) @PathVariable String id,
            @Valid @RequestBody MiniCommentDTO commentDTO
    ) {
        log.info("发表评论，foodId: {}, rating: {}", id, commentDTO.getRating());
        
        String userId = SecurityUtils.getCurrentUserId();
        commentService.addComment(userId, id, commentDTO);
        
        return Result.success("评论成功");
    }
}