package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序应用配置VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigVO {
    
    /**
     * 应用版本号
     */
    private String version;
    
    /**
     * 公告信息
     */
    private String announcement;
    
    /**
     * 客服电话
     */
    private String servicePhone;
    
    /**
     * 客服微信
     */
    private String serviceWechat;
    
    /**
     * 是否开启评论审核
     */
    private Boolean commentAudit;
    
    /**
     * 每日抽奖次数限制
     */
    private Integer dailyLuckyDrawLimit;
}
