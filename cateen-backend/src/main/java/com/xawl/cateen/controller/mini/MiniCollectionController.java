package com.xawl.cateen.controller.mini;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.mini.CollectionDTO;
import com.xawl.cateen.service.mini.CollectionService;
import com.xawl.cateen.util.SecurityUtils;
import com.xawl.cateen.vo.mini.MiniFoodVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 小程序端 - 收藏管理控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-收藏管理")
@Slf4j
@RestController
@RequestMapping("/api/mini/user/collections")
@RequiredArgsConstructor
public class MiniCollectionController {

    private final CollectionService collectionService;

    /**
     * 获取收藏列表
     */
    @ApiOperation(value = "获取收藏列表", notes = "获取用户收藏的美食列表")
    @GetMapping
    public Result<Page<MiniFoodVO>> getCollections(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer limit,
            @ApiParam(value = "分类ID") @RequestParam(required = false) String category
    ) {
        log.info("获取收藏列表，page: {}, limit: {}, category: {}", page, limit, category);
        
        String userId = SecurityUtils.getCurrentUserId();
        Page<MiniFoodVO> collections = collectionService.getCollections(userId, page, limit, category);
        
        return Result.success(collections);
    }

    /**
     * 添加收藏
     */
    @ApiOperation(value = "添加收藏", notes = "收藏指定美食")
    @PostMapping
    public Result<?> addCollection(@Valid @RequestBody CollectionDTO collectionDTO) {
        log.info("添加收藏，foodId: {}", collectionDTO.getFoodId());
        
        String userId = SecurityUtils.getCurrentUserId();
        collectionService.addCollection(userId, collectionDTO.getFoodId());
        
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @ApiOperation(value = "取消收藏", notes = "取消收藏指定美食")
    @DeleteMapping("/{foodId}")
    public Result<?> removeCollection(
            @ApiParam(value = "美食ID", required = true) @PathVariable String foodId
    ) {
        log.info("取消收藏，foodId: {}", foodId);
        
        String userId = SecurityUtils.getCurrentUserId();
        collectionService.removeCollection(userId, foodId);
        
        return Result.success("已取消收藏");
    }
}