package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.mini.WxLoginDTO;
import com.xawl.cateen.service.mini.WechatAuthService;
import com.xawl.cateen.util.SecurityUtils;
import com.xawl.cateen.vo.mini.WxLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 小程序端 - 微信登录认证控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-认证模块")
@Slf4j
@RestController
@RequestMapping("/api/mini/auth")
@RequiredArgsConstructor
public class MiniAuthController {

    private final WechatAuthService wechatAuthService;

    /**
     * 微信小程序登录
     * 
     * 流程：
     * 1. 小程序调用 wx.login() 获取 code
     * 2. 将 code 发送到后端
     * 3. 后端调用微信接口 code2Session 获取 openid 和 session_key
     * 4. 根据 openid 查询或创建用户
     * 5. 生成 JWT token 返回给小程序
     * 
     * @param loginDTO 登录信息（包含code和用户信息）
     * @return token和用户信息
     */
    @ApiOperation(value = "微信登录", notes = "使用微信授权登录，返回JWT Token")
    @PostMapping("/login")
    public Result<WxLoginVO> wxLogin(@Valid @RequestBody WxLoginDTO loginDTO) {
        log.info("微信小程序登录，code: {}", loginDTO.getCode());
        
        WxLoginVO loginVO = wechatAuthService.wxLogin(loginDTO);
        
        return Result.success("登录成功", loginVO);
    }

    /**
     * 刷新Token
     * 
     * @return 新的token
     */
    @ApiOperation(value = "刷新Token", notes = "刷新访问令牌")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh() {
        log.info("刷新Token");
        
        // 从上下文获取当前用户ID
        String userId = SecurityUtils.getCurrentUserId();
        
        // 生成新token
        String newToken = wechatAuthService.refreshToken(userId);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        
        return Result.success("Token刷新成功", result);
    }
}
