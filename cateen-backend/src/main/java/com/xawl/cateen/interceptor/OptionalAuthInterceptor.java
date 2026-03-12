package com.xawl.cateen.interceptor;

import cn.hutool.core.util.StrUtil;
import com.xawl.cateen.util.JwtUtil;
import com.xawl.cateen.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 可选认证拦截器
 * 如果有Token则解析并设置用户信息，没有Token也允许通过
 *
 * @author xawl
 * @date 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionalAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.info("[可选认证拦截器] 处理请求: {}", requestURI);

        // 从请求头中获取Token
        String authHeader = request.getHeader(header);
        log.info("[可选认证拦截器] Authorization header: {}", authHeader != null ? "存在" : "不存在");

        // 如果没有Token或Token格式不正确，允许通过（作为未登录用户）
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(tokenPrefix)) {
            log.warn("[可选认证拦截器] 请求未携带Token，作为未登录用户处理");
            return true;
        }

        try {
            // 提取Token
            String token = authHeader.substring(tokenPrefix.length()).trim();

            // 验证Token（忽略过期，如果过期也作为未登录处理）
            if (jwtUtil.isTokenExpired(token)) {
                log.debug("Token已过期，作为未登录用户处理");
                return true;
            }

            // 从Token中获取用户信息并设置到上下文
            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            if (StrUtil.isNotBlank(userId) && StrUtil.isNotBlank(username)) {
                UserContext.setUserId(userId);
                UserContext.setUsername(username);
                UserContext.setRole(role);
                log.info("[可选认证拦截器] Token解析成功 - userId: {}, username: {}, role: {}", userId, username, role);
            } else {
                log.warn("[可选认证拦截器] Token解析失败 - userId或username为空");
            }
        } catch (Exception e) {
            // Token解析失败，作为未登录用户处理
            log.warn("Token解析失败，作为未登录用户处理: {}", e.getMessage());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文
        UserContext.clear();
    }
}

