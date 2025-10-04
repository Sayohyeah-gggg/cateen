package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xawl.cateen.entity.RankingFood;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 榜单美食关联Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface RankingFoodMapper extends BaseMapper<RankingFood> {

    /**
     * 批量插入榜单美食关联
     *
     * @param relations 榜单美食关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("relations") List<RankingFood> relations);

    /**
     * 删除榜单的所有美食关联
     *
     * @param rankingId 榜单ID
     * @return 删除数量
     */
    int deleteByRankingId(@Param("rankingId") String rankingId);

}

