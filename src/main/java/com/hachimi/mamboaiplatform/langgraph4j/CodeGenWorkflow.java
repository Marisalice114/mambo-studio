package com.hachimi.mamboaiplatform.langgraph4j;

import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.langgraph4j.model.QualityResult;
import com.hachimi.mamboaiplatform.langgraph4j.node.*;
import com.hachimi.mamboaiplatform.langgraph4j.state.WorkflowContext;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 代码生成工作流
 */
@Slf4j
public class CodeGenWorkflow {

  /**
   * 创建完整的工作流
   */
  public CompiledGraph<MessagesState<String>> createWorkflow() {
    try {
      return new MessagesStateGraph<String>()
          // 添加节点 - 使用完整实现的节点
          .addNode("image_collector", ImageCollectorNode.create())
          .addNode("prompt_enhancer", PromptEnhancerNode.create())
          .addNode("router", RouterNode.create())
          .addNode("code_generator", CodeGeneratorNode.create())
          .addNode("code_quality_check", CodeQualityCheckNode.create())
          .addNode("project_builder", ProjectBuilderNode.create())

          // 添加边
          .addEdge(START, "image_collector")
          .addEdge("image_collector", "prompt_enhancer")
          .addEdge("prompt_enhancer", "router")
          .addEdge("router", "code_generator")
          .addEdge("code_generator", "code_quality_check")

          // 使用条件边 - 从 code_quality_check 节点开始判断
          .addConditionalEdges("code_quality_check", AsyncEdgeAction.edge_async(this::routeAfterQualityCheck),
              Map.of("build", "project_builder", "skip_build", END,
                  "fail", "code_generator"))
          .addEdge("project_builder", END)

          // 编译工作流
          .compile();
    } catch (GraphStateException e) {
      log.error("工作流编译失败: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败: " + e.getMessage());
    }
  }

  private String routeBuildOrSkip(MessagesState<String> state) {
    WorkflowContext context = WorkflowContext.getContext(state);
    CodeGenTypeEnum generationType = context.getGenerationType();
    // HTML 和 MULTI_FILE 类型不需要构建，直接结束
    if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
      return "skip_build";
    }
    // VUE_PROJECT 需要构建
    return "build";
  }

  private String routeAfterQualityCheck(MessagesState<String> state) {
    WorkflowContext context = WorkflowContext.getContext(state);
    QualityResult qualityResult = context.getQualityResult();

    // 如果质检失败
    if (qualityResult == null || !qualityResult.getIsValid()) {
      // 检查重试次数是否超过3次
      if (context.getRetryCount() >= 3) {
        log.error("代码质检失败，已达到最大重试次数(3次)，停止重试");
        // 设置错误信息并结束流程
        context.setErrorMessage("代码生成失败: 质量检查连续失败，已达到最大重试次数(3次)");
        return END;
      }

      // 增加重试次数
      context.setRetryCount(context.getRetryCount() + 1);
      log.warn("代码质检失败，准备第 {} 次重试", context.getRetryCount());
      return "fail";
    }

    // 质检通过，重置重试次数并继续后续流程
    context.setRetryCount(0);
    log.info("代码质检通过，继续后续流程");
    return routeBuildOrSkip(state);
  }

  /**
   * 执行工作流
   */
  public WorkflowContext executeWorkflow(String originalPrompt) {
    CompiledGraph<MessagesState<String>> workflow = createWorkflow();

    // 初始化 WorkflowContext
    WorkflowContext initialContext = WorkflowContext.builder()
        .originalPrompt(originalPrompt)
        .currentStep("初始化")
        .build();

    GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
    log.info("工作流图:\n{}", graph.content());
    log.info("开始执行代码生成工作流");

    WorkflowContext finalContext = null;
    int stepCounter = 1;
    for (NodeOutput<MessagesState<String>> step : workflow.stream(
        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
      log.info("--- 第 {} 步完成 ---", stepCounter);
      // 显示当前状态
      WorkflowContext currentContext = WorkflowContext.getContext(step.state());
      if (currentContext != null) {
        finalContext = currentContext;
        log.info("当前步骤上下文: {}", currentContext);
      }
      stepCounter++;
    }
    log.info("代码生成工作流执行完成！");
    return finalContext;
  }
}
