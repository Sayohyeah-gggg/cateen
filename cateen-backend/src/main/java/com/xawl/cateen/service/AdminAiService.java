package com.xawl.cateen.service;

import com.xawl.cateen.config.LangChain4jConfig.AdminAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 管理端AI服务
 * 提供管理员专用的AI助手功能，支持数据库查询和工具调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAiService {

    private final AdminAssistant adminAssistant;

    /**
     * 获取AI聊天回复（支持工具调用和数据库查询）
     * 
     * @param sessionId 会话ID
     * @param userInput 用户输入
     * @return AI回复
     */
    public String getChatResponse(String sessionId, String userInput) {
        try {
            String memoryId = "admin_session_" + sessionId;
            String response = adminAssistant.chat(memoryId, userInput);
            
            log.info("管理端AI回复完成，用户输入：{}，会话ID：{}", userInput, sessionId);
            return response;

        } catch (Exception e) {
            log.error("管理端AI回复失败：{}", e.getMessage(), e);
            return getFallbackResponse();
        }
    }

    /**
     * 获取降级回复
     */
    private String getFallbackResponse() {
        return "🤖 抱歉，AI服务暂时不可用。\n\n" +
                "您可以尝试：\n" +
                "1. 刷新页面重试\n" +
                "2. 检查网络连接\n" +
                "3. 联系技术支持\n\n" +
                "（AI服务正在恢复中，请稍后再试）";
    }
}
