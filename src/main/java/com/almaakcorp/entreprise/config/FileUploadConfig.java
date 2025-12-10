package com.almaakcorp.entreprise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
@ConfigurationProperties(prefix = "app.file")
public class FileUploadConfig {

    private String uploadDir = "uploads";
    private String baseUrl = "http://localhost:8080";
    private long maxFileSize = 5 * 1024 * 1024; // 5MB
    private long maxRequestSize = 10 * 1024 * 1024; // 10MB

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    // Getters and setters
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }
}