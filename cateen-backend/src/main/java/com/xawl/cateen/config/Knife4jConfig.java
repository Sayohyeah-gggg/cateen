package com.xawl.cateen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Knife4j（Swagger）配置类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xawl.cateen.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * API基本信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("美食评估管理系统 API文档")
                .description("基于Spring Boot + MyBatis-Plus的美食评估管理系统后端接口文档")
                .version("1.0.0")
                .contact(new Contact("XAWL Team", "https://github.com", "contact@xawl.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }

    /**
     * 配置安全方案（JWT Token）
     */
    private List<SecurityScheme> securitySchemes() {
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
        return Collections.singletonList(apiKey);
    }

    /**
     * 配置安全上下文
     */
    private List<SecurityContext> securityContexts() {
        SecurityContext securityContext = SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("^(?!auth).*$"))
                .build();
        return Collections.singletonList(securityContext);
    }

    /**
     * 默认安全引用
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("Authorization", authorizationScopes));
    }

}

