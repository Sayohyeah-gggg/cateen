package com.xawl.cateen.controller;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器
 *
 * @author xawl
 * @date 2025-10-04
 */
@Api(tags = "9. 系统管理")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @ApiOperation(value = "同步美食评分统计", notes = "根据评论表重新计算所有美食的评分和评论数量")
    @PostMapping("/sync-food-ratings")
    public Result<String> syncFoodRatings() {
        int count = adminService.syncFoodRatings();
        return Result.success("同步完成，共更新 " + count + " 个美食的评分统计");
    }
}

