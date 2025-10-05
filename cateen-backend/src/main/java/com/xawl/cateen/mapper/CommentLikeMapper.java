package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xawl.cateen.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper
 *
 * @author xawl
 * @date 2025-10-05
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    
    /**
     * 统计评论点赞数
     */
    Integer countByCommentId(@Param("commentId") String commentId);
    
    /**
     * 统计用户获得的点赞数
     */
    Integer countByUserId(@Param("userId") String userId);
    
    /**
     * 检查是否已点赞
     */
    Integer existsByUserIdAndCommentId(@Param("userId") String userId, @Param("commentId") String commentId);
}
