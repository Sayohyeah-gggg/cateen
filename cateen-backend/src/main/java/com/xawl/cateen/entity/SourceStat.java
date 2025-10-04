package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 来源统计实体类
 *
 * @author xawl
 * @date 2025-10-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("source_stats")
public class SourceStat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 来源类型: direct/search/social/referral/other
     */
    private String sourceType;

    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

