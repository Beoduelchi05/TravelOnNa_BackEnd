package com.travelonna.demo.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TravelOnNa API")
                        .description("TravelOnNa 서비스의 API 문서\n\n" +
                                "## 인증이 필요한 API\n" +
                                "그룹, 개인 일정, 그룹 일정, 프로필, 팔로우, 여행 장소 관련 API는 인증이 필요합니다.\n\n" +
                                "## 인증이 필요 없는 API\n" +
                                "로그인, 회원가입 등 인증 관련 API는 인증이 필요하지 않습니다.\n\n" +
                                "## 인증 방법\n" +
                                "1. `/api/v1/auth/google` API를 사용하여 로그인합니다.\n" +
                                "2. 응답으로 받은 JWT 토큰을 복사합니다.\n" +
                                "3. 오른쪽 상단의 'Authorize' 버튼을 클릭합니다.\n" +
                                "4. 'bearer-key' 항목에 `Bearer {토큰값}` 형식으로 입력합니다. (예: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...)\n" +
                                "5. 'Authorize' 버튼을 클릭하여 인증을 완료합니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TravelOnNa Team")
                                .email("travelonna@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
                .tags(Arrays.asList(
                        new Tag().name("인증").description("인증 관련 API (인증 불필요)"),
                        new Tag().name("그룹").description("그룹 관리 API (인증 필요)"),
                        new Tag().name("개인 일정").description("개인 일정 관리 API (인증 필요)"),
                        new Tag().name("그룹 일정").description("그룹 일정 관리 API (인증 필요)"),
                        new Tag().name("Profile").description("프로필 관리 API (인증 필요)"),
                        new Tag().name("Follow").description("팔로우 관련 API (인증 필요)"),
                        new Tag().name("여행 장소").description("여행 장소 관리 API (인증 필요)"),
                        new Tag().name("여행 기록").description("여행 기록 CRUD 및 좋아요/댓글 관리 API (인증 필요)")
                ));
    }
} 