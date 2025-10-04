package com.xawl.cateen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.RankingDTO;
import com.xawl.cateen.vo.RankingVO;

/**
 * 榜单服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface RankingService {

    /**
     * 分页查询榜单列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @param type 榜单类型
     * @param status 状态
     * @return 榜单列表
     */
    Page<RankingVO> getRankingPage(Long pageNum, Long pageSize, String keyword, String type, String status);

    /**
     * 获取榜单详情
     *
     * @param id 榜单ID
     * @return 榜单详情
     */
    RankingVO getRankingDetail(String id);

    /**
     * 创建榜单
     *
     * @param dto 榜单信息
     * @return 榜单
     */
    RankingVO createRanking(RankingDTO dto);

    /**
     * 更新榜单
     *
     * @param id 榜单ID
     * @param dto 榜单信息
     */
    void updateRanking(String id, RankingDTO dto);

    /**
     * 删除榜单
     *
     * @param id 榜单ID
     */
    void deleteRanking(String id);

    /**
     * 更新榜单状态
     *
     * @param id 榜单ID
     * @param status 状态
     */
    void updateRankingStatus(String id, String status);

}

