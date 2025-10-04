package com.xawl.cateen.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 榜单DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class RankingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 榜单标题
     */
    @NotBlank(message = "榜单标题不能为空")
    @Size(min = 2, max = 100, message = "榜单标题长度必须在2-100个字符之间")
    private String title;

    /**
     * 榜单描述
     */
    private String description;

    /**
     * 榜单类型
     */
    private String type;

    /**
     * 美食ID列表（按排序）
     */
    private List<String> foodIds;

}

