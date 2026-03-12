package com.xawl.cateen;

import com.xawl.cateen.util.PasswordUtil;

/**
 * 密码生成器 - 用于生成正确的密码哈希值
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        // 生成 admin123 的哈希值
        String password = "admin123";
        String hash = PasswordUtil.encode(password);
        System.out.println("密码: " + password);
        System.out.println("哈希值: " + hash);
        
        // 验证生成的哈希值
        boolean matches = PasswordUtil.matches(password, hash);
        System.out.println("验证结果: " + matches);
        
        System.out.println("\n=== SQL 更新语句 ===");
        System.out.println("UPDATE profiles SET password_hash = '" + hash + "' WHERE username = 'admin';");
    }
}
