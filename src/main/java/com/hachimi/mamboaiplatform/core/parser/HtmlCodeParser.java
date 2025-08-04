package com.hachimi.mamboaiplatform.core.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import static com.hachimi.mamboaiplatform.constant.CodePattern.HTML_PATTERN;


@Slf4j
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    @Resource
    private JudgeJson judgeJson;

    /**
     * 从JSON格式解析HTML代码
     */
    private HtmlCodeResult parseHtmlCodeFromJson(String aiResponse) {
        try {
            // 提取JSON内容
            String jsonContent = judgeJson.extractJsonFromResponse(aiResponse);

            ObjectMapper objectMapper = judgeJson.getObjectMapper();
            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonContent);

            HtmlCodeResult result = new HtmlCodeResult();

            // 对于HTML代码结果，只解析htmlCode和description字段
            result.setHtmlCode(judgeJson.getJsonValue(jsonNode, "htmlCode"));
            result.setDescription(judgeJson.getJsonValue(jsonNode, "description"));

            // 验证必要字段
            if (result.getHtmlCode() == null || result.getHtmlCode().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析失败：缺少HTML代码");
            }

            log.info("HTML代码解析成功（JSON格式），HTML长度: {}", result.getHtmlCode().length());

            return result;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析JSON格式HTML代码失败: " + e.getMessage());
        }
    }

    /**
     * 从Markdown格式解析HTML代码
     */
    private HtmlCodeResult parseHtmlCodeFromMarkdown(String aiResponse) {
        try {
            HtmlCodeResult result = new HtmlCodeResult();

            // 解析HTML代码
            String htmlCode = judgeJson.extractCodeBlock(aiResponse, HTML_PATTERN, "HTML");

            // 如果没找到HTML代码块，尝试将整个内容作为HTML
            if (htmlCode == null || htmlCode.trim().isEmpty()) {
                // 移除可能的markdown标记，将剩余内容作为HTML
                String cleanedContent = aiResponse
                        .replaceAll("```[\\s\\S]*?```", "") // 移除代码块
                        .replaceAll("#{1,6}\\s*[^\\n]*", "") // 移除标题
                        .trim();

                if (!cleanedContent.isEmpty()) {
                    htmlCode = cleanedContent;
                }
            }

            result.setHtmlCode(htmlCode);

            // 提取描述信息
            String description = judgeJson.extractDescription(aiResponse);
            result.setDescription(description);

            // 验证必要字段
            if (result.getHtmlCode() == null || result.getHtmlCode().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析失败：缺少HTML代码");
            }

            log.info("HTML代码解析成功（Markdown格式），HTML长度: {}", result.getHtmlCode().length());

            return result;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析Markdown格式HTML代码失败: " + e.getMessage());
        }
    }


    @Override
    public HtmlCodeResult codeParse(String codeContent, CodeGenTypeEnum codeGenType) throws Exception {
        if (judgeJson.isJsonFormat(codeContent)) {
            return parseHtmlCodeFromJson(codeContent);
        } else {
            // 解析Markdown格式
            return parseHtmlCodeFromMarkdown(codeContent);
        }
    }
}
