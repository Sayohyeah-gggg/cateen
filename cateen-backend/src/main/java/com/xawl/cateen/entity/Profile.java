package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@TableName("profiles")
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户唯一标识
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色：admin-管理员，user-普通用户
     */
    private String role;

    /**
     * 状态：active-启用，inactive-禁用
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}

