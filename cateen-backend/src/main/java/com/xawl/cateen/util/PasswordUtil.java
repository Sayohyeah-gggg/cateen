package com.xawl.cateen.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 *
 * @author xawl
 * @date 2025-10-03
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 密码加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 密码验证
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

}

