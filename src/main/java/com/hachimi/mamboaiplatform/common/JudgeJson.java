package com.hachimi.mamboaiplatform.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON处理工具类
 */
@Slf4j
public class JudgeJson {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 判断响应是否为JSON格式
     */
    public static boolean isJsonFormat(String response) {
        try {
            // 尝试提取JSON内容
            String jsonContent = extractJsonFromResponse(response);
            OBJECT_MAPPER.readTree(jsonContent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从AI响应中提取JSON内容
     * 支持从AI的complete response中提取content字段中的JSON
     */
    public static String extractJsonFromResponse(String response) {
        try {
            // 首先尝试解析整个响应作为JSON（适用于AI API的完整响应）
            JsonNode responseNode = OBJECT_MAPPER.readTree(response);

            // 检查是否是AI API的响应格式
            if (responseNode.has("choices")) {
                JsonNode choices = responseNode.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    if (firstChoice.has("message")) {
                        JsonNode message = firstChoice.get("message");
                        if (message.has("content")) {
                            String content = message.get("content").asText();
                            // content中包含JSON字符串，需要进一步解析
                            return extractJsonFromContent(content);
                        }
                    }
                }
            }

            // 如果不是AI API格式，直接查找JSON内容
            return extractJsonFromContent(response);

        } catch (Exception e) {
            // 如果解析失败，尝试直接提取JSON内容
            return extractJsonFromContent(response);
        }
    }

    /**
     * 从内容字符串中提取JSON
     */
    private static String extractJsonFromContent(String content) {
        // 查找JSON开始和结束位置
        int jsonStart = content.indexOf("{");
        int jsonEnd = content.lastIndexOf("}");

        if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应中未找到有效的JSON内容");
        }

        return content.substring(jsonStart, jsonEnd + 1);
    }

    /**
     * 安全地从JSON节点获取字符串值
     */
    public static String getJsonValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }

    /**
     * 从响应中提取指定类型的代码块
     */
    public static String extractCodeBlock(String response, Pattern pattern, String codeType) {
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String code = matcher.group(1).trim();
            log.debug("提取到{}代码，长度: {}", codeType, code.length());
            return code;
        }

        log.warn("未找到{}代码块", codeType);
        return null;
    }

    /**
     * 提取描述信息
     * 通常是代码块之外的文本内容
     */
    public static String extractDescription(String response) {
        try {
            // 移除所有代码块，剩下的就是描述性文本
            String description = response
                    .replaceAll("```[\\s\\S]*?```", "") // 移除所有代码块
                    .replaceAll("#{1,6}\\s*[^\\n]*", "") // 移除markdown标题
                    .trim();

            // 如果描述太长，截取前500个字符
            if (description.length() > 500) {
                description = description.substring(0, 500) + "...";
            }

            return description.isEmpty() ? null : description;
        } catch (Exception e) {
            log.warn("提取描述信息失败: {}", e.getMessage());
            return null;
        }
    }
}
