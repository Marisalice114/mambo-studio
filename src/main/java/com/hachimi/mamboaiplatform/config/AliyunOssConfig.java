package com.hachimi.mamboaiplatform.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 *
 * @author your-name
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class AliyunOssConfig {

    /**
     * OSS访问域名端点
     */
    private String endpoint;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret（注意不要泄露）
     */
    private String accessKeySecret;

    /**
     * 地域
     */
    private String region;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 文件访问路径前缀
     */
    private String urlPrefix;

    /**
     * 上传文件夹路径前缀
     */
    private String dirPrefix = "";

    /**
     * 创建OSS客户端Bean
     */
    @Bean
    public OSS ossClient() {
        // 创建ClientBuilderConfiguration实例，用于配置一些额外参数
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();
        // 显式声明使用 V4 签名算法
        config.setSignatureVersion(SignVersion.V4);

        // 创建凭证提供者
        DefaultCredentialProvider credentialsProvider =
                CredentialsProviderFactory.newDefaultCredentialProvider(
                        accessKeyId,
                        accessKeySecret
                );

        // 创建OSSClient实例
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .region(region)
                .clientConfiguration(config)
                .build();
    }
}