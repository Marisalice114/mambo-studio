package com.hachimi.mamboaiplatform.core;

import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateCodeAndSave("帮我生成一个任务记录网站，使用不超过30行代码完成", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }
}
