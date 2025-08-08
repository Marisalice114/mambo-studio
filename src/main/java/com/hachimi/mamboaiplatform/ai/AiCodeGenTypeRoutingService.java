package com.hachimi.mamboaiplatform.ai;


import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;


public interface AiCodeGenTypeRoutingService {


    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum getCodeGenType(String userMessage);
}
