package com.xawl.cateen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection")
public class UserCollection {
    
    /**
     * 收藏ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 美食ID
     */
    private String foodId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}