package com.xawl.cateen.controller;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.CategoryDTO;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.service.FoodCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 美食分类控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class FoodCategoryController {

    private final FoodCategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping
    public Result<List<FoodCategory>> getCategoryList(@RequestParam(required = false) String keyword) {
        log.info("获取分类列表: keyword={}", keyword);
        List<FoodCategory> list = categoryService.getCategoryList(keyword);
        return Result.success(list);
    }

    /**
     * 创建分类
     */
    @PostMapping
    public Result<FoodCategory> createCategory(@Valid @RequestBody CategoryDTO dto) {
        log.info("创建分类: name={}", dto.getName());
        FoodCategory category = categoryService.createCategory(dto);
        return Result.success("分类创建成功", category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryDTO dto) {
        log.info("更新分类: id={}, name={}", id, dto.getName());
        categoryService.updateCategory(id, dto);
        return Result.success("分类更新成功", null);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable String id) {
        log.info("删除分类: id={}", id);
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功", null);
    }

}

