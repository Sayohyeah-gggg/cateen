package com.xawl.cateen.service;

import com.xawl.cateen.dto.LoginDTO;
import com.xawl.cateen.dto.PasswordDTO;
import com.xawl.cateen.dto.RegisterDTO;
import com.xawl.cateen.vo.LoginVO;
import com.xawl.cateen.vo.UserVO;

/**
 * 认证服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 登录结果（包含Token和用户信息）
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 用户信息
     */
    UserVO register(RegisterDTO dto);

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 修改密码
     *
     * @param dto 密码信息
     */
    void changePassword(PasswordDTO dto);

}

