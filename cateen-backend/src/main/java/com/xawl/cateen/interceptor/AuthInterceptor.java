package com.xawl.cateen.interceptor;

import cn.hutool.core.util.StrUtil;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.util.JwtUtil;
import com.xawl.cateen.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取Token
        String authHeader = request.getHeader(header);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(tokenPrefix)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或Token已过期");
        }

        // 提取Token
        String token = authHeader.substring(tokenPrefix.length()).trim();

        // 验证Token
        if (jwtUtil.isTokenExpired(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token已过期");
        }

        // 从Token中获取用户信息并设置到上下文
        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        if (StrUtil.isBlank(userId) || StrUtil.isBlank(username)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token无效");
        }

        UserContext.setUserId(userId);
        UserContext.setUsername(username);
        UserContext.setRole(role);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文
        UserContext.clear();
    }

}

