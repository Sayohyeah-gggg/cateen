package com.xawl.cateen.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.dto.TagDTO;
import com.xawl.cateen.entity.FoodTag;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.mapper.FoodTagMapper;
import com.xawl.cateen.service.FoodTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 美食标签服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodTagServiceImpl implements FoodTagService {

    private final FoodTagMapper tagMapper;

    @Override
    public List<FoodTag> getTagList(String keyword) {
        LambdaQueryWrapper<FoodTag> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(FoodTag::getName, keyword);
        }
        wrapper.orderByDesc(FoodTag::getCreatedAt);
        return tagMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FoodTag createTag(TagDTO dto) {
        FoodTag tag = new FoodTag();
        tag.setId(IdUtil.getSnowflakeNextIdStr());
        tag.setName(dto.getName());
        tag.setColor(StrUtil.isNotBlank(dto.getColor()) ? dto.getColor() : "#3b82f6");

        tagMapper.insert(tag);
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(String id, TagDTO dto) {
        FoodTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }

        tag.setName(dto.getName());
        if (StrUtil.isNotBlank(dto.getColor())) {
            tag.setColor(dto.getColor());
        }

        tagMapper.updateById(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(String id) {
        FoodTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }

        tagMapper.deleteById(id);
    }

}

