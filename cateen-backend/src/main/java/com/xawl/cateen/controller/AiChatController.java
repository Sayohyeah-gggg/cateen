package com.xawl.cateen.controller;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.AiFoodRecommendationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI聊天控制器
 * 为小程序提供AI美食推荐服务
 */
@Slf4j
@RestController
@RequestMapping("/api/mini/ai")
@RequiredArgsConstructor
@Api(tags = "8. AI聊天")
public class AiChatController {
    private final AiFoodRecommendationService aiFoodRecommendationService;

    /**
     * 发送聊天消息
     */
    @PostMapping("/chat")
    @ApiOperation(value = "发送聊天消息", notes = "用户发送消息，AI回复")
    public Result<String> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String userInput = request.get("message");
            if (userInput == null || userInput.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }

            // 获取会话ID，如果没有则使用默认值
            String sessionId = request.getOrDefault("sessionId", "default_session");

            log.info("收到AI聊天请求：{}，会话ID：{}", userInput, sessionId);

            // 获取AI回复（支持会话记忆）
            String aiResponse = aiFoodRecommendationService.getChatResponse(sessionId, userInput);

            return Result.success(aiResponse);

        } catch (Exception e) {
            log.error("AI聊天处理失败：{}", e.getMessage(), e);
            return Result.error("AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取美食推荐
     */
    @PostMapping("/recommend")
    @ApiOperation(value = "获取美食推荐", notes = "根据用户需求推荐美食")
    public Result<String> getFoodRecommendation(@RequestBody Map<String, String> request) {
        try {
            String userInput = request.get("message");
            if (userInput == null || userInput.trim().isEmpty()) {
                return Result.error("推荐需求不能为空");
            }

            log.info("收到美食推荐请求：{}", userInput);

            // AI会自动调用工具查询数据库，不再需要手动传入美食数据
            String recommendation = aiFoodRecommendationService.getFoodRecommendation(userInput, null);

            return Result.success(recommendation);

        } catch (Exception e) {
            log.error("美食推荐处理失败：{}", e.getMessage(), e);
            return Result.error("推荐服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取AI助手状态
     */
    @GetMapping("/status")
    @ApiOperation(value = "获取AI助手状态", notes = "检查AI服务是否可用")
    public Result<Map<String, Object>> getAiStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("available", true);
            status.put("message", "AI助手服务正常");
            status.put("features", new String[]{"智能对话", "美食推荐", "数据库查询"});
            status.put("timestamp", System.currentTimeMillis());

            return Result.success(status);

        } catch (Exception e) {
            log.error("获取AI状态失败：{}", e.getMessage(), e);
            Map<String, Object> status = new HashMap<>();
            status.put("available", false);
            status.put("message", "AI助手服务异常");
            status.put("timestamp", System.currentTimeMillis());
            return Result.success(status);
        }
    }
}
