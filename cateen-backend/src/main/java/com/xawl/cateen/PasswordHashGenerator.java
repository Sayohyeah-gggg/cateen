package com.xawl.cateen;

import com.xawl.cateen.util.PasswordUtil;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        System.out.println("=== 密码哈希生成器 ===");
        
        // 生成 admin123 的哈希
        String adminHash = PasswordUtil.encode("admin123");
        System.out.println("admin123 的哈希: " + adminHash);
        
        // 生成 123456 的哈希
        String userHash = PasswordUtil.encode("123456");
        System.out.println("123456 的哈希: " + userHash);
        
        System.out.println("\n=== 更新密码的 SQL 语句 ===");
        System.out.println("-- 更新 admin 用户的密码");
        System.out.println("UPDATE `profiles` SET `password_hash` = '" + adminHash + "' WHERE `username` = 'admin';");
        System.out.println();
        System.out.println("-- 更新 testuser 用户的密码");
        System.out.println("UPDATE `profiles` SET `password_hash` = '" + userHash + "' WHERE `username` = 'testuser';");
    }
}