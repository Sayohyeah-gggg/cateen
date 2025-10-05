package com.xawl.cateen.vo.mini;

import com.xawl.cateen.vo.FoodVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 幸运抽奖结果VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@ApiModel(description = "幸运抽奖结果")
public class LuckyDrawResultVO {

    @ApiModelProperty(value = "抽中的美食信息")
    private FoodVO food;

    @ApiModelProperty(value = "转盘旋转角度（前端动画使用）", example = "720")
    private Integer angle;

    @ApiModelProperty(value = "美食在转盘中的索引位置", example = "3")
    private Integer index;

    @ApiModelProperty(value = "抽奖时间戳", example = "1696512000000")
    private Long timestamp;
}
