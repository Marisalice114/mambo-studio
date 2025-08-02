package com.hachimi.mamboaiplatform.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.ai.model.MultiFileCodeResult;

import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 * 用于解析AI生成的HTML代码和多文件代码响应
 */
@Component
@Slf4j
public class CodeParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 匹配HTML代码块的正则表达式
    private static final Pattern HTML_PATTERN = Pattern.compile(
        "```html\\s*\\n([\\s\\S]*?)\\n```", Pattern.CASE_INSENSITIVE
    );

    // 匹配CSS代码块的正则表达式
    private static final Pattern CSS_PATTERN = Pattern.compile(
        "```css\\s*\\n([\\s\\S]*?)\\n```", Pattern.CASE_INSENSITIVE
    );

    // 匹配JavaScript代码块的正则表达式
    private static final Pattern JS_PATTERN = Pattern.compile(
        "```(?:javascript|js)\\s*\\n([\\s\\S]*?)\\n```", Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析AI生成的HTML代码响应（JSON格式）
     *
     * @param aiResponse AI的原始响应内容
     * @return HtmlCodeResult 解析后的HTML代码结果
     */
    public HtmlCodeResult parseHtmlCode(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI响应内容为空");
        }

        try {
            // 提取JSON内容
            String jsonContent = extractJsonFromResponse(aiResponse);

            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonContent);

            HtmlCodeResult result = new HtmlCodeResult();

            // 对于HTML代码结果，只解析htmlCode和description字段
            result.setHtmlCode(getJsonValue(jsonNode, "htmlCode"));
            result.setDescription(getJsonValue(jsonNode, "description"));

            // 验证必要字段
            if (result.getHtmlCode() == null || result.getHtmlCode().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析失败：缺少HTML代码");
            }

            log.info("HTML代码解析成功，HTML长度: {}", result.getHtmlCode().length());

            return result;

        } catch (Exception e) {
            log.error("解析HTML代码失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析HTML代码失败: " + e.getMessage());
        }
    }

    /**
     * 解析AI生成的多文件代码响应（支持JSON格式和Markdown格式）
     *
     * @param aiResponse AI的原始响应内容
     * @return MultiFileCodeResult 解析后的多文件代码结果
     */
    public MultiFileCodeResult parseMultiFileCode(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI响应内容为空");
        }

        try {
            // 先尝试解析JSON格式
            if (isJsonFormat(aiResponse)) {
                return parseMultiFileFromJson(aiResponse);
            } else {
                // 解析Markdown格式
                return parseMultiFileFromMarkdown(aiResponse);
            }

        } catch (Exception e) {
            log.error("解析多文件代码失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析多文件代码失败: " + e.getMessage());
        }
    }

    /**
     * 从JSON格式解析多文件代码
     */
    private MultiFileCodeResult parseMultiFileFromJson(String aiResponse) {
        try {
            // 提取JSON内容
            String jsonContent = extractJsonFromResponse(aiResponse);

            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonContent);

            MultiFileCodeResult result = new MultiFileCodeResult();
            result.setHtmlCode(getJsonValue(jsonNode, "htmlCode"));
            result.setCssCode(getJsonValue(jsonNode, "cssCode"));
            result.setJsCode(getJsonValue(jsonNode, "jsCode"));
            result.setDescription(getJsonValue(jsonNode, "description"));

            // 验证必要字段
            if (result.getHtmlCode() == null || result.getHtmlCode().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析失败：缺少HTML代码");
            }

            log.info("多文件代码解析成功（JSON格式），HTML长度: {}, CSS长度: {}, JS长度: {}",
                result.getHtmlCode().length(),
                result.getCssCode() != null ? result.getCssCode().length() : 0,
                result.getJsCode() != null ? result.getJsCode().length() : 0);

            return result;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析JSON格式多文件代码失败: " + e.getMessage());
        }
    }

    /**
     * 从Markdown格式解析多文件代码
     */
    private MultiFileCodeResult parseMultiFileFromMarkdown(String aiResponse) {
        try {
            MultiFileCodeResult result = new MultiFileCodeResult();

            // 解析HTML代码
            String htmlCode = extractCodeBlock(aiResponse, HTML_PATTERN, "HTML");
            result.setHtmlCode(htmlCode);

            // 解析CSS代码
            String cssCode = extractCodeBlock(aiResponse, CSS_PATTERN, "CSS");
            result.setCssCode(cssCode);

            // 解析JavaScript代码
            String jsCode = extractCodeBlock(aiResponse, JS_PATTERN, "JavaScript");
            result.setJsCode(jsCode);

            // 提取描述信息（通常在代码块外的文本）
            String description = extractDescription(aiResponse);
            result.setDescription(description);

            // 验证必要字段
            if (result.getHtmlCode() == null || result.getHtmlCode().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析失败：缺少HTML代码");
            }

            log.info("多文件代码解析成功（Markdown格式），HTML长度: {}, CSS长度: {}, JS长度: {}",
                result.getHtmlCode().length(),
                result.getCssCode() != null ? result.getCssCode().length() : 0,
                result.getJsCode() != null ? result.getJsCode().length() : 0);

            return result;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析Markdown格式多文件代码失败: " + e.getMessage());
        }
    }

    /**
     * 判断响应是否为JSON格式
     */
    private boolean isJsonFormat(String response) {
        try {
            // 尝试提取JSON内容
            String jsonContent = extractJsonFromResponse(response);
            objectMapper.readTree(jsonContent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从AI响应中提取JSON内容
     * 支持从AI的complete response中提取content字段中的JSON
     */
    private String extractJsonFromResponse(String response) {
        try {
            // 首先尝试解析整个响应作为JSON（适用于AI API的完整响应）
            JsonNode responseNode = objectMapper.readTree(response);

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
    private String extractJsonFromContent(String content) {
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
    private String getJsonValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }

    /**
     * 从响应中提取指定类型的代码块
     */
    private String extractCodeBlock(String response, Pattern pattern, String codeType) {
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
    private String extractDescription(String response) {
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
