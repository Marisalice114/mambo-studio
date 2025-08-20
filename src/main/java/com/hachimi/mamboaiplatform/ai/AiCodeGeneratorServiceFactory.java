package com.hachimi.mamboaiplatform.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hachimi.mamboaiplatform.ai.guardrail.PromptSafetyInputGuardrail;
import com.hachimi.mamboaiplatform.ai.guardrail.RetryOutputGuardrail;
import com.hachimi.mamboaiplatform.ai.tools.*;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import com.hachimi.mamboaiplatform.monitor.MonitorContext;
import com.hachimi.mamboaiplatform.monitor.MonitorContextHolder;
import com.hachimi.mamboaiplatform.service.ChatHistoryService;
import com.hachimi.mamboaiplatform.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 创建工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

  @Resource(name = "openAiChatModel")
  private ChatModel chatModel;

  @Resource
  private RedisChatMemoryStore redisChatMemoryStore;

  @Resource
  private ChatHistoryService chatHistoryService;

  @Resource
  private ToolManager toolManager;

  // @Bean
  // public AiCodeGeneratorService aiCodeGeneratorService() {
  // return AiServices.create(AiCodeGeneratorService.class, chatModel);
  // }

  /**
   * AI 服务实例缓存
   * 缓存策略：
   * - 最大缓存 1000 个实例
   * - 写入后 30 分钟过期
   * - 访问后 10 分钟过期
   */
  private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofMinutes(30))
      .expireAfterAccess(Duration.ofMinutes(10))
      .removalListener((key, value, cause) -> {
        log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
      })
      .build();

  /**
   * 根据 appId 获取服务（带缓存）
   */
  public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
    // 如果appId没有获取到aiservice，调用createAiCodeGeneratorService快速生成一个aiservice
    return createAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
  }

  /**
   * 根据 appId 获取服务（带缓存）支持传入代码生成类型
   */
  public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // 如果appId没有获取到aiservice，调用createAiCodeGeneratorService快速生成一个aiservice
    String cacheKey = buildCacheKey(appId, codeGenType);
    return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
  }

  /**
   * 为每个应用单独创建一个aiservice
   * 
   * @param appId
   * @param codeGenType 生成类型
   * @return
   */
  private AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
    log.info("创建新的 AiCodeGeneratorService 实例，appId: {}", appId);

    // 确保在创建AI服务时设置监控上下文
    ensureMonitorContextForService(appId);

    // 根据appId构建独立的对话记忆
    MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
        .id(appId)
        .chatMemoryStore(redisChatMemoryStore)
        .maxMessages(20)
        .build();
    // 从数据库中加载会话历史到记忆中
    chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
    return switch (codeGenType) {
      // 普通生成
      case HTML, MULTI_FILE -> {
        // 拿到指定bean
        StreamingChatModel streamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype",
            StreamingChatModel.class);
        yield AiServices.builder(AiCodeGeneratorService.class)
            .streamingChatModel(streamingChatModel)
            .chatModel(chatModel)
            .chatMemory(chatMemory)
            .inputGuardrails(new PromptSafetyInputGuardrail()) // 输入护轨
            // .outputGuardrails(new RetryOutputGuardrail()) //输出护轨 但是注意，流式输出最好不要用输出护轨
            // 经过测试，如果用了输出护轨，可能会导致流式输出的响应不及时，
            // 等到AI输出结束才一起返回，所以如果为了追求流式输出效果，建议不要通过护轨的方式进行重试。
            .build();
      }

      // vue生成
      case VUE_PROJECT -> {
        // 使用多例模式的streamingchatmodel来解决并发问题
        StreamingChatModel reasoningStreamingChatModel = SpringContextUtil
            .getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
        yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(reasoningStreamingChatModel)
            // 必须为每个memoryId绑定对话记忆
            .chatMemoryProvider(memoryId -> chatMemory)
            .tools(
                (Object[]) toolManager.getAllTools())
            // 处理调用不存在的工具
            .maxSequentialToolsInvocations(20)
            .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest,
                "Error: there is no tool named '" + toolExecutionRequest.name()))
            .inputGuardrails(new PromptSafetyInputGuardrail())
            // .outputGuardrails(new RetryOutputGuardrail())
            .build();
      }

      default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.getValue());
    };
  }

  /**
   * 创建 AiCodeGeneratorService 的 Bean
   * 使用 AiServices 工厂方法创建服务实例 分别创建流式调用和非流式调用
   * 老方法
   * 
   * @return AiCodeGeneratorService 实例
   */
  // @Bean
  public AiCodeGeneratorService aiCodeGeneratorService() {
    return getAiCodeGeneratorService(0L);
  }

  /**
   * 构造缓存键(从一个参数变成了两个参数)
   * 
   * @param appId
   * @param codeGenType
   * @return
   */
  public String buildCacheKey(Long appId, CodeGenTypeEnum codeGenType) {
    return appId + "_" + codeGenType.getValue();
  }

  /**
   * 确保监控上下文存在，专用于AI服务创建
   *
   * @param appId 应用ID
   */
  private void ensureMonitorContextForService(Long appId) {
    MonitorContext existingContext = MonitorContextHolder.getContext();
    if (existingContext == null) {
      log.info("AI服务创建时MonitorContext为空，创建默认上下文 appId: {} on thread: {}",
              appId, Thread.currentThread().getName());
      MonitorContext defaultContext = MonitorContext.builder()
          .userId("system") // AI服务创建时使用系统标识
          .appId(appId.toString())
          .build();
      MonitorContextHolder.setContext(defaultContext);

      // 验证设置是否成功
      MonitorContext verifyContext = MonitorContextHolder.getContext();
      if (verifyContext != null) {
        log.info("AI服务MonitorContext设置成功: userId={}, appId={}",
                verifyContext.getUserId(), verifyContext.getAppId());
      } else {
        log.error("AI服务MonitorContext设置失败，仍为null");
      }
    } else {
      log.debug("AI服务创建时MonitorContext已存在: userId={}, appId={}",
              existingContext.getUserId(), existingContext.getAppId());
    }
  }
}
