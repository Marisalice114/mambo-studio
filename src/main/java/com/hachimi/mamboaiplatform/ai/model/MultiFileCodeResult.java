package com.hachimi.mamboaiplatform.ai.model;


import jdk.jfr.Description;
import lombok.Data;

/**
 * 生成的多文件代码的json输出格式
 */
@Data
@Description("生成多文件代码文件结果")
public class MultiFileCodeResult {

    /**
     * HTML 代码
     */
    private String htmlCode;

    /**
     * css 代码
     */
    private String cssCode;

    /**
     * js 代码
     */
    private String jsCode;

    /**
     * 代码的描述信息
     */
    private String description;
}
