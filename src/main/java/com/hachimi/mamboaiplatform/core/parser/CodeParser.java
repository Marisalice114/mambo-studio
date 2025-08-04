package com.hachimi.mamboaiplatform.core.parser;

import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;

/**
 * 解析接口，使用策略模式
 */
public interface CodeParser<T> {


    /**
     * 解析代码
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型
     * @return 解析后的结果
     * @throws Exception 解析异常
     */
    T codeParse(String codeContent, CodeGenTypeEnum codeGenType) throws Exception;
}
