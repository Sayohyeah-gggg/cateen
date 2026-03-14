package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.ForumComment;
import com.xawl.cateen.vo.ForumCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {

    Page<ForumCommentVO> selectCommentPage(Page<ForumCommentVO> page, @Param("postId") String postId);
}
