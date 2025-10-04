package com.xawl.cateen.controller;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.TagDTO;
import com.xawl.cateen.entity.FoodTag;
import com.xawl.cateen.service.FoodTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 美食标签控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class FoodTagController {

    private final FoodTagService tagService;

    /**
     * 获取标签列表
     */
    @GetMapping
    public Result<List<FoodTag>> getTagList(@RequestParam(required = false) String keyword) {
        log.info("获取标签列表: keyword={}", keyword);
        List<FoodTag> list = tagService.getTagList(keyword);
        return Result.success(list);
    }

    /**
     * 创建标签
     */
    @PostMapping
    public Result<FoodTag> createTag(@Valid @RequestBody TagDTO dto) {
        log.info("创建标签: name={}", dto.getName());
        FoodTag tag = tagService.createTag(dto);
        return Result.success("标签创建成功", tag);
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable String id, @Valid @RequestBody TagDTO dto) {
        log.info("更新标签: id={}, name={}", id, dto.getName());
        tagService.updateTag(id, dto);
        return Result.success("标签更新成功", null);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable String id) {
        log.info("删除标签: id={}", id);
        tagService.deleteTag(id);
        return Result.success("标签删除成功", null);
    }

}

