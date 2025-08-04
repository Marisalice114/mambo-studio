package com.hachimi.mamboaiplatform.core.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hachimi.mamboaiplatform.ai.model.MultiFileCodeResult;
import com.hachimi.mamboaiplatform.common.JudgeJson;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;

import static com.hachimi.mamboaiplatform.common.JudgeJson.*;
import static com.hachimi.mamboaiplatform.constant.CodePattern.*;

@Slf4j
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    /**
     * 从JSON格式解析多文件代码
     */
    private MultiFileCodeResult parseMultiFileFromJson(String aiResponse) {
        try {
            // 提取JSON内容
            String jsonContent = extractJsonFromResponse(aiResponse);

            ObjectMapper objectMapper = getObjectMapper();
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

    @Override
    public MultiFileCodeResult codeParse(String codeContent, CodeGenTypeEnum codeGenType) throws Exception {
        if (isJsonFormat(codeContent)) {
            return parseMultiFileFromJson(codeContent);
        } else {
            // 解析Markdown格式
            return parseMultiFileFromMarkdown(codeContent);
        }
    }
}