package com.hachimi.mamboaiplatform.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求包装
 */
@Data
public class BatchDeleteRequest implements Serializable {

    /**
     * id 列表
     */
    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}