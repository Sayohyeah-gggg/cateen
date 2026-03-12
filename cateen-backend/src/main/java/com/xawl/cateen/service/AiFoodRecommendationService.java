package com.xawl.cateen.service;

import com.xawl.cateen.config.LangChain4jConfig.FoodAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI美食推荐服务
 * 基于LangChain4j实现智能美食推荐，支持会话记忆和工具调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFoodRecommendationService {

    private final FoodAssistant foodAssistant;

    /**
     * 获取AI美食推荐
     * 
     * @param userInput 用户输入
     * @param foodData  美食数据上下文（已废弃，AI会自动调用工具查询数据库）
     * @return AI推荐结果
     */
    public String getFoodRecommendation(String userInput, String foodData) {
        try {
            // 使用会话记忆ID
            String memoryId = "recommend_" + System.currentTimeMillis();

            // 直接传递用户输入，AI会根据系统提示自动使用工具
            String response = foodAssistant.chat(memoryId, userInput);

            log.info("AI推荐完成（使用工具），用户输入：{}", userInput);
            return response;

        } catch (Exception e) {
            log.error("AI推荐失败：{}", e.getMessage(), e);
            return getFallbackResponse(userInput);
        }
    }

    /**
     * 获取AI聊天回复
     * 
     * @param userInput 用户输入
     * @return AI回复
     */
    public String getChatResponse(String userInput) {
        return getChatResponse("default_session", userInput);
    }

    /**
     * 获取AI聊天回复（指定会话ID）
     * 
     * @param sessionId 会话ID
     * @param userInput 用户输入
     * @return AI回复
     */
    public String getChatResponse(String sessionId, String userInput) {
        try {
            // 使用固定的会话ID来维护会话记忆
            String memoryId = "session_" + sessionId;

            // 直接传递用户输入，AI会根据系统提示自动使用工具
            String response = foodAssistant.chat(memoryId, userInput);

            log.info("AI聊天回复完成（使用工具），用户输入：{}，会话ID：{}", userInput, sessionId);
            return response;

        } catch (Exception e) {
            log.error("AI聊天回复失败：{}", e.getMessage(), e);
            return getFallbackResponse(userInput);
        }
    }

    /**
     * 获取降级回复（AI服务不可用时的备用回复）
     */
    private String getFallbackResponse(String userInput) {
        return "🤖 抱歉，AI服务暂时不可用，但我仍然可以为您推荐美食！\n\n" +
                "根据您的输入，我为您推荐以下经典美食：\n\n" +
                "🍽️ **宫保鸡丁** - 川菜经典，麻辣鲜香，下饭神器\n" +
                "🍽️ **红烧肉** - 家常美味，肥瘦相间，入口即化\n" +
                "🍽️ **小笼包** - 江南特色，皮薄馅大，汤汁丰富\n\n" +
                "💡 **小贴士**：如果您有特殊需求或偏好，请告诉我，我会为您提供更精准的推荐！\n\n" +
                "（AI服务正在恢复中，请稍后再试）";
    }
}
