package com.xawl.cateen.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子返回VO
 */
@Data
public class ForumPostVO {

    private String id;
    private String userId;
    private String content;
    /** 图片URL列表（序列化给前端） */
    private List<String> imageList;
    /** 原始JSON字符串，仅内部使用 */
    @JsonIgnore
    private String images;
    /** 视频URL */
    private String video;
    private Integer likeCount;
    private Integer commentCount;
    private String status;
    private LocalDateTime createdAt;

    /** 作者昵称 */
    private String userNickname;
    /** 作者头像 */
    private String userAvatar;
    /** 当前用户是否已点赞 */
    private Boolean liked;
}
