package com.xawl.cateen.service;

import java.util.List;

/**
 * 搜索历史服务接口
 *
 * @author xawl
 * @date 2025-10-05
 */
public interface SearchHistoryService {

    /**
     * 记录搜索历史
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     */
    void recordSearch(String userId, String keyword);

    /**
     * 获取热门搜索关键词
     *
     * @param limit 返回数量
     * @return 热门关键词列表
     */
    List<String> getHotKeywords(Integer limit);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 用户搜索历史
     */
    List<String> getUserSearchHistory(String userId, Integer limit);

    /**
     * 清除用户搜索历史
     *
     * @param userId 用户ID
     */
    void clearUserSearchHistory(String userId);
}
