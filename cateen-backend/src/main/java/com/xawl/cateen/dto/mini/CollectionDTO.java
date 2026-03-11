package com.xawl.cateen.dto.mini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 收藏DTO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDTO {
    
    /**
     * 美食ID
     */
    @NotBlank(message = "美食ID不能为空")
    private String foodId;
}
