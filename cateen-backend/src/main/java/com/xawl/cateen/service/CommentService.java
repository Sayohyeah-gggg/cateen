package com.xawl.cateen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.dto.CommentDTO;
import com.xawl.cateen.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface CommentService {

    /**
     * 分页查询评论列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @param foodId 美食ID
     * @param userId 用户ID
     * @param status 状态
     * @param rating 评分
     * @return 评论列表
     */
    Page<CommentVO> getCommentPage(Long pageNum, Long pageSize, String keyword, 
                                     String foodId, String userId, String status, Integer rating);

    /**
     * 获取评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    CommentVO getCommentDetail(String id);

    /**
     * 创建评论
     *
     * @param dto 评论信息
     * @return 评论
     */
    CommentVO createComment(CommentDTO dto);

    /**
     * 更新评论状态
     *
     * @param id 评论ID
     * @param status 状态
     */
    void updateCommentStatus(String id, String status);

    /**
     * 批量更新评论状态
     *
     * @param ids 评论ID列表
     * @param status 状态
     */
    void batchUpdateCommentStatus(List<String> ids, String status);

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    void deleteComment(String id);

}

