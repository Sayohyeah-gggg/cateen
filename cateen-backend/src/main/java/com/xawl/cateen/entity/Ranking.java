package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 榜单表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@TableName("rankings")
public class Ranking implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 榜单标题
     */
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
     * 状态：active-启用，inactive-禁用
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}

