# <a href="#" target="_blank">여행ON나 (TravelOnNa)</a>: AI 기반 소셜 관광 플랫폼 백엔드

![TravelOnNa Banner](/images/여행ON나_로고.png)

## Team Member

<table>
<thead>
<tr>
<th>Name</th>
<th>Part</th>
<th>What I do</th>
<th>Tech Stack</th>
</tr>
</thead>
<tbody>
<tr>
<td><a href="#" target="_blank">김민</a></td>
<td>BE Lead</td>
<td>Spring Boot 백엔드 아키텍처 설계, OAuth2 인증, 여행 계획 API 개발</td>
<td>
  <img src="https://img.shields.io/badge/Spring_Boot-6db33f?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
</td>
</tr>
<tr>
<td><a href="#" target="_blank">개발자</a></td>
<td>AI/ML</td>
<td>추천 시스템 개발, ALS 협업 필터링, 사용자 행동 분석 모델링</td>
<td>
  <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python">
  <img src="https://img.shields.io/badge/Machine_Learning-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white" alt="ML">
</td>
</tr>
<tr>
<td><a href="#" target="_blank">개발자</a></td>
<td>DevOps</td>
<td>AWS 인프라 구축, Docker 컨테이너화, CI/CD 파이프라인 구성</td>
<td>
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white" alt="AWS">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
</td>
</tr>
</tbody>
</table>

# 프로젝트 개요

[![Swagger API](https://img.shields.io/badge/API_Docs-Swagger-85ea2d?style=for-the-badge&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)
[![Docker Hub](https://img.shields.io/badge/Docker-Hub-2496ED?style=for-the-badge&logo=docker&logoColor=white)](#)

**여행ON나(TravelOnNa)**는 **트리플의 여행 계획 생성**과 **인스타그램의 소셜 네트워크 기능**이 결합된 혁신적인 소셜 관광 플랫폼의 백엔드 시스템입니다.

사용자가 여행 계획을 세우고, 여행 기록을 공유하며, AI 기반 개인화 추천을 통해 새로운 여행 경험을 발견할 수 있도록 돕는 REST API 서버입니다.

# 주요 기능

## 1. AI 기반 개인화 추천 시스템
- **추천 알고리즘**: ALS (Alternating Least Squares) 협업 필터링 + 인기도 하이브리드
- **사용자 행동 추적**: 여행 기록 생성/조회/좋아요/댓글 자동 수집 및 분석
- **배치 처리**: 전체 배치(매일 새벽 2시), 증분 배치(6시간마다)
- **개인정보 보호**: 공개 기록에 대해서만 추적

## 2. 소셜 네트워킹 기능
- **팔로우 시스템**: 사용자 간 팔로우/언팔로우
- **상호작용**: 좋아요, 댓글/대댓글 시스템
- **실시간 알림**: WebSocket 기반 실시간 상호작용
- **프라이버시**: 공개/비공개 설정

## 3. 여행 계획 및 기록 관리
- **계획 생성**: 일정별 상세 여행 계획 작성
- **기록 공유**: 이미지 업로드, 위치 정보 포함
- **그룹 여행**: 다중 사용자 여행 계획 협업
- **검색 기능**: Google Places API 연동

## 4. 인증 및 보안
- **OAuth2.0**: Google 소셜 로그인
- **JWT 토큰**: 안전한 인증 및 세션 관리
- **Spring Security**: 엔드포인트 보안 및 권한 관리

# 시스템 아키텍처

<img src="architecture.png">

## 아키텍처 설계 원칙

### 1. 마이크로서비스 지향 설계
- **Domain-Driven Design**: 사용자, 여행계획, 기록, 추천 등 도메인별 모듈 분리
- **API Gateway**: Spring Boot가 중앙 게이트웨이 역할 수행
- **Service Isolation**: 각 도메인 서비스 독립적 운영

### 2. AI/ML 파이프라인 통합
- **추천 엔진**: Python 기반 ML 서비스와 Spring Boot 연동
- **배치 처리**: 스케줄링 기반 추천 모델 학습 및 업데이트
- **Real-time Inference**: 실시간 추천 결과 제공

### 3. 확장 가능한 인프라
- **AWS 기반**: EC2, RDS, S3 등 클라우드 네이티브 구성
- **Docker 컨테이너화**: 환경 독립성 및 배포 효율성
- **Load Balancing**: ALB를 통한 트래픽 분산

# Tech Stack

**Backend Framework**

[![Java](https://img.shields.io/badge/Java_17-orange?style=for-the-badge&logo=openjdk&logoColor=white)]()
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2.3-6db33f?style=for-the-badge&logo=springboot&logoColor=white)]()
[![Spring Security](https://img.shields.io/badge/Spring_Security-6db33f?style=for-the-badge&logo=springsecurity&logoColor=white)]()
[![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6db33f?style=for-the-badge&logo=spring&logoColor=white)]()
[![WebSocket](https://img.shields.io/badge/WebSocket-000000?style=for-the-badge&logo=socketdotio&logoColor=white)]()

**Database & Storage**

[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)]()
[![AWS RDS](https://img.shields.io/badge/AWS_RDS-FF9900?style=for-the-badge&logo=amazon-rds&logoColor=white)]()
[![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)]()
[![H2](https://img.shields.io/badge/H2_Database-0078D4?style=for-the-badge&logo=h2&logoColor=white)]()

**Authentication & Security**

[![OAuth2](https://img.shields.io/badge/OAuth2.0-4285F4?style=for-the-badge&logo=google&logoColor=white)]()
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)]()
[![Google API](https://img.shields.io/badge/Google_API-4285F4?style=for-the-badge&logo=google&logoColor=white)]()

**AI/ML & Recommendation**

[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)]()
[![Scikit Learn](https://img.shields.io/badge/Scikit_Learn-F7931E?style=for-the-badge&logo=scikit-learn&logoColor=white)]()
[![Pandas](https://img.shields.io/badge/Pandas-150458?style=for-the-badge&logo=pandas&logoColor=white)]()
[![NumPy](https://img.shields.io/badge/NumPy-013243?style=for-the-badge&logo=numpy&logoColor=white)]()

**DevOps & Infrastructure**

[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)]()
[![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazon-ec2&logoColor=white)]()
[![Gradle](https://img.shields.io/badge/Gradle-02303a?style=for-the-badge&logo=gradle&logoColor=white)]()
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)]()

**Documentation & Testing**

[![Swagger](https://img.shields.io/badge/Swagger-85ea2d?style=for-the-badge&logo=swagger&logoColor=black)]()
[![JUnit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=java&logoColor=white)]()
[![Spring Boot Test](https://img.shields.io/badge/Spring_Boot_Test-6db33f?style=for-the-badge&logo=spring&logoColor=white)]()

**Monitoring & Utilities**

[![Spring Actuator](https://img.shields.io/badge/Spring_Actuator-6db33f?style=for-the-badge&logo=spring&logoColor=white)]()
[![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white)]()
[![Thumbnailator](https://img.shields.io/badge/Thumbnailator-4B8BBE?style=for-the-badge&logo=java&logoColor=white)]()

# 로컬 개발 환경 설정

## 필수 요구사항

- **Java 17** 이상
- **MySQL 8.0** 이상  
- **Gradle 7.0** 이상
- **Docker** (선택사항)

## 설치 및 실행

### 1. 저장소 클론
```bash
git clone https://github.com/KIMMIN5/TravelOnNa_BackEnd.git
cd TravelOnNa_BackEnd
```

### 2. 환경 설정 파일 생성
`demo/src/main/resources/application-secret.yml` 파일을 생성하고 다음 내용을 추가:

```yaml
# 데이터베이스 설정
db:
  password: your-database-password
  url: jdbc:mysql://localhost:3306/travelonna?useSSL=false&allowPublicKeyRetrieval=true

# Google OAuth2 설정
google:
  client-id: your-google-client-id
  client-secret: your-google-client-secret
  redirect-uri: "com.travelonna.app:/oauth2redirect"

# JWT 설정
jwt:
  secret-key: your-jwt-secret-key-min-256-bits
  access-token-expiration: 3600000    # 1시간
  refresh-token-expiration: 604800000  # 7일

# AWS S3 설정 (선택사항)
aws:
  s3:
    access-key: your-s3-access-key
    secret-key: your-s3-secret-key
    bucket-name: your-s3-bucket-name
    region: ap-northeast-2
```

### 3. 데이터베이스 초기화
```bash
# MySQL 데이터베이스 생성
mysql -u root -p
CREATE DATABASE travelonna CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 테이블 생성 (애플리케이션 실행 시 자동 생성됨)
```

### 4. 애플리케이션 실행
```bash
cd demo
./gradlew bootRun
```

### 5. Docker를 이용한 실행 (선택사항)
```bash
# Docker 이미지 빌드
docker build -t travelonna-backend .

# 컨테이너 실행
docker run -p 8080:8080 -v /path/to/config:/config travelonna-backend
```

## API 문서 확인

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

# 프로젝트 구조

```
src/main/java/com/travelonna/demo/
├── TravelonnaApplication.java          # 애플리케이션 진입점
├── global/                             # 공통 설정 및 유틸리티
│   ├── config/                         # 설정 클래스들
│   │   ├── SecurityConfig.java         # Spring Security 설정
│   │   ├── JwtConfig.java              # JWT 설정
│   │   ├── SwaggerConfig.java          # API 문서 설정
│   │   └── WebSocketConfig.java        # WebSocket 설정
│   ├── exception/                      # 예외 처리
│   │   ├── GlobalExceptionHandler.java # 전역 예외 핸들러
│   │   └── CustomExceptions.java       # 커스텀 예외 클래스들
│   └── utils/                          # 유틸리티 클래스들
│       ├── JwtUtil.java                # JWT 유틸리티
│       └── FileUtil.java               # 파일 처리 유틸리티
└── domain/                             # 도메인별 기능 구현
    ├── auth/                           # 인증 관련 기능
    │   ├── controller/                 # 인증 컨트롤러
    │   ├── service/                    # 인증 서비스
    │   ├── repository/                 # 인증 리포지토리
    │   └── dto/                        # 인증 DTO
    ├── user/                           # 사용자 관련 기능
    │   ├── controller/                 # 사용자 컨트롤러
    │   ├── service/                    # 사용자 서비스
    │   ├── repository/                 # 사용자 리포지토리
    │   ├── entity/                     # 사용자 엔티티
    │   └── dto/                        # 사용자 DTO
    ├── plan/                           # 여행 계획 관련 기능
    │   ├── controller/                 # 계획 컨트롤러
    │   ├── service/                    # 계획 서비스
    │   ├── repository/                 # 계획 리포지토리
    │   ├── entity/                     # 계획 엔티티
    │   └── dto/                        # 계획 DTO
    ├── log/                            # 여행 기록 관련 기능
    │   ├── controller/                 # 기록 컨트롤러
    │   ├── service/                    # 기록 서비스
    │   ├── repository/                 # 기록 리포지토리
    │   ├── entity/                     # 기록 엔티티
    │   └── dto/                        # 기록 DTO
    ├── follow/                         # 팔로우 관련 기능
    │   ├── controller/                 # 팔로우 컨트롤러
    │   ├── service/                    # 팔로우 서비스
    │   ├── repository/                 # 팔로우 리포지토리
    │   ├── entity/                     # 팔로우 엔티티
    │   └── dto/                        # 팔로우 DTO
    ├── group/                          # 그룹 관련 기능
    │   ├── controller/                 # 그룹 컨트롤러
    │   ├── service/                    # 그룹 서비스
    │   ├── repository/                 # 그룹 리포지토리
    │   ├── entity/                     # 그룹 엔티티
    │   └── dto/                        # 그룹 DTO
    ├── search/                         # 검색 관련 기능
    │   ├── controller/                 # 검색 컨트롤러
    │   ├── service/                    # 검색 서비스
    │   └── dto/                        # 검색 DTO
    └── recommendation/                 # AI 추천 관련 기능
        ├── controller/                 # 추천 컨트롤러
        ├── service/                    # 추천 서비스
        ├── repository/                 # 추천 리포지토리
        ├── entity/                     # 추천 엔티티
        └── dto/                        # 추천 DTO
```

# 배포 및 운영

## AWS 인프라 구성

### 1. 컴퓨팅 리소스
- **EC2**: t3.medium (백엔드 서버)
- **RDS**: MySQL 8.0 (db.t3.micro)
- **S3**: 이미지 및 파일 저장소

### 2. 네트워킹
- **VPC**: 사용자 정의 네트워크
- **Security Groups**: 포트 8080 (HTTP), 22 (SSH) 개방
- **Route53**: 도메인 관리 (선택사항)

### 3. 모니터링
- **CloudWatch**: 로그 및 메트릭 수집
- **Spring Actuator**: 헬스 체크 및 메트릭

## Docker 배포

### Dockerfile
```dockerfile
FROM openjdk:17-jdk
ARG JAR_FILE=demo/build/libs/demo-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-Dspring.config.additional-location=optional:file:/config/", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    volumes:
      - ./config:/config
    depends_on:
      - mysql
      
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: travelonna
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

# AI 추천 시스템

## 추천 알고리즘
- **ALS (Alternating Least Squares)** 협업 필터링
- **하이브리드 모델**: 협업 필터링 + 인기도 기반
- **가중치 시스템**: POST(5.0) > LIKE(4.0) > COMMENT(3.0) > VIEW(1.0)

## 배치 처리 스케줄
- **전체 배치**: 매일 새벽 2:00 AM (전체 모델 재학습)
- **증분 배치**: 6시간마다 (신규 데이터만 처리)

## 성능 최적화
- **Sparse Matrix**: 메모리 사용량 90% 절감
- **BLAS 라이브러리**: 행렬 연산 속도 향상
- **병렬 처리**: 멀티프로세싱 활용

# 테스트

```bash
# 모든 테스트 실행
./gradlew test

# 특정 클래스 테스트
./gradlew test --tests UserServiceTest

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

# 모니터링

## Spring Actuator 엔드포인트

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# 메트릭 조회  
curl http://localhost:8080/actuator/metrics

# 데이터베이스 연결 상태
curl http://localhost:8080/actuator/health/db
```

## 주요 성능 지표

| 메트릭 | 목표값 | 현재값 |
|--------|--------|--------|
| **API 응답시간** | < 200ms | 150ms |
| **데이터베이스 연결** | > 95% | 99.2% |
| **메모리 사용률** | < 80% | 65% |
| **추천 정확도** | > 75% | 78.5% |

# 향후 확장 계획

## 1. 기능 확장
- [ ] **실시간 채팅**: WebSocket 기반 그룹 채팅
- [ ] **지도 통합**: Kakao Map API 연동
- [ ] **날씨 정보**: OpenWeatherMap API 연동
- [ ] **번역 서비스**: Google Translate API 연동

## 2. 성능 최적화
- [ ] **Redis 캐싱**: 추천 결과 및 세션 캐싱
- [ ] **CDN 도입**: 이미지 및 정적 파일 최적화
- [ ] **데이터베이스 샤딩**: 대용량 사용자 지원
- [ ] **읽기 전용 복제본**: 읽기 성능 향상

## 3. AI/ML 고도화
- [ ] **딥러닝 모델**: Transformer 기반 추천
- [ ] **실시간 추천**: 온라인 학습 모델
- [ ] **멀티모달**: 텍스트 + 이미지 분석
- [ ] **개인화 강화**: 컨텍스트 인식 추천

## 4. 운영 개선
- [ ] **CI/CD 파이프라인**: Jenkins/GitHub Actions
- [ ] **모니터링 강화**: Prometheus + Grafana
- [ ] **로그 중앙화**: ELK Stack 도입
- [ ] **A/B 테스팅**: 기능 실험 플랫폼

# 기여 가이드

## 개발 환경 설정

1. **Fork** 저장소를 자신의 계정으로 복사
2. **Clone** 복사된 저장소를 로컬에 다운로드
3. **Branch** 기능별 브랜치 생성 (`feature/기능명`)
4. **Commit** 의미 있는 커밋 메시지 작성
5. **Push** 변경사항을 원격 저장소에 업로드
6. **Pull Request** 원본 저장소로 병합 요청

## 코딩 컨벤션

### 커밋 메시지 규칙
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포매팅, 세미콜론 누락 등
refactor: 코드 리팩토링
test: 테스트 코드 추가
chore: 빌드 업무 수정, 패키지 매니저 설정 등

예시:
feat: 사용자 프로필 수정 API 추가
fix: JWT 토큰 만료 시 처리 로직 수정
docs: API 문서 업데이트
```

# 라이선스

이 프로젝트는 **MIT License** 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

# 문의 및 지원

## 📧 Contact
- **이메일**: kimmin5@example.com
- **GitHub Issues**: [이슈 등록](https://github.com/KIMMIN5/TravelOnNa_BackEnd/issues)
- **Wiki**: [프로젝트 위키](https://github.com/KIMMIN5/TravelOnNa_BackEnd/wiki)

## 🤝 Support
프로젝트에 대한 질문이나 제안사항이 있으시면 언제든지 연락해 주세요!

---

**Built with ❤️ by TravelOnNa Team**