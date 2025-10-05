package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.LoginDTO;
import com.xawl.cateen.dto.PasswordDTO;
import com.xawl.cateen.dto.RegisterDTO;
import com.xawl.cateen.service.AuthService;
import com.xawl.cateen.vo.LoginVO;
import com.xawl.cateen.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端 - 认证控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "管理端-认证模块")
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @ApiOperation(value = "用户登录", notes = "使用用户名和密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        log.info("用户登录: {}", dto.getUsername());
        LoginVO loginVO = authService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册", notes = "创建新用户账户")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        log.info("用户注册: {}", dto.getUsername());
        UserVO userVO = authService.register(dto);
        return Result.success("注册成功", userVO);
    }

    /**
     * 获取当前用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "根据Token获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = authService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 修改密码
     */
    @ApiOperation(value = "修改密码", notes = "修改当前登录用户的密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        log.info("修改密码");
        authService.changePassword(dto);
        return Result.success("密码修改成功", null);
    }

    /**
     * 退出登录
     */
    @ApiOperation(value = "退出登录", notes = "用户退出登录（前端清除Token即可）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("退出登录");
        return Result.success("退出成功", null);
    }

}
