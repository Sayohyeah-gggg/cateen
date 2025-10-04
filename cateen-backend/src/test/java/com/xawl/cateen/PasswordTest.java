package com.xawl.cateen;

import com.xawl.cateen.util.PasswordUtil;
import org.junit.jupiter.api.Test;

/**
 * 密码测试类
 */
public class PasswordTest {

    @Test
    public void testPasswordMatch() {
        // 测试 admin123 密码
        String rawPassword = "admin123";
        String encodedPassword = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyE3xLuPBG1y";
        
        boolean matches = PasswordUtil.matches(rawPassword, encodedPassword);
        System.out.println("Password matches: " + matches);
        
        // 生成新的加密密码用于测试
        String newEncoded = PasswordUtil.encode(rawPassword);
        System.out.println("New encoded password: " + newEncoded);
        
        boolean newMatches = PasswordUtil.matches(rawPassword, newEncoded);
        System.out.println("New password matches: " + newMatches);
    }
}
