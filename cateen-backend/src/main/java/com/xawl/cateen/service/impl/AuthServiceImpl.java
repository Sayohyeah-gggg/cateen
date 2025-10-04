package com.xawl.cateen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.constant.RoleConstants;
import com.xawl.cateen.constant.StatusConstants;
import com.xawl.cateen.dto.LoginDTO;
import com.xawl.cateen.dto.PasswordDTO;
import com.xawl.cateen.dto.RegisterDTO;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.service.AuthService;
import com.xawl.cateen.util.JwtUtil;
import com.xawl.cateen.util.PasswordUtil;
import com.xawl.cateen.util.UserContext;
import com.xawl.cateen.vo.LoginVO;
import com.xawl.cateen.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ProfileMapper profileMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 根据用户名查询用户
        Profile profile = profileMapper.selectByUsername(dto.getUsername());
        if (profile == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 检查账户状态
        if (StatusConstants.INACTIVE.equals(profile.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账户已被禁用");
        }

        // 验证密码
        if (StrUtil.isBlank(profile.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未设置密码");
        }

        if (!PasswordUtil.matches(dto.getPassword(), profile.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(profile.getId(), profile.getUsername(), profile.getRole());

        // 构建返回结果
        UserVO userVO = BeanUtil.copyProperties(profile, UserVO.class);
        return LoginVO.builder()
                .token(token)
                .user(userVO)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        // 验证两次密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
        }

        // 检查用户名是否已存在
        Profile existProfile = profileMapper.selectByUsername(dto.getUsername());
        if (existProfile != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        // 创建用户
        Profile profile = new Profile();
        profile.setId(IdUtil.getSnowflakeNextIdStr());
        profile.setUserId("user_" + IdUtil.fastSimpleUUID());
        profile.setUsername(dto.getUsername());
        profile.setPasswordHash(PasswordUtil.encode(dto.getPassword()));
        profile.setPhone(dto.getPhone());
        profile.setRole(RoleConstants.USER);
        profile.setStatus(StatusConstants.ACTIVE);

        profileMapper.insert(profile);

        return BeanUtil.copyProperties(profile, UserVO.class);
    }

    @Override
    public UserVO getCurrentUser() {
        String userId = UserContext.getUserId();
        if (StrUtil.isBlank(userId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }

        Profile profile = profileMapper.selectById(userId);
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        return BeanUtil.copyProperties(profile, UserVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(PasswordDTO dto) {
        // 验证两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码输入不一致");
        }

        // 获取当前用户
        String userId = UserContext.getUserId();
        Profile profile = profileMapper.selectById(userId);
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtil.matches(dto.getOldPassword(), profile.getPasswordHash())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "旧密码错误");
        }

        // 更新密码
        profile.setPasswordHash(PasswordUtil.encode(dto.getNewPassword()));
        profileMapper.updateById(profile);
    }

}

