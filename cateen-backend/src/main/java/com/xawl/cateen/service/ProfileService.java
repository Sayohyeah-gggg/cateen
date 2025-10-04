package com.xawl.cateen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface ProfileService {

    /**
     * 分页查询用户列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @param role 角色
     * @param status 状态
     * @return 用户列表
     */
    Page<UserVO> getUserPage(Long pageNum, Long pageSize, String keyword, String role, String status);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserDetail(String id);

    /**
     * 更新用户角色
     *
     * @param id 用户ID
     * @param role 角色
     */
    void updateRole(String id, String role);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 状态
     */
    void updateStatus(String id, String status);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(String id);

}

