package com.xawl.cateen.vo.mini;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 微信登录返回VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@ApiModel(description = "微信登录响应")
public class WxLoginVO {

    @ApiModelProperty(value = "JWT访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @ApiModelProperty(value = "用户ID", example = "1001")
    private String userId;

    @ApiModelProperty(value = "微信OpenID", example = "oXYZ123456789")
    private String openId;

    @ApiModelProperty(value = "用户昵称", example = "美食爱好者")
    private String nickName;

    @ApiModelProperty(value = "用户头像", example = "https://xxx.com/avatar.jpg")
    private String avatarUrl;

    @ApiModelProperty(value = "是否新用户", example = "false")
    private Boolean isNewUser;
}
