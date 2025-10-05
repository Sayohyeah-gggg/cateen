package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.CategoryDTO;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.service.FoodCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端 - 美食分类控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "管理端-分类管理")
@Slf4j
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final FoodCategoryService categoryService;

    /**
     * 获取分类列表
     */
    @ApiOperation(value = "获取分类列表", notes = "获取所有美食分类，支持关键词搜索")
    @GetMapping
    public Result<List<FoodCategory>> getCategoryList(@ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword) {
        log.info("获取分类列表: keyword={}", keyword);
        List<FoodCategory> list = categoryService.getCategoryList(keyword);
        return Result.success(list);
    }

    /**
     * 创建分类
     */
    @ApiOperation(value = "创建分类", notes = "创建新的美食分类")
    @PostMapping
    public Result<FoodCategory> createCategory(@ApiParam(value = "分类信息", required = true) @Valid @RequestBody CategoryDTO dto) {
        log.info("创建分类: name={}", dto.getName());
        FoodCategory category = categoryService.createCategory(dto);
        return Result.success("分类创建成功", category);
    }

    /**
     * 更新分类
     */
    @ApiOperation(value = "更新分类", notes = "更新指定分类的信息")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable String id, 
            @ApiParam(value = "分类信息", required = true) @Valid @RequestBody CategoryDTO dto) {
        log.info("更新分类: id={}, name={}", id, dto.getName());
        categoryService.updateCategory(id, dto);
        return Result.success("分类更新成功", null);
    }

    /**
     * 删除分类
     */
    @ApiOperation(value = "删除分类", notes = "删除指定的美食分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@ApiParam(value = "分类ID", required = true) @PathVariable String id) {
        log.info("删除分类: id={}", id);
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功", null);
    }

}
