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
                                "## 사용자 행동 추적 시스템\n" +
                                "API 사용 시 다음 행동들이 자동으로 user_actions 테이블에 기록되어 추천 시스템에 활용됩니다 (공개 기록에 대해서만):\n" +
                                "- **POST**: 여행 기록 생성 시\n" +
                                "- **VIEW**: 공개 여행 기록 조회 시 (1시간 내 중복 제외)\n" +
                                "- **LIKE**: 공개 여행 기록 좋아요 추가 시 (취소 시에는 기록되지 않음)\n" +
                                "- **COMMENT**: 공개 여행 기록에 댓글 작성 시\n\n" +
                                "## 추천 시스템\n" +
                                "수집된 사용자 행동 데이터는 ALS(Alternating Least Squares) 협업 필터링 알고리즘을 통해 개인화된 여행 기록 추천에 활용됩니다.\n" +
                                "- 배치 처리: 매일 새벽 2시 전체 모델 학습, 6시간마다 증분 업데이트\n" +
                                "- 추천 결과: recommendations 테이블에 저장\n\n" +
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
                        new Tag().name("여행 기록").description("여행 기록 CRUD 및 좋아요/댓글 관리 API (인증 필요)"),
                        new Tag().name("추천").description("AI 기반 개인화 추천 API (인증 필요)")
                ));
    }
} 