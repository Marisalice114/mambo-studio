package com.hachimi.mamboaiplatform.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用生成状态VO
 * 用于追踪代码生成过程的实时状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppGenerationStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 是否正在生成
     */
    private Boolean isGenerating;

    /**
     * 生成阶段描述
     */
    private String stage;

    /**
     * 进度百分比 (0-100)
     */
    private Integer progress;

    /**
     * 当前生成的消息内容（如果正在生成）
     */
    private String currentContent;

    /**
     * 生成开始时间
     */
    private LocalDateTime startTime;

    /**
     * 估计剩余时间（秒）
     */
    private Long estimatedTimeLeft;

    /**
     * 错误信息（如果生成失败）
     */
    private String errorMessage;

    /**
     * 生成状态
     */
    private GenerationStatus status;

    /**
     * 最后一条用户消息
     */
    private String lastUserMessage;

    public enum GenerationStatus {
        IDLE,           // 空闲状态
        GENERATING,     // 正在生成
        COMPLETED,      // 生成完成
        ERROR,          // 生成错误
        CANCELLED       // 生成被取消
    }
}
