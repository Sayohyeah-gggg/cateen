package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.vo.mini.AppConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端 - 配置管理控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-配置管理")
@Slf4j
@RestController
@RequestMapping("/api/mini/config")
@RequiredArgsConstructor
public class MiniConfigController {

    /**
     * 获取应用配置
     */
    @ApiOperation(value = "获取应用配置", notes = "获取小程序基础配置信息")
    @GetMapping("/app")
    public Result<AppConfigVO> getAppConfig() {
        log.info("获取应用配置");
        
        // 返回固定配置
        // 生产环境可以从数据库或配置中心读取
        AppConfigVO config = AppConfigVO.builder()
                .version("1.0.0")
                .announcement("欢迎使用美食评估小程序！")
                .servicePhone("400-123-4567")
                .serviceWechat("cateen_service")
                .commentAudit(false) // 评论不需要审核
                .dailyLuckyDrawLimit(3) // 每日抽奖次数限制
                .build();
        
        return Result.success(config);
    }
}