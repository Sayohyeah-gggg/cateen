package com.xawl.cateen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserVO user;

}

