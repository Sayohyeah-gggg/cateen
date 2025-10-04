package com.xawl.cateen.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.dto.CategoryDTO;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.mapper.FoodCategoryMapper;
import com.xawl.cateen.service.FoodCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 美食分类服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodCategoryServiceImpl implements FoodCategoryService {

    private final FoodCategoryMapper categoryMapper;

    @Override
    public List<FoodCategory> getCategoryList(String keyword) {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(FoodCategory::getName, keyword);
        }
        wrapper.orderByAsc(FoodCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodCategory createCategory(CategoryDTO dto) {
        FoodCategory category = new FoodCategory();
        category.setId(IdUtil.getSnowflakeNextIdStr());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(String id, CategoryDTO dto) {
        FoodCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }

        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(String id) {
        FoodCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }

        categoryMapper.deleteById(id);
    }

}

