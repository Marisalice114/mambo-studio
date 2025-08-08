package com.hachimi.mamboaiplatform.manager;



import com.hachimi.mamboaiplatform.config.AliyunOssConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.OSSException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * OSS对象存储管理器
 *
 * @author your-name
 */
@Component
@Slf4j
public class OssManager {

    @Resource
    private AliyunOssConfig AliyunConfig;

    @Resource
    private OSS ossClient;

    /**
     * 上传对象（File类型）
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    AliyunConfig.getBucketName(),
                    key,
                    file
            );
            return ossClient.putObject(putObjectRequest);
        } catch (OSSException e) {
            log.error("OSS上传文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 上传对象（InputStream类型）
     *
     * @param key         唯一键
     * @param inputStream 输入流
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, InputStream inputStream) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    AliyunConfig.getBucketName(),
                    key,
                    inputStream
            );
            return ossClient.putObject(putObjectRequest);
        } catch (OSSException e) {
            log.error("OSS上传流失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 上传对象（字节数组）
     *
     * @param key   唯一键
     * @param bytes 字节数组
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, byte[] bytes) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            return putObject(key, inputStream);
        } catch (Exception e) {
            log.error("OSS上传字节数组失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 上传文件到 OSS 并返回访问 URL
     *
     * @param key  OSS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        try {
            // 上传文件
            PutObjectResult result = putObject(key, file);
            if (result != null && result.getETag() != null) {
                // 构建访问URL
                String url = String.format("%s%s", AliyunConfig.getUrlPrefix(), key);
                log.info("文件上传OSS成功: {} -> {}", file.getName(), url);
                return url;
            } else {
                log.error("文件上传OSS失败，返回结果为空");
                return null;
            }
        } catch (Exception e) {
            log.error("文件上传OSS异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 上传MultipartFile到 OSS 并返回访问 URL
     *
     * @param key  OSS对象键（完整路径）
     * @param file MultipartFile文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, MultipartFile file) {
        try {
            // 上传文件
            PutObjectResult result = putObject(key, file.getInputStream());
            if (result != null && result.getETag() != null) {
                // 构建访问URL
                String url = String.format("%s%s", AliyunConfig.getUrlPrefix(), key);
                log.info("文件上传OSS成功: {} -> {}", file.getOriginalFilename(), url);
                return url;
            } else {
                log.error("文件上传OSS失败，返回结果为空");
                return null;
            }
        } catch (Exception e) {
            log.error("文件上传OSS异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 上传文件并自动生成唯一键名
     *
     * @param file        要上传的文件
     * @param pathPrefix  路径前缀（可选）
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFileWithUniqueKey(MultipartFile file, String pathPrefix) {
        String key = generateUniqueKey(file.getOriginalFilename(), pathPrefix);
        return uploadFile(key, file);
    }

    /**
     * 上传文件并自动生成唯一键名（无路径前缀）
     *
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFileWithUniqueKey(MultipartFile file) {
        return uploadFileWithUniqueKey(file, AliyunConfig.getDirPrefix());
    }

    /**
     * 下载文件
     *
     * @param key 文件键
     * @return OSS对象
     */
    public OSSObject getObject(String key) {
        try {
            return ossClient.getObject(AliyunConfig.getBucketName(), key);
        } catch (OSSException e) {
            log.error("OSS下载文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除文件
     *
     * @param key 文件键
     * @return 是否删除成功
     */
    public boolean deleteObject(String key) {
        try {
            ossClient.deleteObject(AliyunConfig.getBucketName(), key);
            log.info("OSS删除文件成功: {}", key);
            return true;
        } catch (OSSException e) {
            log.error("OSS删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param key 文件键
     * @return 是否存在
     */
    public boolean doesObjectExist(String key) {
        try {
            return ossClient.doesObjectExist(AliyunConfig.getBucketName(), key);
        } catch (OSSException e) {
            log.error("OSS检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 列出文件
     *
     * @param prefix 前缀（可选）
     * @return 文件列表
     */
    public List<OSSObjectSummary> listObjects(String prefix) {
        try {
            ObjectListing objectListing;
            if (prefix != null && !prefix.isEmpty()) {
                objectListing = ossClient.listObjects(AliyunConfig.getBucketName(), prefix);
            } else {
                objectListing = ossClient.listObjects(AliyunConfig.getBucketName());
            }
            return objectListing.getObjectSummaries();
        } catch (OSSException e) {
            log.error("OSS列出文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 列出所有文件
     *
     * @return 文件列表
     */
    public List<OSSObjectSummary> listObjects() {
        return listObjects(null);
    }

    /**
     * 生成带签名的URL（用于私有文件访问）
     *
     * @param key        文件键
     * @param expiration 过期时间
     * @return 带签名的URL
     */
    public String generatePresignedUrl(String key, Date expiration) {
        try {
            URL url = ossClient.generatePresignedUrl(AliyunConfig.getBucketName(), key, expiration);
            return url.toString();
        } catch (OSSException e) {
            log.error("OSS生成签名URL失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成唯一的对象键
     *
     * @param originalFilename 原始文件名
     * @param pathPrefix      路径前缀
     * @return 唯一键
     */
    private String generateUniqueKey(String originalFilename, String pathPrefix) {
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 构建完整路径
        String prefix = (pathPrefix != null && !pathPrefix.isEmpty()) ? pathPrefix : "";
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix += "/";
        }

        return prefix + uuid + extension;
    }

    /**
     * 获取文件的完整访问URL
     *
     * @param key 文件键
     * @return 完整的访问URL
     */
    public String getFileUrl(String key) {
        return String.format("%s%s", AliyunConfig.getUrlPrefix(), key);
    }
}