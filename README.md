# 여행ON나 (Travel-ON-NA) 백엔드

여행ON나는 트리플의 여행 계획 생성과 인스타그램의 소셜 네트워크 기능이 결합된 소셜 관광 플랫폼입니다.

## 기술 스택

- **백엔드**: Spring Boot 3.2.3, Java 17
- **데이터베이스**: MySQL (AWS RDS)
- **인증**: OAuth2.0, JWT
- **API 문서**: Swagger UI (SpringDoc OpenAPI)
- **배포**: AWS EC2, Docker

## 주요 기능

- Google OAuth2.0을 이용한 소셜 로그인
- JWT 기반 인증 시스템
- 사용자 프로필 관리
- 여행 계획 생성, 공유, 수정
- 소셜 네트워킹 (팔로우, 좋아요, 댓글)
- 여행 장소 검색 및 추천

## 로컬 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- Gradle
- MySQL

### 실행 방법
```bash
git clone https://github.com/KIMMIN5/TravelOnNa_BackEnd.git
cd TravelOnNa_BackEnd/demo
./gradlew bootRun
```

## API 문서

애플리케이션 실행 후 Swagger UI로 API 문서를 확인할 수 있습니다:
- http://localhost:8080/swagger-ui.html

## 환경 설정

`application-secret.yml` 파일에 다음과 같은 설정이 필요합니다:

```yaml
db:
  password: your-database-password

google:
  client-id: your-google-client-id
  redirect-uri: "com.travelonna.app:/oauth2redirect"

jwt:
  secret-key: your-jwt-secret-key
```

## 프로젝트 구조

```
src/main/java/com/travelonna/demo/
├── global/         # 공통 설정 및 유틸리티
├── domain/         # 도메인별 기능 구현
│   ├── auth/       # 인증 관련 기능
│   ├── user/       # 사용자 관련 기능
│   ├── plan/       # 여행 계획 관련 기능
│   ├── log/        # 여행 기록 관련 기능
│   ├── follow/     # 팔로우 관련 기능
│   └── group/      # 그룹 관련 기능
└── TravelonnaApplication.java  # 애플리케이션 진입점
```