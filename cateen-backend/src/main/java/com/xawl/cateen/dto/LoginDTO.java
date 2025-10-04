package com.xawl.cateen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@ApiModel(description = "登录请求参数")
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", required = true, example = "admin123")
    @NotBlank(message = "密码不能为空")
    private String password;

}

