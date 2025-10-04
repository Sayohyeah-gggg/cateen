package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美食标签表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@TableName("food_tags")
public class FoodTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}

