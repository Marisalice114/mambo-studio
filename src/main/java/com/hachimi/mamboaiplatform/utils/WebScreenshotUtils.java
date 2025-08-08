package com.hachimi.mamboaiplatform.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenshotUtils {

    // ThreadLocal存储每个线程的WebDriver实例
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // 默认配置
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;

    /**
     * 获取当前线程的WebDriver实例
     * 如果不存在则创建新实例（默认使用Chrome，失败时尝试Edge）
     */
    private static WebDriver getWebDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            try {
                // 优先尝试Chrome
                driver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                log.info("线程 {} 创建Chrome WebDriver成功", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("线程 {} Chrome WebDriver创建失败，尝试Edge: {}", Thread.currentThread().getName(), e.getMessage());
                try {
                    // Chrome失败时尝试Edge
                    driver = initEdgeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    log.info("线程 {} 创建Edge WebDriver成功", Thread.currentThread().getName());
                } catch (Exception edgeException) {
                    log.error("线程 {} 所有浏览器驱动创建失败", Thread.currentThread().getName(), edgeException);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法创建WebDriver实例");
                }
            }
            driverThreadLocal.set(driver);
        }
        return driver;
    }

    /**
     * 清理当前线程的WebDriver资源
     */
    public static void cleanupCurrentThreadDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("线程 {} 的WebDriver已清理", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("清理线程 {} 的WebDriver时出现异常: {}", Thread.currentThread().getName(), e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }

        String threadName = Thread.currentThread().getName();
        log.info("线程 {} 开始处理截图请求: {}", threadName, webUrl);

        try {
            // 创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            // 图片后缀
            final String IMAGE_SUFFIX = ".png";
            // 原始截图文件路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            // 获取当前线程的WebDriver实例
            WebDriver webDriver = getWebDriver();

            // 访问网页
            webDriver.get(webUrl);
            // 等待页面加载完成
            waitForPageLoad(webDriver);
            // 截图
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            // 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("线程 {} 原始截图保存成功: {}", threadName, imageSavePath);

            // 压缩图片
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("线程 {} 压缩图片保存成功: {}", threadName, compressedImagePath);

            // 删除原始图片，只保留压缩图片
            FileUtil.del(imageSavePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("线程 {} 网页截图失败: {}", threadName, webUrl, e);
            return null;
        }
        // 注意：这里不清理WebDriver，保持线程本地实例以供重复使用
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        // 自动管理 ChromeDriver
        WebDriverManager.chromedriver().setup();
        // 配置 Chrome 选项
        ChromeOptions options = new ChromeOptions();
        // 无头模式
        options.addArguments("--headless");
        // 禁用GPU（在某些环境下避免问题）
        options.addArguments("--disable-gpu");
        // 禁用沙盒模式（Docker环境需要）
        options.addArguments("--no-sandbox");
        // 禁用开发者shm使用
        options.addArguments("--disable-dev-shm-usage");
        // 设置窗口大小
        options.addArguments(String.format("--window-size=%d,%d", width, height));
        // 禁用扩展
        options.addArguments("--disable-extensions");
        // 设置用户代理
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // 创建驱动
        WebDriver driver = new ChromeDriver(options);
        // 设置页面加载超时
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        // 设置隐式等待
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    /**
     * 初始化 Edge 浏览器驱动
     */
    private static WebDriver initEdgeDriver(int width, int height) {
        // 自动管理 EdgeDriver
        WebDriverManager.edgedriver().setup();
        // 配置 Edge 选项
        EdgeOptions options = new EdgeOptions();
        // 无头模式
        options.addArguments("--headless");
        // 禁用GPU（在某些环境下避免问题）
        options.addArguments("--disable-gpu");
        // 禁用沙盒模式（Docker环境需要）
        options.addArguments("--no-sandbox");
        // 禁用开发者shm使用
        options.addArguments("--disable-dev-shm-usage");
        // 设置窗口大小
        options.addArguments(String.format("--window-size=%d,%d", width, height));
        // 禁用扩展
        options.addArguments("--disable-extensions");
        // 设置用户代理
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // 创建驱动
        WebDriver driver = new EdgeDriver(options);
        // 设置页面加载超时
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        // 设置隐式等待
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    /**
     * 保存图片到文件
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        // 压缩图片质量（0.3 = 30% 质量）
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }
}
