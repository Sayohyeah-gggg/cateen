package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.mini.LuckyDrawService;
import com.xawl.cateen.vo.FoodVO;
import com.xawl.cateen.vo.mini.LuckyDrawResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端 - 幸运转盘控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-幸运转盘")
@Slf4j
@RestController
@RequestMapping("/api/mini/lucky")
@RequiredArgsConstructor
public class MiniLuckyController {

    private final LuckyDrawService luckyDrawService;

    /**
     * 获取转盘美食列表
     * 
     * 随机返回6-8个评分较高的美食用于转盘展示
     * 
     * @return 转盘美食列表
     */
    @ApiOperation(value = "获取转盘美食", notes = "获取用于转盘展示的随机美食")
    @GetMapping("/foods")
    public Result<List<FoodVO>> getFoods() {
        log.info("获取转盘美食");
        
        List<FoodVO> foods = luckyDrawService.getRandomFoods();
        
        return Result.success(foods);
    }

    /**
     * 执行抽奖
     * 
     * 从当前转盘美食中随机抽取一个
     * 
     * @return 抽中的美食信息和转盘角度
     */
    @ApiOperation(value = "执行抽奖", notes = "执行幸运转盘抽奖")
    @PostMapping("/draw")
    public Result<LuckyDrawResultVO> draw() {
        log.info("执行幸运抽奖");
        
        LuckyDrawResultVO result = luckyDrawService.draw();
        
        return Result.success("抽奖成功", result);
    }
}
