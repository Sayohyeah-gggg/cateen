package com.xawl.cateen.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Map;

/**
 * 评论DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class CommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 美食ID
     */
    @NotBlank(message = "美食ID不能为空")
    private String foodId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 5, max = 500, message = "评论内容长度必须在5-500个字符之间")
    private String content;

    /**
     * 评分(1-5)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;

    /**
     * 多维度评分（可选）
     * key: taste(口味), price(价格), environment(环境), service(服务)
     * value: 1-5分
     */
    private Map<String, Integer> preferences;

}

