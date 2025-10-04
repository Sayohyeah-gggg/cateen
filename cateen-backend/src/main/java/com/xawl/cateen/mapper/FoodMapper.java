package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.vo.FoodVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 美食Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface FoodMapper extends BaseMapper<Food> {

    /**
     * 分页查询美食列表（带分类和标签信息）
     *
     * @param page 分页对象
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 状态
     * @return 美食列表
     */
    IPage<FoodVO> selectFoodPage(Page<FoodVO> page,
                                   @Param("keyword") String keyword,
                                   @Param("categoryId") String categoryId,
                                   @Param("status") String status);

    /**
     * 根据ID查询美食详情（带分类和标签信息）
     *
     * @param id 美食ID
     * @return 美食详情
     */
    FoodVO selectFoodDetailById(@Param("id") String id);

    /**
     * 查询热门美食
     *
     * @param limit 数量限制
     * @return 热门美食列表
     */
    List<FoodVO> selectPopularFoods(@Param("limit") Integer limit);

}

