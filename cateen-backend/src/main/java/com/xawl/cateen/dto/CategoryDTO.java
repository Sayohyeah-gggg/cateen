package com.xawl.cateen.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 美食分类DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 50, message = "分类名称长度必须在2-50个字符之间")
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;

}

