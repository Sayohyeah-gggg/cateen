package com.xawl.cateen.vo.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序用户资料VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniUserProfileVO {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 收藏数量
     */
    private Integer collectionCount;
    
    /**
     * 评论数量
     */
    private Integer commentCount;
    
    /**
     * 点赞获得数
     */
    private Integer likeCount;
}
