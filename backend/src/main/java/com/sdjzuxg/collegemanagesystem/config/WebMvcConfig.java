package com.sdjzuxg.collegemanagesystem.config;

import com.sdjzuxg.collegemanagesystem.common.auth.AuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 附件存储目录,与 FileController 共用 application.yaml 的 file.upload-path 配置
    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射：访问 /upload/** 时，去配置的 uploads 目录下找文件
        // 相对路径 ./uploads/ 统一转成绝对路径,避免 file: 协议解析歧义
        java.io.File dir = new java.io.File(uploadPath);
        String absolutePath = dir.getAbsolutePath().replace("\\", "/") + "/";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolutePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求,放行登录接口、静态资源、错误页
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/login/**",
                        "/upload/**",
                        "/file/preview/**",
                        "/file/download/**",
                        "/error",
                        "/favicon.ico"
                );
    }
}
