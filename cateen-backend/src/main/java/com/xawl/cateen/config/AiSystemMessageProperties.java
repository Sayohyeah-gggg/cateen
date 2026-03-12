package com.xawl.cateen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI系统消息配置属性
 * 用于管理AI助手的系统提示信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "custom.ai.system.message")
public class AiSystemMessageProperties {

    /**
     * 美食推荐系统消息
     */
    private String foodRecommendation;

    /**
     * 聊天回复系统消息
     */
    private String chatResponse;
}
