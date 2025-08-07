package com.hachimi.mamboaiplatform.config;


import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties( prefix = "langchain4j.open-ai.reasoning-model")
@Data
public class ReasoningStreamingChatConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private int maxTokens;

    private boolean logRequests;

    private boolean logResponses;

    private Duration timeout;

    /**
     * 流式推理模型
     * @return
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
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
