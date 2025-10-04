package com.xawl.cateen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 美食DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@ApiModel(description = "美食信息")
@Data
public class FoodDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 美食名称
     */
    @ApiModelProperty(value = "美食名称", required = true, example = "宫保鸡丁")
    @NotBlank(message = "美食名称不能为空")
    @Size(min = 2, max = 100, message = "美食名称长度必须在2-100个字符之间")
    private String name;

    /**
     * 美食描述
     */
    @ApiModelProperty(value = "美食描述", example = "经典川菜，香辣可口")
    private String description;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID", example = "cat_002")
    private String categoryId;

    /**
     * 图片URL
     */
    @ApiModelProperty(value = "图片URL", example = "https://example.com/food.jpg")
    private String imageUrl;

    /**
     * 价格
     */
    @ApiModelProperty(value = "价格", example = "32.00")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /**
     * 标签ID列表
     */
    @ApiModelProperty(value = "标签ID列表", example = "[\"tag_001\", \"tag_004\"]")
    private List<String> tagIds;

}

