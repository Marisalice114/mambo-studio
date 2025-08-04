package com.hachimi.mamboaiplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    /**
     * 是否为VIP专属应用（null-不限制，true-是，false-否）
     */
    private Boolean isVipOnly;

    private static final long serialVersionUID = 1L;
}
