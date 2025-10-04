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
 * 设备统计实体类
 *
 * @author xawl
 * @date 2025-10-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("device_stats")
public class DeviceStat implements Serializable {

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
     * 设备类型: desktop/mobile/tablet
     */
    private String deviceType;

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

