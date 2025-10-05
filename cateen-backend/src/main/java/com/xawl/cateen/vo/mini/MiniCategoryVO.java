package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序分类VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniCategoryVO {
    
    /**
     * 分类ID
     */
    private String id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 美食数量
     */
    private Integer foodCount;
}
