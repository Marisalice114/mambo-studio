package com.hachimi.mamboaiplatform.config;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties( prefix = "langchain4j.open-ai.fast-model")
@Data
public class FastChatConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private int maxTokens;

    private boolean logRequests;

    private boolean logResponses;

    private Duration timeout;

    /**
     * 普通任务推理模型
     * @return
     */
    @Bean
    public ChatModel fastChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .timeout(timeout)
                .build();
    }
}
