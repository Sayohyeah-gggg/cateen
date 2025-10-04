package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xawl.cateen.entity.FoodTagRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 美食标签关联Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface FoodTagRelationMapper extends BaseMapper<FoodTagRelation> {

    /**
     * 批量插入标签关联
     *
     * @param relations 标签关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("relations") List<FoodTagRelation> relations);

    /**
     * 删除美食的所有标签关联
     *
     * @param foodId 美食ID
     * @return 删除数量
     */
    int deleteByFoodId(@Param("foodId") String foodId);

}

