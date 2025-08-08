package com.hachimi.mamboaiplatform.scheduler;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 临时文件清理定时任务
 * 定期清理tmp目录下的临时文件
 *
 * @author hachimi
 */
@Slf4j
@Component
public class CleanTmpScheduler {

    /**
     * 截图文件保留时间（小时）
     * 默认保留24小时
     */
    private static final int SCREENSHOT_RETENTION_HOURS = 24;

    /**
     * 代码输出文件保留时间（小时）
     * 默认保留72小时（3天）
     */
    private static final int CODE_OUTPUT_RETENTION_HOURS = 72;

    /**
     * tmp目录根路径
     */
    private static final String TMP_ROOT_DIR = System.getProperty("user.dir") + File.separator + "tmp";

    /**
     * 每天凌晨2点执行清理任务
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanTmpFiles() {
        log.info("开始执行临时文件清理任务...");

        try {
            // 清理截图文件
            cleanScreenshots();

            // 清理过期的代码输出文件（可选）
            //cleanOldCodeOutput();

            log.info("临时文件清理任务执行完成");
        } catch (Exception e) {
            log.error("临时文件清理任务执行失败", e);
        }
    }

    /**
     * 清理截图文件
     * 删除超过保留时间的截图文件
     */
    private void cleanScreenshots() {
        String screenshotsDir = TMP_ROOT_DIR + File.separator + "screenshots";
        File screenshotsDirFile = new File(screenshotsDir);

        if (!screenshotsDirFile.exists() || !screenshotsDirFile.isDirectory()) {
            log.info("截图目录不存在，跳过清理: {}", screenshotsDir);
            return;
        }

        log.info("开始清理截图文件，目录: {}", screenshotsDir);

        AtomicInteger deletedDirs = new AtomicInteger(0);
        AtomicInteger deletedFiles = new AtomicInteger(0);
        AtomicLong totalSize = new AtomicLong(0);

        try {
            // 获取当前时间
            long currentTime = System.currentTimeMillis();
            long retentionTime = SCREENSHOT_RETENTION_HOURS * 60 * 60 * 1000L; // 转换为毫秒

            // 遍历截图目录下的所有子目录
            File[] subDirs = screenshotsDirFile.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    try {
                        // 检查目录的最后修改时间
                        long lastModified = subDir.lastModified();
                        if (currentTime - lastModified > retentionTime) {
                            // 计算目录大小
                            long dirSize = calculateDirectorySize(subDir);

                            // 删除过期目录
                            boolean deleted = FileUtil.del(subDir);
                            if (deleted) {
                                deletedDirs.incrementAndGet();
                                totalSize.addAndGet(dirSize);
                                log.debug("删除过期截图目录: {} (大小: {} bytes)", subDir.getName(), dirSize);
                            } else {
                                log.warn("删除截图目录失败: {}", subDir.getAbsolutePath());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("处理截图目录时出现异常: {}", subDir.getName(), e);
                    }
                }
            }

            // 清理单独的文件（如果有的话）
            File[] files = screenshotsDirFile.listFiles(File::isFile);
            if (files != null) {
                for (File file : files) {
                    try {
                        long lastModified = file.lastModified();
                        if (currentTime - lastModified > retentionTime) {
                            long fileSize = file.length();
                            boolean deleted = FileUtil.del(file);
                            if (deleted) {
                                deletedFiles.incrementAndGet();
                                totalSize.addAndGet(fileSize);
                                log.debug("删除过期截图文件: {} (大小: {} bytes)", file.getName(), fileSize);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("处理截图文件时出现异常: {}", file.getName(), e);
                    }
                }
            }

            log.info("截图文件清理完成 - 删除目录: {} 个, 删除文件: {} 个, 释放空间: {} MB",
                    deletedDirs.get(), deletedFiles.get(), totalSize.get() / (1024 * 1024));

        } catch (Exception e) {
            log.error("清理截图文件时发生异常", e);
        }
    }

    /**
     * 清理过期的代码输出文件（可选功能）
     * 清理code_output目录下的过期文件
     */
    private void cleanOldCodeOutput() {
        String codeOutputDir = TMP_ROOT_DIR + File.separator + "code_output";
        File codeOutputDirFile = new File(codeOutputDir);

        if (!codeOutputDirFile.exists() || !codeOutputDirFile.isDirectory()) {
            log.info("代码输出目录不存在，跳过清理: {}", codeOutputDir);
            return;
        }

        log.info("开始清理过期代码输出文件，目录: {}", codeOutputDir);

        AtomicInteger deletedDirs = new AtomicInteger(0);
        AtomicLong totalSize = new AtomicLong(0);

        try {
            long currentTime = System.currentTimeMillis();
            long retentionTime = CODE_OUTPUT_RETENTION_HOURS * 60 * 60 * 1000L;

            File[] subDirs = codeOutputDirFile.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    try {
                        long lastModified = subDir.lastModified();
                        if (currentTime - lastModified > retentionTime) {
                            long dirSize = calculateDirectorySize(subDir);

                            boolean deleted = FileUtil.del(subDir);
                            if (deleted) {
                                deletedDirs.incrementAndGet();
                                totalSize.addAndGet(dirSize);
                                log.debug("删除过期代码输出目录: {} (大小: {} bytes)", subDir.getName(), dirSize);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("处理代码输出目录时出现异常: {}", subDir.getName(), e);
                    }
                }
            }

            log.info("代码输出文件清理完成 - 删除目录: {} 个, 释放空间: {} MB",
                    deletedDirs.get(), totalSize.get() / (1024 * 1024));

        } catch (Exception e) {
            log.error("清理代码输出文件时发生异常", e);
        }
    }

    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        try {
            return FileUtil.size(directory);
        } catch (Exception e) {
            log.warn("计算目录大小失败: {}", directory.getName(), e);
            return 0;
        }
    }

    /**
     * 手动触发清理任务（用于测试或紧急清理）
     */
    public void manualCleanup() {
        log.info("手动触发临时文件清理任务");
        cleanTmpFiles();
    }

    /**
     * 获取tmp目录统计信息
     */
    public String getTmpDirectoryStats() {
        StringBuilder stats = new StringBuilder();

        // 截图目录统计
        String screenshotsDir = TMP_ROOT_DIR + File.separator + "screenshots";
        File screenshotsDirFile = new File(screenshotsDir);
        if (screenshotsDirFile.exists()) {
            File[] screenshotSubDirs = screenshotsDirFile.listFiles(File::isDirectory);
            long screenshotSize = calculateDirectorySize(screenshotsDirFile);
            stats.append(String.format("截图目录: %d 个子目录, 总大小: %.2f MB%n",
                    screenshotSubDirs != null ? screenshotSubDirs.length : 0,
                    screenshotSize / (1024.0 * 1024.0)));
        }

        // 代码输出目录统计
        String codeOutputDir = TMP_ROOT_DIR + File.separator + "code_output";
        File codeOutputDirFile = new File(codeOutputDir);
        if (codeOutputDirFile.exists()) {
            File[] codeSubDirs = codeOutputDirFile.listFiles(File::isDirectory);
            long codeSize = calculateDirectorySize(codeOutputDirFile);
            stats.append(String.format("代码输出目录: %d 个子目录, 总大小: %.2f MB%n",
                    codeSubDirs != null ? codeSubDirs.length : 0,
                    codeSize / (1024.0 * 1024.0)));
        }

        return stats.toString();
    }
}
