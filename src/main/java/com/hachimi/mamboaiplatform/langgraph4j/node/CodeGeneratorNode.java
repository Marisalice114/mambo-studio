package com.hachimi.mamboaiplatform.langgraph4j.node;

import com.hachimi.mamboaiplatform.constant.AppConstant;
import com.hachimi.mamboaiplatform.core.AiCodeGeneratorFacade;
import com.hachimi.mamboaiplatform.langgraph4j.model.QualityResult;
import com.hachimi.mamboaiplatform.langgraph4j.state.WorkflowContext;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import com.hachimi.mamboaiplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 网站代码生成节点
 */
@Slf4j
public class CodeGeneratorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");
            // 构造用户消息（包含原始提示词和可能的错误修复信息）
            String userMessage = buildUserMessage(context);
            CodeGenTypeEnum generationType = context.getGenerationType();
            // 获取 AI 代码生成外观服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {} ({})", generationType.getValue(), generationType.getText());

            // 使用时间戳作为 appId，避免对话历史污染
            Long appId = System.currentTimeMillis();

            // 添加重试机制，处理网络连接问题
            int maxRetries = 3;
            int retryCount = 0;
            Exception lastException = null;

            while (retryCount < maxRetries) {
                try {
                    log.info("尝试生成代码，第{}次", retryCount + 1);

                    // 如果是重试，使用新的appId避免对话历史问题
                    if (retryCount > 0) {
                        appId = System.currentTimeMillis() + retryCount;
                        log.info("重试使用新的appId: {}", appId);
                    }

                    // 调用流式代码生成
                    Flux<String> codeStream = codeGeneratorFacade.generateCodeAndSaveStream(userMessage, generationType, appId);
                    // 同步等待流式输出完成，增加超时处理
                    codeStream.blockLast(Duration.ofMinutes(20)); // 增加到20分钟

                    // 根据类型设置生成目录
                    String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
                    log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);

                    // 更新状态
                    context.setCurrentStep("代码生成");
                    context.setGeneratedCodeDir(generatedCodeDir);
                    return WorkflowContext.saveContext(context);

                } catch (Exception e) {
                    lastException = e;
                    retryCount++;
                    log.error("代码生成失败，第{}次尝试: {}", retryCount, e.getMessage());

                    // 判断是否为可重试的错误
                    if (isRetryableError(e) && retryCount < maxRetries) {
                        long delaySeconds = Math.min(5 * retryCount, 30); // 指数退避，最大30秒
                        log.info("将在{}秒后进行第{}次重试", delaySeconds, retryCount + 1);
                        try {
                            Thread.sleep(delaySeconds * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }

            // 所有重试都失败了
            log.error("代码生成最终失败，已重试{}次", retryCount);

            // 设置错误信息
            String errorMessage = getErrorMessage(lastException);
            context.setErrorMessage(errorMessage);
            context.setCurrentStep("代码生成失败");
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 构造用户消息，如果存在质检失败结果则添加错误修复信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            // 直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }

    /**
     * 判断是否为可重试的错误
     */
    private static boolean isRetryableError(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;

        // 网络连接相关错误
        if (message.contains("ConnectionClosedException") ||
            message.contains("Premature end of chunk") ||
            message.contains("Connection reset") ||
            message.contains("timeout") ||
            message.contains("Connection refused")) {
          return true;
        }

        // HTTP相关错误
        if (message.contains("500") ||
            message.contains("502") ||
            message.contains("503") ||
            message.contains("504")) {
          return true;
        }

        // 流式传输错误
        if (message.contains("SSE") ||
            message.contains("ServerSentEvent") ||
            message.contains("chunk coded message")) {
          return true;
        }

        return false;
    }

    /**
     * 根据异常类型生成友好的错误信息
     */
    private static String getErrorMessage(Exception e) {
        if (e == null) return "代码生成失败：未知错误";

        String message = e.getMessage();
        if (message == null) message = e.getClass().getSimpleName();

        // 连接错误
        if (message.contains("ConnectionClosedException") || message.contains("Premature end of chunk")) {
            return "网络连接中断，请检查网络状态后重试";
        }

        // 工具调用协议错误
        if (message.contains("tool_calls")) {
            return "AI工具调用协议错误，系统已自动重置对话状态";
        }

        // JSON解析错误
        if (e.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
            return "AI模型返回的JSON格式错误，请重试";
        }

        // 超时错误
        if (message.contains("timeout")) {
            return "请求超时，可能是网络不稳定，请重试";
        }

        // 默认错误信息
        return "代码生成失败: " + (message.length() > 100 ? message.substring(0, 100) + "..." : message);
    }
}