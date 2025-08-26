package com.hachimi.mamboaiplatform.service.impl;

import com.hachimi.mamboaiplatform.service.GenerationContentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 生成内容处理服务实现
 */
@Service
@Slf4j
public class GenerationContentProcessorImpl implements GenerationContentProcessor {
    
    // 匹配工具调用中的content字段
    private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\":\\s*\"([^\"]*?)\"");
    // 匹配tool_executed类型
    private static final Pattern TOOL_EXECUTED_PATTERN = Pattern.compile("\"type\":\\s*\"tool_executed\"");
    
    @Override
    public String processStreamChunk(String chunk) {
        if (chunk == null || chunk.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 检查是否是工具执行完成的消息
            if (TOOL_EXECUTED_PATTERN.matcher(chunk).find()) {
                // 提取content字段的内容
                Matcher contentMatcher = CONTENT_PATTERN.matcher(chunk);
                if (contentMatcher.find()) {
                    String content = contentMatcher.group(1);
                    // 处理转义字符
                    content = content.replace("\\n", "\n")
                                   .replace("\\t", "\t")
                                   .replace("\\\"", "\"")
                                   .replace("\\/", "/");
                    return content;
                }
            }
            
            // 如果不是工具调用相关的内容，直接返回（可能是普通的AI回复）
            if (!chunk.contains("tool_request") && !chunk.contains("tool_executed")) {
                return chunk;
            }
            
            return null; // 工具调用过程中的数据，不展示给用户
            
        } catch (Exception e) {
            log.warn("处理流数据块时出错: {}, 原始数据: {}", e.getMessage(), chunk);
            return null;
        }
    }
    
    @Override
    public boolean isGenerationComplete(String chunk) {
        if (chunk == null) return false;
        
        // 检查是否包含最终的tool_executed，并且是最后一个工具调用
        return chunk.contains("\"type\":\"tool_executed\"") && 
               (chunk.contains("\"result\":") || chunk.contains("文件写入成功"));
    }
    
    @Override
    public String extractFinalContent(String accumulatedContent) {
        if (accumulatedContent == null || accumulatedContent.trim().isEmpty()) {
            return "";
        }
        
        // 从累积内容中提取最终的完整内容
        // 这里可以根据项目需要进行更复杂的处理
        StringBuilder finalContent = new StringBuilder();
        
        // 按行处理，提取有效内容
        String[] lines = accumulatedContent.split("\n");
        for (String line : lines) {
            String processed = processStreamChunk(line);
            if (processed != null && !processed.trim().isEmpty()) {
                finalContent.append(processed).append("\n");
            }
        }
        
        return finalContent.toString().trim();
    }
}
