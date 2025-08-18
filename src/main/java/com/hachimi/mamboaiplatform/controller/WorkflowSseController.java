package com.hachimi.mamboaiplatform.controller;


import com.hachimi.mamboaiplatform.langgraph4j.CodeGenWorkflow;
import com.hachimi.mamboaiplatform.langgraph4j.state.WorkflowContext;
import com.hachimi.mamboaiplatform.ratelimit.annotation.RateLimit;
import com.hachimi.mamboaiplatform.ratelimit.enums.RateLimitType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * 工作流 SSE 控制器
 * 演示 LangGraph4j 工作流的流式输出功能
 */
@RestController
@RequestMapping("/workflow")
@Slf4j
public class WorkflowSseController {

    /**
     * 同步执行工作流
     */
    @PostMapping("/execute")
    @RateLimit(
        limitType = RateLimitType.USER,
        rate = 3,
        vipRate = 10,
        rateInterval = 300,
        enableVipDifferentiation = true,
        message = "工作流执行过于频繁，5分钟内最多执行3次。升级VIP可享有更高限额",
        vipMessage = "VIP用户工作流执行过于频繁，5分钟内最多执行10次"
    )
    public WorkflowContext executeWorkflow(@RequestParam String prompt) {
        log.info("收到同步工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflow(prompt);
    }

    /**
     * Flux 流式执行工作流
     */
    @GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(
        limitType = RateLimitType.USER,
        rate = 3,
        vipRate = 10,
        rateInterval = 300,
        enableVipDifferentiation = true,
        message = "流式工作流执行过于频繁，5分钟内最多执行3次。升级VIP可享有更高限额",
        vipMessage = "VIP用户流式工作流执行过于频繁，5分钟内最多执行10次"
    )
    public Flux<String> executeWorkflowWithFlux(@RequestParam String prompt) {
        log.info("收到 Flux 工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflowWithFlux(prompt);
    }

    /**
     * SSE 流式执行工作流
     */
    @GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(
        limitType = RateLimitType.USER,
        rate = 3,
        vipRate = 10,
        rateInterval = 300,
        enableVipDifferentiation = true,
        message = "SSE工作流执行过于频繁，5分钟内最多执行3次。升级VIP可享有更高限额",
        vipMessage = "VIP用户SSE工作流执行过于频繁，5分钟内最多执行10次"
    )
    public SseEmitter executeWorkflowWithSse(@RequestParam String prompt) {
        log.info("收到 SSE 工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflowWithSse(prompt);
    }
}