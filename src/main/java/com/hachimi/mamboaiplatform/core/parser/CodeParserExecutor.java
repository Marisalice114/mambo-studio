package com.hachimi.mamboaiplatform.core.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CodeParserExecutor {

    private final static HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private final static MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        if (codeContent == null || codeContent.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码内容不能为空");
        }
        try {

            return switch (codeGenType) {
                case HTML -> htmlCodeParser.codeParse(codeContent, codeGenType);
                case MULTI_FILE -> multiFileCodeParser.codeParse(codeContent, codeGenType);
                default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
            };
        } catch (Exception e) {
            String errorMessage = String.format("解析%s代码失败: %s", codeGenType, e.getMessage());
            log.error("解析{}代码失败: {}", codeGenType,e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
        }
    }

}
