package com.xawl.cateen.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 榜单VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class RankingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 榜单ID
     */
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
     * 状态
     */
    private String status;

    /**
     * 榜单美食列表
     */
    private List<RankingFoodVO> foods;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 榜单美食VO
     */
    @Data
    public static class RankingFoodVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 关联ID
         */
        private String id;

        /**
         * 排名位置
         */
        private Integer rankPosition;

        /**
         * 美食信息
         */
        private FoodVO food;
    }

}

