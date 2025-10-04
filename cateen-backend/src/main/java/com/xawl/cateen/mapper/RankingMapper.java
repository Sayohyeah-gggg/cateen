package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.Ranking;
import com.xawl.cateen.vo.RankingVO;
import org.apache.ibatis.annotations.Param;

/**
 * 榜单Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface RankingMapper extends BaseMapper<Ranking> {

    /**
     * 分页查询榜单列表
     *
     * @param page 分页对象
     * @param keyword 搜索关键词
     * @param type 榜单类型
     * @param status 状态
     * @return 榜单列表
     */
    IPage<RankingVO> selectRankingPage(Page<RankingVO> page,
                                         @Param("keyword") String keyword,
                                         @Param("type") String type,
                                         @Param("status") String status);

    /**
     * 根据ID查询榜单详情
     *
     * @param id 榜单ID
     * @return 榜单详情
     */
    RankingVO selectRankingDetailById(@Param("id") String id);

}

