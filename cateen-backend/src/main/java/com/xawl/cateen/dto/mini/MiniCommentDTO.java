package com.xawl.cateen.dto.mini;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 小程序评论DTO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
public class MiniCommentDTO {
    
    /**
     * 评分（1-5星）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;
    
    /**
     * 评论内容
     */
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;
    
    /**
     * 评论图片URL列表
     */
    private List<String> images;
    
    /**
     * 口味评分（1-5）
     */
    @Min(value = 1, message = "口味评分最小为1")
    @Max(value = 5, message = "口味评分最大为5")
    private Integer tasteRating;
    
    /**
     * 环境评分（1-5）
     */
    @Min(value = 1, message = "环境评分最小为1")
    @Max(value = 5, message = "环境评分最大为5")
    private Integer environmentRating;
    
    /**
     * 服务评分（1-5）
     */
    @Min(value = 1, message = "服务评分最小为1")
    @Max(value = 5, message = "服务评分最大为5")
    private Integer serviceRating;
}
