package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 榜单美食关联表
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@TableName("ranking_foods")
public class RankingFood implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 榜单ID
     */
    private String rankingId;

    /**
     * 美食ID
     */
    private String foodId;

    /**
     * 排名位置
     */
    private Integer rankPosition;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}

