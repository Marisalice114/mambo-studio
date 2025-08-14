package com.hachimi.mamboaiplatform.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.hachimi.mamboaiplatform.langgraph4j.model.ImageCategoryEnum;
import com.hachimi.mamboaiplatform.langgraph4j.model.ImageResource;
import com.hachimi.mamboaiplatform.manager.OssManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model:wan2.2-t2i-plus}")
    private String imageModel;

    @Resource
    private OssManager ossManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 构建 Logo 设计提示词
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何中英文文字！Logo 介绍：%s", description);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(logoPrompt)
                    .size("512*512")
                    .n(1) // 生成 1 张
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);

            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for (Map<String, String> imageResult : results) {
                    String imageUrl = imageResult.get("url");
                    if (StrUtil.isNotBlank(imageUrl)) {
                        // 下载图片并上传到OSS
                        String ossUrl = downloadAndUploadLogo(imageUrl, description);
                        if (StrUtil.isNotBlank(ossUrl)) {
                            logoList.add(ImageResource.builder()
                                    .category(ImageCategoryEnum.LOGO)
                                    .description(description)
                                    .url(ossUrl)
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }

    /**
     * 下载Logo图片并上传到OSS
     * @param imageUrl 原始图片URL
     * @param description Logo描述
     * @return OSS中的图片URL
     */
    private String downloadAndUploadLogo(String imageUrl, String description) {
        File tempFile = null;
        try {
            // 创建临时目录
            String tmpDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "logos";
            FileUtil.mkdir(tmpDir);

            // 生成临时文件名
            String fileName = String.format("logo_%s_%s.jpg",
                    RandomUtil.randomString(8), System.currentTimeMillis());
            tempFile = new File(tmpDir, fileName);

            log.info("开始下载Logo图片: {}", imageUrl);

            // 使用Hutool下载图片
            HttpUtil.downloadFile(imageUrl, tempFile);

            // 检查文件是否下载成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("Logo图片下载失败: {}", imageUrl);
                return null;
            }

            log.info("Logo图片下载成功，文件大小: {} bytes", tempFile.length());

            // 上传到OSS
            String ossKey = String.format("logos/%s/%s",
                    RandomUtil.randomString(6), fileName);
            String ossUrl = ossManager.uploadFile(ossKey, tempFile);

            if (StrUtil.isNotBlank(ossUrl)) {
                log.info("Logo图片上传OSS成功: {}", ossUrl);
                return ossUrl;
            } else {
                log.error("Logo图片上传OSS失败");
                return null;
            }

        } catch (Exception e) {
            log.error("下载或上传Logo图片失败: {}", e.getMessage(), e);
            return null;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = FileUtil.del(tempFile);
                if (deleted) {
                    log.debug("临时Logo文件已清理: {}", tempFile.getName());
                }
            }
        }
    }
}
