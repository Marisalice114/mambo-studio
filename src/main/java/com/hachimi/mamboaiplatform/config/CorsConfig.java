package com.hachimi.mamboaiplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许跨域访问的来源，逗号分隔。
     * 默认仅允许本地开发地址，生产环境请通过环境变量 APP_CORS_ALLOWED_ORIGINS 覆盖为前端域名。
     * 注意：由于启用了 allowCredentials(true)，禁止使用 "*" 通配。
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8234}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 仅放行配置的来源（禁止 * 与 allowCredentials 并用）
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}