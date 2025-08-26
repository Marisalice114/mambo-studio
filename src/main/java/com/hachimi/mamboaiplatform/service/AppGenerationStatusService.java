package com.hachimi.mamboaiplatform.service;

import com.hachimi.mamboaiplatform.model.vo.AppGenerationStatusVO;

/**
 * 应用生成状态管理服务
 * 用于追踪和管理代码生成过程的状态
 */
public interface AppGenerationStatusService {

    /**
     * 开始生成任务
     * @param appId 应用ID
     * @param userMessage 用户消息
     */
    void startGeneration(Long appId, String userMessage);

    /**
     * 更新生成进度
     * @param appId 应用ID
     * @param stage 当前阶段
     * @param progress 进度百分比
     * @param currentContent 当前生成的内容
     */
    void updateProgress(Long appId, String stage, Integer progress, String currentContent);

    /**
     * 完成生成任务
     * @param appId 应用ID
     * @param finalContent 最终生成的内容
     */
    void completeGeneration(Long appId, String finalContent);

    /**
     * 标记生成失败
     * @param appId 应用ID
     * @param errorMessage 错误信息
     */
    void markGenerationError(Long appId, String errorMessage);

    /**
     * 获取生成状态
     * @param appId 应用ID
     * @return 生成状态信息
     */
    AppGenerationStatusVO getGenerationStatus(Long appId);

    /**
     * 清除生成状态
     * @param appId 应用ID
     */
    void clearGenerationStatus(Long appId);

    /**
     * 检查是否正在生成
     * @param appId 应用ID
     * @return 是否正在生成
     */
    boolean isGenerating(Long appId);

    /**
     * 取消生成任务
     * @param appId 应用ID
     * @return 是否取消成功
     */
    boolean cancelGeneration(Long appId);
}
