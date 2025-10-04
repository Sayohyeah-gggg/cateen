package com.xawl.cateen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.constant.StatusConstants;
import com.xawl.cateen.dto.FoodDTO;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.FoodTagRelation;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.FoodTagRelationMapper;
import com.xawl.cateen.service.FoodService;
import com.xawl.cateen.util.UserContext;
import com.xawl.cateen.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 美食服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodMapper foodMapper;
    private final FoodTagRelationMapper tagRelationMapper;

    @Override
    public Page<FoodVO> getFoodPage(Long pageNum, Long pageSize, String keyword, String categoryId, String status) {
        Page<FoodVO> page = new Page<>(pageNum, pageSize);
        foodMapper.selectFoodPage(page, keyword, categoryId, status);
        return page;
    }

    @Override
    public FoodVO getFoodDetail(String id) {
        FoodVO foodVO = foodMapper.selectFoodDetailById(id);
        if (foodVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "美食不存在");
        }
        return foodVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodVO createFood(FoodDTO dto) {
        // 创建美食
        Food food = new Food();
        food.setId(IdUtil.getSnowflakeNextIdStr());
        food.setName(dto.getName());
        food.setDescription(dto.getDescription());
        food.setCategoryId(dto.getCategoryId());
        food.setImageUrl(dto.getImageUrl());
        food.setPrice(dto.getPrice());
        food.setRating(BigDecimal.ZERO);
        food.setRatingCount(0);
        food.setStatus(StatusConstants.ACTIVE);
        food.setCreatedBy(UserContext.getUserId());

        foodMapper.insert(food);

        // 创建标签关联
        if (CollUtil.isNotEmpty(dto.getTagIds())) {
            List<FoodTagRelation> relations = dto.getTagIds().stream()
                    .map(tagId -> {
                        FoodTagRelation relation = new FoodTagRelation();
                        relation.setId(IdUtil.getSnowflakeNextIdStr());
                        relation.setFoodId(food.getId());
                        relation.setTagId(tagId);
                        relation.setCreatedAt(LocalDateTime.now());
                        return relation;
                    })
                    .collect(Collectors.toList());
            tagRelationMapper.batchInsert(relations);
        }

        return getFoodDetail(food.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFood(String id, FoodDTO dto) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "美食不存在");
        }

        // 更新美食信息
        food.setName(dto.getName());
        food.setDescription(dto.getDescription());
        food.setCategoryId(dto.getCategoryId());
        food.setImageUrl(dto.getImageUrl());
        food.setPrice(dto.getPrice());

        foodMapper.updateById(food);

        // 更新标签关联
        if (dto.getTagIds() != null) {
            // 删除旧的标签关联
            tagRelationMapper.deleteByFoodId(id);

            // 创建新的标签关联
            if (CollUtil.isNotEmpty(dto.getTagIds())) {
                List<FoodTagRelation> relations = dto.getTagIds().stream()
                        .map(tagId -> {
                            FoodTagRelation relation = new FoodTagRelation();
                            relation.setId(IdUtil.getSnowflakeNextIdStr());
                            relation.setFoodId(id);
                            relation.setTagId(tagId);
                            relation.setCreatedAt(LocalDateTime.now());
                            return relation;
                        })
                        .collect(Collectors.toList());
                tagRelationMapper.batchInsert(relations);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFood(String id) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "美食不存在");
        }

        foodMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFoodStatus(String id, String status) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "美食不存在");
        }

        food.setStatus(status);
        foodMapper.updateById(food);
    }

    @Override
    public List<FoodVO> getPopularFoods(Integer limit) {
        return foodMapper.selectPopularFoods(limit != null ? limit : 10);
    }

}

