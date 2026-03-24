package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.vo.ForumPostVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 分页查询帖子列表（带用户信息）
     */
    Page<ForumPostVO> selectPostPage(Page<ForumPostVO> page, @Param("currentUserId") String currentUserId);

    /**
     * 管理端分页查询（支持关键词、状态筛选）
     */
    Page<ForumPostVO> adminSelectPostPage(Page<ForumPostVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("status") String status);

    /**
     * 统计帖子评论总数（基于帖子comment_count求和）
     */
    Long sumCommentCount();

    /**
     * 根据ID查询帖子（绕过逻辑删除）
     */
    @Select("SELECT * FROM forum_posts WHERE id = #{id}")
    ForumPost selectByIdIgnoreLogicDelete(@Param("id") String id);

    /**
     * 物理删除帖子（绕过逻辑删除）
     */
    @Delete("DELETE FROM forum_posts WHERE id = #{id}")
    int deletePhysical(@Param("id") String id);
}
