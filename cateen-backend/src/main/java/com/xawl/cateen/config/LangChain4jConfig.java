package com.xawl.cateen.config;

import com.xawl.cateen.service.tools.AdminDatabaseTools;
import com.xawl.cateen.service.tools.FoodDatabaseTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j配置类
 * 手动配置ChatLanguageModel Bean和AI服务
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name:qwen-plus}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:2000}")
    private Integer maxTokens;

    /**
     * 配置ChatLanguageModel Bean
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("配置ChatLanguageModel - API Key: {}, Base URL: {}, Model: {}",
                apiKey.isEmpty() ? "未设置" : "已设置", baseUrl, modelName);

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * AI助手接口 - 支持工具调用（小程序端）
     */
    public interface FoodAssistant {
        @dev.langchain4j.service.SystemMessage({
            "你是一位专业的美食推荐助手，名叫「食堂小助手」。",
            "",
            "你的职责：",
            "1. 帮助用户了解食堂的美食信息",
            "2. 根据用户需求推荐合适的菜品",
            "3. 回答关于美食的价格、评分、口味等问题",
            "",
            "你可以使用以下工具查询真实的数据库数据：",
            "- getPopularFoods: 查询热门美食",
            "- getFoodDetail: 查询美食详情",
            "- searchFoods: 搜索美食",
            "- getFoodsByCategory: 按分类查询",
            "- getHighRatedFoods: 查询高评分美食",
            "",
            "回复风格：",
            "- 友好、热情、专业",
            "- 使用表情符号增加亲和力（🍽️ 🌟 💡等）",
            "- 提供具体的美食信息（名称、价格、评分）",
            "- 根据用户偏好给出个性化建议",
            "",
            "重要提示：",
            "- 当用户询问美食信息时，优先使用工具查询数据库",
            "- 基于真实数据给出推荐，不要编造信息",
            "- 如果数据库中没有相关信息，诚实告知用户"
        })
        String chat(@MemoryId String memoryId, @UserMessage String userMessage);
    }

    /**
     * 管理端AI助手接口 - 支持管理功能（带工具调用）
     */
    public interface AdminAssistant {
        @dev.langchain4j.service.SystemMessage({
            "你是美食管理系统的后台助手。",
            "",
            "职责：帮助管理员查询和分析系统数据，包括用户、美食、评论、食友分享帖子等。",
            "",
            "可用工具：",
            "- getUserStatistics: 用户统计",
            "- getUserDetail: 用户详情",
            "- searchUsers: 搜索用户",
            "- getCommentStatistics: 评论统计",
            "- getCommentsByFood: 指定美食的评论",
            "- getFoodStatistics: 美食统计",
            "- getActivityStatistics: 用户和评论活跃度统计",
            "- getForumPostStatistics: 帖子统计（总数、状态分布、最近帖子）",
            "- searchForumPosts: 按关键词搜索帖子",
            "- getForumCommentsByPost: 指定帖子的评论",
            "- getForumActivityStatistics: 帖子活跃度统计",
            "- generateExcel(type): 生成Excel报表，type可选 users/foods/comments/posts",
            "- generatePpt(): 生成系统数据概览PPT报告",
            "",
            "回复要求：",
            "- 直接给出数据和结论，不加多余修饰",
            "- 不使用表情符号",
            "- 数据不足时如实说明",
            "- 优先调用工具获取真实数据，不编造"
        })
        String chat(@MemoryId String memoryId, @UserMessage String userMessage);
    }

    /**
     * 配置支持工具调用的AI助手（小程序端）
     */
    @Bean
    public FoodAssistant foodAssistant(ChatLanguageModel chatLanguageModel, 
                                       FoodDatabaseTools foodDatabaseTools) {
        log.info("配置FoodAssistant - 启用数据库工具支持");
        
        return AiServices.builder(FoodAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(foodDatabaseTools)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    /**
     * 配置管理端AI助手（带工具，支持数据库查询）
     */
    @Bean
    public AdminAssistant adminAssistant(ChatLanguageModel chatLanguageModel,
                                        AdminDatabaseTools adminDatabaseTools) {
        log.info("配置AdminAssistant - 启用管理工具支持");
        
        return AiServices.builder(AdminAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(adminDatabaseTools)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
