package com.xawl.cateen.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 角色更新DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(admin|user)$", message = "角色值不合法")
    private String role;

}

