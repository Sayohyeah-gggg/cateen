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
    public Result<?> addCollection(@RequestBody(required = false) CollectionDTO collectionDTO,
                                   @RequestParam(required = false) String foodId) {
        log.info("添加收藏请求，接收到的DTO: {}", collectionDTO);
        log.info("添加收藏请求，接收到的foodId参数: {}", foodId);
        
        // 优先使用DTO中的foodId，如果为空则使用参数中的foodId
        String finalFoodId = null;
        if (collectionDTO != null && collectionDTO.getFoodId() != null) {
            finalFoodId = collectionDTO.getFoodId();
        } else if (foodId != null) {
            finalFoodId = foodId;
        }
        
        log.info("最终使用的foodId: {}", finalFoodId);
        
        if (finalFoodId == null || finalFoodId.trim().isEmpty()) {
            log.error("foodId为空或null，collectionDTO: {}, foodId参数: {}", collectionDTO, foodId);
            return Result.error("美食ID不能为空");
        }
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("当前用户ID: {}", userId);
        
        collectionService.addCollection(userId, finalFoodId);
        
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