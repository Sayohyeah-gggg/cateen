package com.xawl.cateen.util;

/**
 * 用户上下文工具类
 *
 * @author xawl
 * @date 2025-10-03
 */
public class UserContext {

    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static String getUserId() {
        return userIdHolder.get();
    }

    /**
     * 设置当前用户名
     */
    public static void setUsername(String username) {
        usernameHolder.set(username);
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return usernameHolder.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setRole(String role) {
        roleHolder.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getRole() {
        return roleHolder.get();
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        roleHolder.remove();
    }

}

