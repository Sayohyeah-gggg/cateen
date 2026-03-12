package com.xawl.cateen.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AI配置测试
 */
@SpringBootTest
@TestPropertySource(properties = {
        "langchain4j.open-ai.chat-model.api-key=test-key",
        "langchain4j.open-ai.chat-model.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1",
        "langchain4j.open-ai.chat-model.model-name=qwen-plus"
})
class AiConfigTest {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Test
    void testChatLanguageModelBean() {
        assertNotNull(chatLanguageModel, "ChatLanguageModel bean should be created");
    }
}



