package com.xiaozhan.config;

import com.xiaozhan.security.AuthInterceptor;
import com.xiaozhan.security.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置：跨域、拦截器、静态资源映射
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final RoleInterceptor roleInterceptor;
    private final XiaozhanProperties properties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .order(1);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .order(2);
    }

    /**
     * 本地磁盘文件访问映射：/files/** -> 本地上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = Paths.get(properties.getUpload().getBasePath()).toAbsolutePath().normalize().toString();
        registry.addResourceHandler(properties.getUpload().getUrlPrefix() + "/**")
                .addResourceLocations("file:" + basePath + "/");
    }
}
