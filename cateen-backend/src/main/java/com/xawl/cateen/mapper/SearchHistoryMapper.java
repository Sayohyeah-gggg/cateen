package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xawl.cateen.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索历史Mapper
 *
 * @author xawl
 * @date 2025-10-05
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    /**
     * 获取热门搜索关键词
     *
     * @param limit 返回数量
     * @return 热门关键词列表
     */
    @Select("SELECT keyword FROM search_history " +
            "WHERE keyword IS NOT NULL AND keyword != '' " +
            "GROUP BY keyword " +
            "ORDER BY SUM(search_count) DESC, MAX(last_search_time) DESC " +
            "LIMIT #{limit}")
    List<String> getHotKeywords(@Param("limit") Integer limit);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 用户搜索历史
     */
    @Select("SELECT keyword FROM search_history " +
            "WHERE user_id = #{userId} " +
            "ORDER BY last_search_time DESC " +
            "LIMIT #{limit}")
    List<String> getUserSearchHistory(@Param("userId") String userId, @Param("limit") Integer limit);

    /**
     * 更新或插入搜索记录
     *
     * @param userId 用户ID
     * @param keyword 关键词
     */
    @Select("INSERT INTO search_history (id, user_id, keyword, search_count, last_search_time, created_at, updated_at) " +
            "VALUES (#{id}, #{userId}, #{keyword}, 1, NOW(), NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "search_count = search_count + 1, " +
            "last_search_time = NOW(), " +
            "updated_at = NOW()")
    void upsertSearchRecord(@Param("id") String id, @Param("userId") String userId, @Param("keyword") String keyword);
}
