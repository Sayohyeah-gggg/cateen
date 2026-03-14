package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.vo.ForumPostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 分页查询帖子列表（带用户信息）
     */
    Page<ForumPostVO> selectPostPage(Page<ForumPostVO> page, @Param("currentUserId") String currentUserId);
}
