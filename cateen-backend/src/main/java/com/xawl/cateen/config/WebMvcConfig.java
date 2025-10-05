package com.xawl.cateen.config;

import com.xawl.cateen.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * @author xawl
 * @date 2025-10-03
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 管理端认证路径
                        "/api/admin/auth/login",
                        "/api/admin/auth/register",
                        // 小程序端认证路径
                        "/api/mini/auth/login",
                        "/api/mini/auth/refresh",
                        // 数据库监控路径
                        "/druid/**",
                        // Swagger相关路径
                        "/doc.html",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/favicon.ico",
                        "/error"
                );
    }

}

