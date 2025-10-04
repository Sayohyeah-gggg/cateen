package com.xawl.cateen.service;

import com.xawl.cateen.dto.TagDTO;
import com.xawl.cateen.entity.FoodTag;

import java.util.List;

/**
 * 美食标签服务接口
 *
 * @author xawl
 * @date 2025-10-03
 */
public interface FoodTagService {

    /**
     * 获取标签列表
     *
     * @param keyword 搜索关键词
     * @return 标签列表
     */
    List<FoodTag> getTagList(String keyword);

    /**
     * 创建标签
     *
     * @param dto 标签信息
     * @return 标签
     */
    FoodTag createTag(TagDTO dto);

    /**
     * 更新标签
     *
     * @param id 标签ID
     * @param dto 标签信息
     */
    void updateTag(String id, TagDTO dto);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(String id);

}

