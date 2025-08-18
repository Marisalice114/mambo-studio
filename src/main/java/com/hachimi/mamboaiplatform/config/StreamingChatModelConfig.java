package com.hachimi.mamboaiplatform.config;


import com.hachimi.mamboaiplatform.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.List;

@Configuration
@ConfigurationProperties( prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private int maxTokens;

    private boolean logRequests;

    private boolean logResponses;

    private Duration timeout;

    private Double temperature;

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    /**
     * 流式推理模型
     * @return
     */
    @Bean
    @Scope("prototype") // Use prototype scope to create a new instance each time
    public StreamingChatModel streamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .timeout(timeout)
                .temperature(temperature)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }
}
