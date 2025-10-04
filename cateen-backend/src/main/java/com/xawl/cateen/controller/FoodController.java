package com.xawl.cateen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.FoodDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.FoodService;
import com.xawl.cateen.vo.FoodVO;
import com.xawl.cateen.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 美食管理控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "3. 美食管理")
@Slf4j
@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    /**
     * 分页查询美食列表
     */
    @ApiOperation(value = "分页查询美食列表", notes = "支持关键词搜索、分类筛选、状态筛选")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", defaultValue = "1", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", defaultValue = "20", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "categoryId", value = "分类ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态(active/inactive)", dataType = "String", paramType = "query")
    })
    @GetMapping
    public Result<PageVO<FoodVO>> getFoodPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status) {
        log.info("分页查询美食列表: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<FoodVO> page = foodService.getFoodPage(pageNum, pageSize, keyword, categoryId, status);
        
        PageVO<FoodVO> pageVO = PageVO.<FoodVO>builder()
                .list(page.getRecords())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .build();
        
        return Result.success(pageVO);
    }

    /**
     * 获取美食详情
     */
    @GetMapping("/{id}")
    public Result<FoodVO> getFoodDetail(@PathVariable String id) {
        log.info("获取美食详情: id={}", id);
        FoodVO foodVO = foodService.getFoodDetail(id);
        return Result.success(foodVO);
    }

    /**
     * 创建美食
     */
    @PostMapping
    public Result<FoodVO> createFood(@Valid @RequestBody FoodDTO dto) {
        log.info("创建美食: name={}", dto.getName());
        FoodVO foodVO = foodService.createFood(dto);
        return Result.success("美食创建成功", foodVO);
    }

    /**
     * 更新美食
     */
    @PutMapping("/{id}")
    public Result<Void> updateFood(@PathVariable String id, @Valid @RequestBody FoodDTO dto) {
        log.info("更新美食: id={}, name={}", id, dto.getName());
        foodService.updateFood(id, dto);
        return Result.success("美食更新成功", null);
    }

    /**
     * 删除美食
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFood(@PathVariable String id) {
        log.info("删除美食: id={}", id);
        foodService.deleteFood(id);
        return Result.success("美食删除成功", null);
    }

    /**
     * 更新美食状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateFoodStatus(@PathVariable String id, @Valid @RequestBody StatusDTO dto) {
        log.info("更新美食状态: id={}, status={}", id, dto.getStatus());
        foodService.updateFoodStatus(id, dto.getStatus());
        return Result.success("状态更新成功", null);
    }

    /**
     * 获取热门美食
     */
    @GetMapping("/popular")
    public Result<List<FoodVO>> getPopularFoods(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门美食: limit={}", limit);
        List<FoodVO> list = foodService.getPopularFoods(limit);
        return Result.success(list);
    }

}

