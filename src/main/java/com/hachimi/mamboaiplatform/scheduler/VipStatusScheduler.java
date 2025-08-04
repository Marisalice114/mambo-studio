package com.hachimi.mamboaiplatform.scheduler;

import com.hachimi.mamboaiplatform.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * VIP状态定时任务调度器
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Component
@Slf4j
public class VipStatusScheduler {

    @Resource
    private UserService userService;

    /**
     * 每天凌晨1点执行VIP状态批量更新任务
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateVipStatus() {
        log.info("=== 开始执行VIP状态定时更新任务 ===");

        try {
            int updatedCount = userService.batchUpdateExpiredVipStatus();
            log.info("=== VIP状态定时更新任务完成，共更新 {} 个用户 ===", updatedCount);
        } catch (Exception e) {
            log.error("=== VIP状态定时更新任务执行失败 ===", e);
        }
    }

    /**
     * 测试用：每5分钟执行一次（仅用于开发测试）
     * 生产环境请注释掉此方法
     */
    // @Scheduled(cron = "0 */5 * * * ?")
    public void updateVipStatusForTest() {
        log.info("=== 测试：开始执行VIP状态更新任务 ===");

        try {
            int updatedCount = userService.batchUpdateExpiredVipStatus();
            log.info("=== 测试：VIP状态更新任务完成，共更新 {} 个用户 ===", updatedCount);
        } catch (Exception e) {
            log.error("=== 测试：VIP状态更新任务执行失败 ===", e);
        }
    }
}
