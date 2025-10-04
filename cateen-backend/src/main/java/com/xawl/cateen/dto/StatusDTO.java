package com.xawl.cateen.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 状态更新DTO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
public class StatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|inactive|pending|approved|rejected)$", 
             message = "状态值不合法")
    private String status;

}

