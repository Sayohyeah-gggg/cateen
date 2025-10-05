package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xawl.cateen.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏Mapper
 *
 * @author xawl
 * @date 2025-10-05
 */
@Mapper
public interface CollectionMapper extends BaseMapper<UserCollection> {
    
    /**
     * 统计用户收藏数
     */
    Integer countByUserId(@Param("userId") String userId);
    
    /**
     * 统计美食收藏数
     */
    Integer countByFoodId(@Param("foodId") String foodId);
    
    /**
     * 检查是否已收藏
     */
    Integer existsByUserIdAndFoodId(@Param("userId") String userId, @Param("foodId") String foodId);
}
