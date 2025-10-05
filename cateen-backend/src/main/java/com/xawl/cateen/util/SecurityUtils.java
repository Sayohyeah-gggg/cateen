package com.xawl.cateen.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 * 用于获取当前登录用户信息
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     * 从请求头中解析JWT Token获取用户ID
     */
    public static String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("无法获取请求上下文");
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj != null) {
                return userIdObj.toString();
            }
            
            // 如果没有设置，尝试从Token中解析
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
                return jwtUtil.getUserIdFromToken(token);
            }
            
            log.warn("无法获取当前用户ID");
            return null;
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取当前用户的角色
     */
    public static String getCurrentUserRole() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            Object roleObj = request.getAttribute("userRole");
            return roleObj != null ? roleObj.toString() : null;
        } catch (Exception e) {
            log.error("获取当前用户角色失败", e);
            return null;
        }
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "admin".equals(role);
    }
}
