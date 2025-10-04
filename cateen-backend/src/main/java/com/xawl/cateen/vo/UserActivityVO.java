package com.xawl.cateen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户活动统计VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String date;

    /**
     * 新增用户数
     */
    private Integer newUsers;

    /**
     * 新增评论数
     */
    private Integer newComments;

    /**
     * 新增美食数
     */
    private Integer newFoods;

}

