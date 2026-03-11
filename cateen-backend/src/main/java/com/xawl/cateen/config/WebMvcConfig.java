package com.xawl.cateen.config;

import com.xawl.cateen.interceptor.AuthInterceptor;
import com.xawl.cateen.interceptor.OptionalAuthInterceptor;
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
    private final OptionalAuthInterceptor optionalAuthInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
//         可选认证拦截器 - 用于美食相关API（有Token时解析，无Token也允许访问）
        registry.addInterceptor(optionalAuthInterceptor)
                .addPathPatterns("/api/mini/foods/**")
                .order(1); // 优先级高，先执行

        // 必须认证拦截器 - 用于需要登录的API
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 管理端认证路径
                        "/api/admin/auth/login",
                        "/api/admin/auth/register",
                        // 小程序端认证路径
                        "/api/mini/auth/login",
                        "/api/mini/auth/refresh",
                        // 小程序端公开API（不需要认证）
                        "/api/mini/ranking/**",
                        "/api/mini/categories/**",
                        "/api/mini/foods/**", // 美食API使用可选认证拦截器
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
                )
                .order(2); // 优先级低，后执行
    }

}

