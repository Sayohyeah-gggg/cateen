package com.xawl.cateen.dto.mini;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信小程序登录DTO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@ApiModel(description = "微信登录请求")
public class WxLoginDTO {

    @ApiModelProperty(value = "微信登录凭证code", required = true, example = "081xKWGa1RaVsL0KfiGa1234AB")
    @NotBlank(message = "微信登录凭证不能为空")
    private String code;

    @ApiModelProperty(value = "用户昵称", example = "微信用户")
    private String nickName;

    @ApiModelProperty(value = "用户头像", example = "https://thirdwx.qlogo.cn/xxx")
    private String avatarUrl;
}
