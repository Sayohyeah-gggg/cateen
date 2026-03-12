package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.AdminAiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端AI控制器
 * 为管理员提供AI助手服务，支持数据库查询和工具调用
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
@Api(tags = "9. 管理端AI助手")
public class AdminAiController {

    private final AdminAiService adminAiService;

    /**
     * 发送聊天消息（支持工具调用和数据库查询）
     */
    @PostMapping("/chat")
    @ApiOperation(value = "发送聊天消息", notes = "管理员发送消息，AI回复并支持数据库查询")
    public Result<String> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String userInput = request.get("message");
            if (userInput == null || userInput.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }

            String sessionId = request.getOrDefault("sessionId", "default_admin_session");

            log.info("收到管理端AI聊天请求：{}，会话ID：{}", userInput, sessionId);

            String aiResponse = adminAiService.getChatResponse(sessionId, userInput);

            return Result.success(aiResponse);

        } catch (Exception e) {
            log.error("管理端AI聊天处理失败：{}", e.getMessage(), e);
            return Result.error("AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取AI助手状态
     */
    @GetMapping("/status")
    @ApiOperation(value = "获取AI助手状态", notes = "检查管理端AI服务是否可用")
    public Result<Map<String, Object>> getAiStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("available", true);
            status.put("message", "管理端AI助手服务正常");
            status.put("features", new String[]{
                "用户数据查询",
                "评论数据分析",
                "美食统计",
                "活跃度分析",
                "打字机效果输出"
            });
            status.put("timestamp", System.currentTimeMillis());

            return Result.success(status);

        } catch (Exception e) {
            log.error("获取管理端AI状态失败：{}", e.getMessage(), e);
            Map<String, Object> status = new HashMap<>();
            status.put("available", false);
            status.put("message", "管理端AI助手服务异常");
            status.put("timestamp", System.currentTimeMillis());
            return Result.success(status);
        }
    }
}
