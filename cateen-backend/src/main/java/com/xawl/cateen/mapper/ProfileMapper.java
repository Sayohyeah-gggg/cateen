package com.xawl.cateen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.vo.UserVO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface ProfileMapper extends BaseMapper<Profile> {

    /**
     * 分页查询用户列表
     *
     * @param page 分页对象
     * @param keyword 搜索关键词
     * @param role 角色
     * @param status 状态
     * @return 用户列表
     */
    IPage<UserVO> selectUserPage(Page<UserVO> page,
                                   @Param("keyword") String keyword,
                                   @Param("role") String role,
                                   @Param("status") String status);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Profile selectByUsername(@Param("username") String username);

}

