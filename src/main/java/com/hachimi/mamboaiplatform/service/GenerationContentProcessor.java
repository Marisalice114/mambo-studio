package com.hachimi.mamboaiplatform.service;

/**
 * 生成内容处理服务
 * 负责处理和格式化AI生成的内容
 */
public interface GenerationContentProcessor {
    
    /**
     * 处理工具调用流数据，提取实际内容
     * 
     * @param chunk 原始流数据块
     * @return 处理后的内容，如果不是有效内容则返回null
     */
    String processStreamChunk(String chunk);
    
    /**
     * 判断生成是否完成
     * 
     * @param chunk 数据块
     * @return 是否是完成信号
     */
    boolean isGenerationComplete(String chunk);
    
    /**
     * 提取最终生成的完整内容
     * 
     * @param accumulatedContent 累积的所有内容
     * @return 最终处理的内容
     */
    String extractFinalContent(String accumulatedContent);
}
