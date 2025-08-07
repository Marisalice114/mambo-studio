package com.hachimi.mamboaiplatform.core.parser;

import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;


/**
 * 代码解析器策略接口
 *
 * @author Marsalice114
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
