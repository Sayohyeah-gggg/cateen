package com.xawl.cateen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.constant.RoleConstants;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.service.ProfileService;
import com.xawl.cateen.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;

    @Override
    public Page<UserVO> getUserPage(Long pageNum, Long pageSize, String keyword, String role, String status) {
        Page<UserVO> page = new Page<>(pageNum, pageSize);
        profileMapper.selectUserPage(page, keyword, role, status);
        return page;
    }

    @Override
    public UserVO getUserDetail(String id) {
        Profile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return BeanUtil.copyProperties(profile, UserVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(String id, String role) {
        // 验证角色值
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.USER.equals(role)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色值不合法");
        }

        Profile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        profile.setRole(role);
        profileMapper.updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, String status) {
        Profile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        profile.setStatus(status);
        profileMapper.updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        Profile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        profileMapper.deleteById(id);
    }

}

