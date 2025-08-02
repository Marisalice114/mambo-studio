package com.hachimi.mamboaiplatform.ai.model;


import jdk.jfr.Description;
import lombok.Data;

/**
 * 生成的 HTML 代码的json输出格式
 */

@Data
@Description("生成html代码文件结果")
public class HtmlCodeResult {

    /**
     * HTML 代码
     */
    private String htmlCode;

    /**
     * 代码的描述信息
     */
    private String description;

}
