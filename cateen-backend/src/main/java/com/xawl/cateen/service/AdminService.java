package com.xawl.cateen.service;

/**
 * 管理服务接口
 *
 * @author xawl
 * @date 2025-10-04
 */
public interface AdminService {

    /**
     * 同步美食评分统计
     * 
     * @return 更新的美食数量
     */
    int syncFoodRatings();
}

