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
 * 访问统计实体类
 *
 * @author xawl
 * @date 2025-10-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("visit_stats")
public class VisitStat implements Serializable {

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
     * 访问次数
     */
    private Integer visits;

    /**
     * 独立访客数
     */
    private Integer uniqueVisitors;

    /**
     * 页面浏览量
     */
    private Integer pageViews;

    /**
     * 跳出率(%)
     */
    private Integer bounceRate;

    /**
     * 平均停留时间(秒)
     */
    private Integer avgStayTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

