package com.hachimi.mamboaiplatform.scheduler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class CleanTmpSchedulerTest {

    @Resource
    private CleanTmpScheduler cleanTmpScheduler;

    /**
     * 测试手动清理功能
     */
    @Test
    void testManualCleanup() {
        log.info("开始测试手动清理功能");

        // 先查看清理前的统计信息
        String statsBefore = cleanTmpScheduler.getTmpDirectoryStats();
        log.info("清理前统计信息:\n{}", statsBefore);

        // 执行清理
        cleanTmpScheduler.manualCleanup();

        // 查看清理后的统计信息
        String statsAfter = cleanTmpScheduler.getTmpDirectoryStats();
        log.info("清理后统计信息:\n{}", statsAfter);
    }

    /**
     * 测试获取目录统计信息
     */
    @Test
    void testGetTmpDirectoryStats() {
        String stats = cleanTmpScheduler.getTmpDirectoryStats();
        log.info("当前tmp目录统计信息:\n{}", stats);

        // 验证统计信息不为空
        assert stats != null && !stats.trim().isEmpty() : "统计信息不应为空";
    }

    /**
     * 测试清理截图文件功能
     * 注意：这个测试会实际删除过期文件，请谨慎使用
     */
    @Test
    void testCleanTmpFiles() {
        log.info("开始测试清理临时文件功能");

        // 获取清理前的统计
        String statsBefore = cleanTmpScheduler.getTmpDirectoryStats();
        log.info("清理前:\n{}", statsBefore);

        // 执行清理任务
        cleanTmpScheduler.cleanTmpFiles();

        // 获取清理后的统计
        String statsAfter = cleanTmpScheduler.getTmpDirectoryStats();
        log.info("清理后:\n{}", statsAfter);
    }
}