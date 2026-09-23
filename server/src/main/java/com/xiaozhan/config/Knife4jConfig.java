package com.xiaozhan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 文档配置
 */
@Configuration
public class Knife4jConfig {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI xiaozhanOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校栈 —— 应届生项目作品集与能力背书平台 API")
                        .description("""
                                校栈后端接口文档。

                                角色说明：STUDENT（学生）/ EXPERT（点评专家）/ COMPANY（企业）/ ADMIN（平台管理员）。
                                除登录注册等公开接口外，其余接口需要在请求头携带 `Authorization: Bearer {token}`。
                                """)
                        .version("v1.0.0")
                        .contact(new Contact().name("校栈研发团队")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
