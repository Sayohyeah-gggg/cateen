package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xawl.cateen.entity.CommentPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 评论关注点Mapper
 *
 * @author xawl
 * @date 2025-10-04
 */
@Mapper
public interface CommentPreferenceMapper extends BaseMapper<CommentPreference> {

    /**
     * 统计各关注点的平均评分和占比
     *
     * @return 关注点统计列表
     */
    List<Map<String, Object>> statisticsPreferences();

    /**
     * 根据食物ID统计关注点
     *
     * @param foodId 食物ID
     * @return 关注点统计列表
     */
    List<Map<String, Object>> statisticsPreferencesByFood(@Param("foodId") String foodId);

    /**
     * 根据分类ID统计关注点
     *
     * @param categoryId 分类ID
     * @return 关注点统计列表
     */
    List<Map<String, Object>> statisticsPreferencesByCategory(@Param("categoryId") String categoryId);

    /**
     * 根据评论ID批量删除关注点
     *
     * @param commentId 评论ID
     * @return 删除数量
     */
    int deleteByCommentId(@Param("commentId") String commentId);

}

