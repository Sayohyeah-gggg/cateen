package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.TagDTO;
import com.xawl.cateen.entity.FoodTag;
import com.xawl.cateen.service.FoodTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端 - 美食标签控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "管理端-标签管理")
@Slf4j
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final FoodTagService tagService;

    /**
     * 获取标签列表
     */
    @ApiOperation(value = "获取标签列表", notes = "获取所有美食标签，支持关键词搜索")
    @GetMapping
    public Result<List<FoodTag>> getTagList(@ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword) {
        log.info("获取标签列表: keyword={}", keyword);
        List<FoodTag> list = tagService.getTagList(keyword);
        return Result.success(list);
    }

    /**
     * 创建标签
     */
    @ApiOperation(value = "创建标签", notes = "创建新的美食标签")
    @PostMapping
    public Result<FoodTag> createTag(@ApiParam(value = "标签信息", required = true) @Valid @RequestBody TagDTO dto) {
        log.info("创建标签: name={}", dto.getName());
        FoodTag tag = tagService.createTag(dto);
        return Result.success("标签创建成功", tag);
    }

    /**
     * 更新标签
     */
    @ApiOperation(value = "更新标签", notes = "更新指定标签的信息")
    @PutMapping("/{id}")
    public Result<Void> updateTag(
            @ApiParam(value = "标签ID", required = true) @PathVariable String id, 
            @ApiParam(value = "标签信息", required = true) @Valid @RequestBody TagDTO dto) {
        log.info("更新标签: id={}, name={}", id, dto.getName());
        tagService.updateTag(id, dto);
        return Result.success("标签更新成功", null);
    }

    /**
     * 删除标签
     */
    @ApiOperation(value = "删除标签", notes = "删除指定的美食标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@ApiParam(value = "标签ID", required = true) @PathVariable String id) {
        log.info("删除标签: id={}", id);
        tagService.deleteTag(id);
        return Result.success("标签删除成功", null);
    }

}
