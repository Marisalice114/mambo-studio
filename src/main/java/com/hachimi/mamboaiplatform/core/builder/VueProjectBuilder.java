package com.hachimi.mamboaiplatform.core.builder;


import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VueProjectBuilder {


    /**
     * 异步构建项目（不阻塞主流程）
     *
     * @param projectPath 项目路径
     */
    public void buildProjectAsync(String projectPath) {
        // 在单独的线程中执行构建，避免阻塞主流程
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis()).start(() -> {
            try {
                buildVueProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
            }
        });
    }



    public boolean buildVueProject(String projectPath) {
        File projectDir = new File(projectPath);

        if (projectDir == null || !projectDir.exists() || !projectDir.isDirectory()) {
            log.error("无效的项目目录: {}", projectDir);
            return false;
        }

        // 判断是否有 package.json 文件
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目目录中未找到 package.json 文件: {}", projectDir.getAbsolutePath());
            return false;
        }
        log.info( "开始构建 Vue 项目: {}", projectDir.getAbsolutePath());

        // 执行 npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 失败");
            return false;
        }

        // 执行 npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 失败");
            return false;
        }

        // 判断是否有 dist 文件
        File distDir = new File(projectDir, "dist");
        if ( !distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成，但是未生生成dist目录: {}", distDir.getAbsolutePath());
            return false;
        }

        log.info("Vue项目构建成功: {}", projectDir.getAbsolutePath());
        return true;
    }



    /**
     * 判断是否为Windows系统
     * @return true表示Windows系统，false表示非Windows系统
     */
    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    /**
     * 获取适合当前操作系统的npm命令
     * @return Windows下返回"npm.cmd"，Linux/Mac下返回"npm"
     */
    private String getNpmCommand() {
        return isWindows() ? "npm.cmd" : "npm";
    }

    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String npmCommand = getNpmCommand() + " install";
        return executeCommand(projectDir, npmCommand, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String npmCommand = getNpmCommand() + " run build";
        return executeCommand(projectDir, npmCommand, 180); // 3分钟超时
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {} (操作系统: {})",
                    workingDir.getAbsolutePath(), command, System.getProperty("os.name"));

            String[] commandArray;
            if (isWindows()) {
                // Windows下使用cmd /c执行命令
                commandArray = new String[]{"cmd", "/c", command};
            } else {
                // Linux/Mac下使用sh -c执行命令
                commandArray = new String[]{"sh", "-c", command};
            }

            ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
            processBuilder.directory(workingDir);
            processBuilder.redirectErrorStream(true); // 合并错误流和输出流

            Process process = processBuilder.start();

            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

}
