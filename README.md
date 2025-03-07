# 여행ON나 (Travel-ON-NA) 백엔드

여행ON나는 트리플의 여행 계획 생성과 인스타그램의 소셜 네트워크 기능이 결합된 소셜 관광 플랫폼입니다.

## 기술 스택

- 백엔드: Spring Boot 3.2.3, Java 17
- 데이터베이스: MySQL (AWS RDS)
- 인증: OAuth2.0, JWT
- API 문서: Swagger UI (SpringDoc OpenAPI)

## 주요 기능

- Google OAuth2.0을 이용한 소셜 로그인
- JWT 기반 인증 시스템
- 사용자 관리
- 여행 계획 생성 및 공유
- 소셜 네트워킹 기능

## 개발 환경 설정

### 필수 요구사항

- Java 17 이상
- Gradle
- MySQL

### 로컬 개발 환경 설정

1. 프로젝트 클론
```bash
git clone https://github.com/your-username/travelonna.git
cd travelonna
```

2. 애플리케이션 실행
```bash
./gradlew bootRun
```

3. 애플리케이션 빌드
```bash
./gradlew build
```

## API 문서 (Swagger UI)

애플리케이션이 실행된 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- http://localhost:8080/swagger-ui.html

Swagger UI를 통해 다음과 같은 작업을 수행할 수 있습니다:
- 모든 API 엔드포인트 확인
- API 요청 및 응답 형식 확인
- API 직접 테스트 (Try it out 기능)
- JWT 토큰을 사용한 인증 테스트

### 인증 API

#### Google 로그인
- URL: `/api/auth/google`
- Method: `POST`
- Request Body:
  ```json
  {
    "code": "google-authorization-code"
  }
  ```
- Response:
  ```json
  {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
  ```

#### 토큰 갱신
- URL: `/api/auth/refresh`
- Method: `POST`
- Request Body:
  ```json
  {
    "refreshToken": "jwt-refresh-token"
  }
  ```
- Response:
  ```json
  {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "existing-jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
  ```

## 안드로이드 OAuth2.0 설정

안드로이드 OAuth2.0 클라이언트는 일반 웹 클라이언트와 달리 client_secret이 없습니다. 대신 다음과 같은 설정이 필요합니다:

1. Google Cloud Console에서 OAuth2.0 클라이언트 ID 생성 (Android 타입)
2. 리다이렉트 URI 설정: `com.travelonna.app:/oauth2redirect`
3. 안드로이드 앱에서 인증 코드를 받아 백엔드로 전송

## 보안 설정

애플리케이션의 보안 설정은 `application-secret.yml` 파일에 저장됩니다. 이 파일은 Git에 커밋되지 않으며, 다음과 같은 형식으로 작성해야 합니다:

```yaml
db:
  password: your-database-password

google:
  client-id: your-google-client-id
  redirect-uri: "com.travelonna.app:/oauth2redirect"

jwt:
  secret-key: your-jwt-secret-key-should-be-at-least-64-bytes-long
``` 