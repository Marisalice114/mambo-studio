package com.hachimi.mamboaiplatform.core;


import com.hachimi.mamboaiplatform.ai.AiCodeGeneratorService;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.ai.model.MultiFileCodeResult;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/*(
 * 门面模式
 * AiCodeGeneratorFacade - 用于提供AI代码生成相关的服务接口
 * 通过此Facade可以调用具体的AI代码生成服务
 *
 */
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    public File generateCodeAndSave(String userMessage, CodeGenTypeEnum codeGenType) {
        if (codeGenType == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成类型不能为空");
        }
        return switch(codeGenType) {
            case HTML -> generateCodeAndSaveHtml(userMessage);
            case MULTI_FILE -> generateCodeAndSaveMultiFile(userMessage);
            default -> {
                String errorMessage = String.format("不支持的代码生成类型: %s", codeGenType.getText());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }

        };
    }

    private File generateCodeAndSaveHtml(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlFile(htmlCodeResult);
    }

    private File generateCodeAndSaveMultiFile(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFile(multiFileCodeResult);
    }

}
