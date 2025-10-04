package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

/**
 * 评论Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询评论列表
     *
     * @param page 分页对象
     * @param keyword 搜索关键词
     * @param foodId 美食ID
     * @param userId 用户ID
     * @param status 状态
     * @param rating 评分
     * @return 评论列表
     */
    IPage<CommentVO> selectCommentPage(Page<CommentVO> page,
                                         @Param("keyword") String keyword,
                                         @Param("foodId") String foodId,
                                         @Param("userId") String userId,
                                         @Param("status") String status,
                                         @Param("rating") Integer rating);

    /**
     * 根据ID查询评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    CommentVO selectCommentDetailById(@Param("id") String id);

}

