package com.hachimi.mamboaiplatform.core;

import cn.hutool.json.JSONUtil;
import com.hachimi.mamboaiplatform.ai.AiCodeGeneratorService;
import com.hachimi.mamboaiplatform.ai.AiCodeGeneratorServiceFactory;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.ai.model.MultiFileCodeResult;
import com.hachimi.mamboaiplatform.ai.model.message.AiResponseMessage;
import com.hachimi.mamboaiplatform.ai.model.message.ToolExecutedMessage;
import com.hachimi.mamboaiplatform.ai.model.message.ToolRequestMessage;
import com.hachimi.mamboaiplatform.core.builder.VueProjectBuilder;
import com.hachimi.mamboaiplatform.core.parser.CodeParserExecutor;
import com.hachimi.mamboaiplatform.core.saver.CodeFileSaverExecutor;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hachimi.mamboaiplatform.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

@Slf4j
@Service
public class AiCodeGeneratorFacade {

  @Resource
  private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

  @Resource
  private VueProjectBuilder vueProjectBuilder;

  /**
   * 统一入口：根据类型生成并保存代码
   *
   * @param userMessage     用户提示词
   * @param codeGenTypeEnum 生成类型
   * @param appId           应用ID
   * @return 保存的目录
   */
  public File generateCodeAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
    if (codeGenTypeEnum == null) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
    }
    // 通过工厂来获得aiservice
    // AiCodeGeneratorService aiCodeGeneratorService =
    // aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
    AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
        codeGenTypeEnum);
    return switch (codeGenTypeEnum) {
      case HTML -> {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
      }
      case MULTI_FILE -> {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
      }
      default -> {
        String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
      }
    };
  }

  /**
   * 统一入口：根据类型生成并保存代码（流式）
   *
   * @param userMessage     用户提示词
   * @param codeGenTypeEnum 生成类型
   */
  public Flux<String> generateCodeAndSaveStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
    if (codeGenTypeEnum == null) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
    }
    // 通过工厂来获得aiservice
    // AiCodeGeneratorService aiCodeGeneratorService =
    // aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
    AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
        codeGenTypeEnum);

    return switch (codeGenTypeEnum) {
      case HTML -> {
        Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
      }
      case MULTI_FILE -> {
        Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
      }
      case VUE_PROJECT -> {
        TokenStream codeStream = aiCodeGeneratorService.generateVueCodeStream(appId, userMessage);
        yield processTokenStream(codeStream, appId);
      }
      default -> {
        String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
      }
    };
  }

  /**
   * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息 适配器模式
   *
   * @param tokenStream TokenStream 对象
   * @param appId       应用ID
   * @return Flux<String> 流式响应
   */
  private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
    return Flux.create(sink -> {
      // 工具调用计数器，用于监控（不设限制）
      final AtomicInteger toolCallCount = new AtomicInteger(0);

      tokenStream.onPartialResponse((String partialResponse) -> {
        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
        log.debug("AI 部分响应: {}",
            partialResponse.length() > 100 ? partialResponse.substring(0, 100) + "..." : partialResponse);
      })
          .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
            log.debug("工具调用: {} (参数长度: {})", toolExecutionRequest.name(),
                toolExecutionRequest.arguments().length());

            ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
            sink.next(JSONUtil.toJsonStr(toolRequestMessage));
          })
          .onToolExecuted((ToolExecution toolExecution) -> {
            int currentCount = toolCallCount.incrementAndGet();
            log.info("工具执行完成 #{}: {} -> {}", currentCount, toolExecution.request().name(),
                toolExecution.result().length() > 100 ? toolExecution.result().substring(0, 100) + "..."
                    : toolExecution.result());
            ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
            sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
          })
          .onCompleteResponse((ChatResponse response) -> {
            log.info("AI 响应完成，总工具调用次数: {}", toolCallCount.get());
            // 同步执行vue项目，确保预览时项目已经就绪
            String pathName = CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
            vueProjectBuilder.buildVueProject(pathName);
            sink.complete();
          })
          .onError((Throwable error) -> {
            log.error("TokenStream 处理错误，工具调用次数: {}", toolCallCount.get(), error);
            sink.error(error);
          })
          .start();
    });
  }

  /**
   * 通用流式代码处理方法
   *
   * @param codeStream  代码流
   * @param codeGenType 代码生成类型
   * @param appId       应用ID
   * @return 流式响应
   */
  private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
    StringBuilder codeBuilder = new StringBuilder();
    return codeStream.doOnNext(chunk -> {
      // 实时收集代码片段
      codeBuilder.append(chunk);
    }).doOnComplete(() -> {
      // 流式返回完成后保存代码
      try {
        String completeCode = codeBuilder.toString();
        // 使用执行器解析代码
        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
        // 使用执行器保存代码
        File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
        log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
      } catch (Exception e) {
        log.error("保存失败3: {}", e.getMessage());
      }
    });
  }

}
