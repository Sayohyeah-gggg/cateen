package com.xawl.cateen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.FoodDTO;
import com.xawl.cateen.vo.FoodVO;

import java.util.List;

/**
 * 美食服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface FoodService {

    /**
     * 分页查询美食列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 状态
     * @return 美食列表
     */
    Page<FoodVO> getFoodPage(Long pageNum, Long pageSize, String keyword, String categoryId, String status);

    /**
     * 获取美食详情
     *
     * @param id 美食ID
     * @return 美食详情
     */
    FoodVO getFoodDetail(String id);

    /**
     * 创建美食
     *
     * @param dto 美食信息
     * @return 美食
     */
    FoodVO createFood(FoodDTO dto);

    /**
     * 更新美食
     *
     * @param id 美食ID
     * @param dto 美食信息
     */
    void updateFood(String id, FoodDTO dto);

    /**
     * 删除美食
     *
     * @param id 美食ID
     */
    void deleteFood(String id);

    /**
     * 更新美食状态
     *
     * @param id 美食ID
     * @param status 状态
     */
    void updateFoodStatus(String id, String status);

    /**
     * 获取热门美食
     *
     * @param limit 数量限制
     * @return 热门美食列表
     */
    List<FoodVO> getPopularFoods(Integer limit);

}

