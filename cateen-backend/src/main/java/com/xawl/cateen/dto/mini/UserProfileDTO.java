package com.xawl.cateen.dto.mini;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 用户资料更新DTO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
public class UserProfileDTO {
    
    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickName;
    
    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatar;
}
