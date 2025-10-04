package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美食标签关联表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@TableName("food_tag_relations")
public class FoodTagRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 美食ID
     */
    private String foodId;

    /**
     * 标签ID
     */
    private String tagId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}

