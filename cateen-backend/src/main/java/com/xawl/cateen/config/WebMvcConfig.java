package com.xawl.cateen.config;

import com.xawl.cateen.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
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
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/druid/**",
                        // Swagger相关路径
                        "/doc.html",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/swagger-ui/**"
                );
    }

}

