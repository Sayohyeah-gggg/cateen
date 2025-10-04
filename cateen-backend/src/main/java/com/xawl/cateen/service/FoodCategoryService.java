package com.xawl.cateen.service;

import com.xawl.cateen.dto.CategoryDTO;
import com.xawl.cateen.entity.FoodCategory;

import java.util.List;

/**
 * 美食分类服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface FoodCategoryService {

    /**
     * 获取分类列表
     *
     * @param keyword 搜索关键词
     * @return 分类列表
     */
    List<FoodCategory> getCategoryList(String keyword);

    /**
     * 创建分类
     *
     * @param dto 分类信息
     * @return 分类
     */
    FoodCategory createCategory(CategoryDTO dto);

    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param dto 分类信息
     */
    void updateCategory(String id, CategoryDTO dto);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(String id);

}

