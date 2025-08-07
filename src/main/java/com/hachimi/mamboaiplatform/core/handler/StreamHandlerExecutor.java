package com.hachimi.mamboaiplatform.core.handler;

import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import com.hachimi.mamboaiplatform.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
public class StreamHandlerExecutor {

    @Resource
    private SimpleTextStreamHandler simpleTextStreamHandler;
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;


    /**
     * 执行代码流处理
     *
     * @param originFlux     代码内容流
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */

    public Flux<String> doExecutor(Flux<String> originFlux, CodeGenTypeEnum codeGenTypeEnum,
                                    ChatHistoryService chatHistoryService,
                                    long appId, User loginUser) {
        return switch (codeGenTypeEnum) {
            case HTML,MULTI_FILE -> simpleTextStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case VUE -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
