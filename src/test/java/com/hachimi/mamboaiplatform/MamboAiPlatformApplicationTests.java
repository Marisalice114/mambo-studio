package com.hachimi.mamboaiplatform;

import com.hachimi.mamboaiplatform.ai.AiCodeGeneratorService;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.ai.model.MultiFileCodeResult;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private ChatModel chatModel;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("帮我生成一个网页页面，最多20行");
        assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个程序员鱼皮的留言板");
        assertNotNull(multiFileCode);
    }

    @Test
    void testChatModel() {
        try {
            String response = chatModel.chat("Hello, how are you?");
            System.out.println("✅ ChatModel 测试成功: " + response);
            assertNotNull(response);
        } catch (Exception e) {
            System.err.println("❌ ChatModel 测试失败: " + e.getMessage());
            fail("ChatModel test failed: " + e.getMessage());
        }
    }
}

