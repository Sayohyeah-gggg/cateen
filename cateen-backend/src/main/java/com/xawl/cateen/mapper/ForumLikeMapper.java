package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.ForumLike;
import com.xawl.cateen.vo.ForumLikeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumLikeMapper extends BaseMapper<ForumLike> {

    Page<ForumLikeVO> selectLikePage(Page<ForumLikeVO> page, @Param("postId") String postId);
}
