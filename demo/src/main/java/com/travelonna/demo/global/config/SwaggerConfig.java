package com.travelonna.demo.global.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("여행ON나 API")
                .version("v1.0")
                .description("여행ON나 애플리케이션의 API 문서입니다.\n\n" +
                        "## 인증 방법\n\n" +
                        "대부분의 API는 인증이 필요합니다. 다음 단계를 따라 인증을 진행하세요:\n\n" +
                        "1. `/api/auth/login` API를 사용하여 로그인합니다.\n" +
                        "2. 응답으로 받은 JWT 토큰을 복사합니다.\n" +
                        "3. 오른쪽 상단의 'Authorize' 버튼을 클릭합니다.\n" +
                        "4. 'bearerAuth' 항목에 `Bearer {토큰값}` 형식으로 입력합니다. (예: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...)\n" +
                        "5. 'Authorize' 버튼을 클릭하여 인증을 완료합니다.\n\n" +
                        "인증이 필요한 API는 🔒 아이콘으로 표시됩니다.")
                .contact(new Contact()
                        .name("TravelONna Team")
                        .email("contact@travelonna.com")
                        .url("https://travelonna.com"))
                .license(new License()
                        .name("Apache License Version 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0"));

        // JWT 인증 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT 인증 토큰을 입력하세요. 형식: Bearer {token}");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // API 그룹 태그 정의
        Tag authTag = new Tag().name("인증").description("인증 관련 API");
        Tag profileTag = new Tag().name("Profile").description("프로필 관리 API (로그인 필요)");
        Tag followTag = new Tag().name("Follow").description("팔로우 관련 API (로그인 필요)");
        Tag planTag = new Tag().name("개인 일정").description("개인 일정 관리 API (로그인 필요)");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement)
                .tags(Arrays.asList(authTag, profileTag, followTag, planTag))
                .externalDocs(new ExternalDocumentation()
                        .description("여행ON나 API 가이드")
                        .url("https://travelonna.com/api-guide"));
    }
} 