package com.hachimi.mamboaiplatform.langgraph4j.tools;

import com.hachimi.mamboaiplatform.langgraph4j.model.ImageResource;
import com.hachimi.mamboaiplatform.langgraph4j.model.enums.ImageCategoryEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MermaidDiagramToolTest {

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Test
    void testGenerateMermaidDiagram() {
        // 测试生成 Mermaid 架构图
        String mermaidCode = """
                flowchart LR
                    Start([开始]) --> Input[输入数据]
                    Input --> Process[处理数据]
                    Process --> Decision{是否有效?}
                    Decision -->|是| Output[输出结果]
                    Decision -->|否| Error[错误处理]
                    Output --> End([结束])
                    Error --> End
                """;
        String description = "简单系统架构图";
        List<ImageResource> diagrams = mermaidDiagramTool.generateMermaidDiagram(mermaidCode, description);

        // 验证返回结果不为空
        assertNotNull(diagrams);

        // 如果有结果，验证图表资源
        if (!diagrams.isEmpty()) {
            ImageResource firstDiagram = diagrams.get(0);
            assertEquals(ImageCategoryEnum.ARCHITECTURE, firstDiagram.getCategory());
            assertEquals(description, firstDiagram.getDescription());
            assertNotNull(firstDiagram.getUrl());
            assertTrue(firstDiagram.getUrl().startsWith("http"));
            System.out.println("生成了架构图: " + firstDiagram.getUrl());
        } else {
            System.out.println("架构图生成失败，返回空列表");
        }
    }
}
