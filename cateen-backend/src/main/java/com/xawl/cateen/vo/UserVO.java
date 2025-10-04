package com.xawl.cateen.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}

