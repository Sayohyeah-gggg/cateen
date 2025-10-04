package com.xawl.cateen.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 美食标签DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class TagDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(min = 2, max = 20, message = "标签名称长度必须在2-20个字符之间")
    private String name;

    /**
     * 标签颜色（HEX格式）
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，必须是HEX格式，如：#3b82f6")
    private String color;

}

