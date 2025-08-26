package com.hachimi.mamboaiplatform.service.impl;

import com.hachimi.mamboaiplatform.model.vo.AppGenerationStatusVO;
import com.hachimi.mamboaiplatform.service.AppGenerationStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 应用生成状态管理服务实现
 * 使用Redis存储生成状态，支持多实例部署
 */
@Service
@Slf4j
public class AppGenerationStatusServiceImpl implements AppGenerationStatusService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String GENERATION_STATUS_KEY_PREFIX = "app:generation:status:";
    private static final String GENERATION_CONTENT_KEY_PREFIX = "app:generation:content:";
    private static final long STATUS_TTL_MINUTES = 30; // 状态过期时间30分钟

    /**
     * 构建Redis键名
     */
    private String getStatusKey(Long appId) {
        return GENERATION_STATUS_KEY_PREFIX + appId;
    }

    private String getContentKey(Long appId) {
        return GENERATION_CONTENT_KEY_PREFIX + appId;
    }

    @Override
    public void startGeneration(Long appId, String userMessage) {
        AppGenerationStatusVO status = AppGenerationStatusVO.builder()
                .appId(appId)
                .isGenerating(true)
                .stage("正在准备生成...")
                .progress(0)
                .currentContent("")
                .startTime(LocalDateTime.now())
                .status(AppGenerationStatusVO.GenerationStatus.GENERATING)
                .lastUserMessage(userMessage)
                .build();

        String statusKey = getStatusKey(appId);
        redisTemplate.opsForValue().set(statusKey, status, STATUS_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("开始生成任务，应用ID: {}, 用户消息: {}", appId, userMessage);
    }

    @Override
    public void updateProgress(Long appId, String stage, Integer progress, String currentContent) {
        String statusKey = getStatusKey(appId);
        AppGenerationStatusVO status = (AppGenerationStatusVO) redisTemplate.opsForValue().get(statusKey);

        if (status != null && status.getIsGenerating()) {
            status.setStage(stage);
            status.setProgress(progress);
            status.setCurrentContent(currentContent);

            // 估算剩余时间
            if (progress > 0) {
                LocalDateTime startTime = status.getStartTime();
                long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
                long estimatedTotal = (elapsedSeconds * 100) / progress;
                status.setEstimatedTimeLeft(Math.max(0, estimatedTotal - elapsedSeconds));
            }

            redisTemplate.opsForValue().set(statusKey, status, STATUS_TTL_MINUTES, TimeUnit.MINUTES);

            // 同时缓存生成内容，用于快速恢复
            if (currentContent != null && !currentContent.isEmpty()) {
                String contentKey = getContentKey(appId);
                redisTemplate.opsForValue().set(contentKey, currentContent, STATUS_TTL_MINUTES, TimeUnit.MINUTES);
            }

            log.debug("更新生成进度，应用ID: {}, 阶段: {}, 进度: {}%", appId, stage, progress);
        }
    }

    @Override
    public void completeGeneration(Long appId, String finalContent) {
        String statusKey = getStatusKey(appId);
        AppGenerationStatusVO status = (AppGenerationStatusVO) redisTemplate.opsForValue().get(statusKey);

        if (status != null) {
            status.setIsGenerating(false);
            status.setStage("生成完成");
            status.setProgress(100);
            status.setCurrentContent(finalContent);
            status.setStatus(AppGenerationStatusVO.GenerationStatus.COMPLETED);
            status.setEstimatedTimeLeft(0L);

            redisTemplate.opsForValue().set(statusKey, status, 5, TimeUnit.MINUTES); // 完成状态保留5分钟

            // 清理内容缓存
            String contentKey = getContentKey(appId);
            redisTemplate.delete(contentKey);

            log.info("生成任务完成，应用ID: {}", appId);
        }
    }

    @Override
    public void markGenerationError(Long appId, String errorMessage) {
        String statusKey = getStatusKey(appId);
        AppGenerationStatusVO status = (AppGenerationStatusVO) redisTemplate.opsForValue().get(statusKey);

        if (status != null) {
            status.setIsGenerating(false);
            status.setStage("生成失败");
            status.setProgress(0);
            status.setStatus(AppGenerationStatusVO.GenerationStatus.ERROR);
            status.setErrorMessage(errorMessage);
            status.setEstimatedTimeLeft(0L);

            redisTemplate.opsForValue().set(statusKey, status, 10, TimeUnit.MINUTES); // 错误状态保留10分钟

            log.error("生成任务失败，应用ID: {}, 错误: {}", appId, errorMessage);
        }
    }

    @Override
    public AppGenerationStatusVO getGenerationStatus(Long appId) {
        String statusKey = getStatusKey(appId);
        AppGenerationStatusVO status = (AppGenerationStatusVO) redisTemplate.opsForValue().get(statusKey);

        if (status == null) {
            // 返回默认的空闲状态
            return AppGenerationStatusVO.builder()
                    .appId(appId)
                    .isGenerating(false)
                    .stage("空闲")
                    .progress(0)
                    .status(AppGenerationStatusVO.GenerationStatus.IDLE)
                    .build();
        }

        // 如果状态显示正在生成，但时间过长，可能是异常中断
        if (status.getIsGenerating() && status.getStartTime() != null) {
            long elapsedMinutes = java.time.Duration.between(status.getStartTime(), LocalDateTime.now()).toMinutes();
            if (elapsedMinutes > 15) { // 超过15分钟认为是异常中断
                log.warn("检测到异常中断的生成任务，应用ID: {}, 开始时间: {}", appId, status.getStartTime());
                markGenerationError(appId, "生成任务异常中断，请重试");
                status.setStatus(AppGenerationStatusVO.GenerationStatus.ERROR);
                status.setIsGenerating(false);
                status.setErrorMessage("生成任务异常中断，请重试");
            }
        }

        return status;
    }

    @Override
    public void clearGenerationStatus(Long appId) {
        String statusKey = getStatusKey(appId);
        String contentKey = getContentKey(appId);

        redisTemplate.delete(statusKey);
        redisTemplate.delete(contentKey);

        log.info("清除生成状态，应用ID: {}", appId);
    }

    @Override
    public boolean isGenerating(Long appId) {
        AppGenerationStatusVO status = getGenerationStatus(appId);
        return status.getIsGenerating() != null && status.getIsGenerating();
    }

    @Override
    public boolean cancelGeneration(Long appId) {
        String statusKey = getStatusKey(appId);
        AppGenerationStatusVO status = (AppGenerationStatusVO) redisTemplate.opsForValue().get(statusKey);

        if (status != null && status.getIsGenerating()) {
            // 更新状态为已取消
            status.setIsGenerating(false);
            status.setStatus(AppGenerationStatusVO.GenerationStatus.CANCELLED);
            status.setStage("生成已取消");
            status.setProgress(0);

            // 保存到Redis
            redisTemplate.opsForValue().set(statusKey, status, STATUS_TTL_MINUTES, TimeUnit.MINUTES);

            log.info("生成任务已取消，应用ID: {}", appId);
            return true;
        }

        log.warn("无法取消生成任务，应用ID: {} 当前不在生成状态", appId);
        return false;
    }
}
