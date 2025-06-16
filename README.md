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
<td><a href="https://github.com/KIMMIN5" target="_blank">김민오</a></td>
<td>BE, DevOps, AI/ML</td>
<td>백엔드 아키텍처 설계, OAuth2 인증<br>
AWS 인프라 구축, Docker 컨테이너화, CI/CD 파이프라인 구성<br>
추천 시스템 개발, ALS 협업 필터링</td>
<td>
  <img src="https://img.shields.io/badge/Spring_Boot-6db33f?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white" alt="AWS">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white" alt="Jenkins">
  <img src="https://img.shields.io/badge/Ansible-EE0000?style=for-the-badge&logo=ansible&logoColor=white" alt="Ansible">
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white" alt="Kubernetes">
  <img src="https://img.shields.io/badge/PyTorch-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white" alt="PyTorch">
</td>
</tr>
<tr>
<td><a href="https://github.com/ehrbs" target="_blank">윤도균</a></td>
<td>BE</td>
<td>프로필, 일정, 나만의지도</td>
<td>
  <img src="https://img.shields.io/badge/Spring_Boot-6db33f?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
</td>
</tr>
<tr>
<td><a href="https://github.com/JungMin-E" target="_blank">이정민</a></td>
<td>BE</td>
<td>그룹, 기록</td>
<td>
  <img src="https://img.shields.io/badge/Spring_Boot-6db33f?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
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

<img src="/images/architecture.png">

## 아키텍처 설계 원칙

### 1. 도메인 중심 설계 (Domain-Driven Design)
- **백엔드를 Auth, User, Follow, Plan, Group, Log, Search, Recommend 도메인으로 분리**
- **모듈 간 통신은 API Layer를 통해서만 허용**

### 2. 마이크로서비스 아키텍처
- **Main Server ↔ AI/ML Server 독립 운영**
- **Docker + Kubernetes 기반 컨테이너 오케스트레이션**
- **서비스별 독립적인 확장 및 배포**

### 3. CI/CD 자동화
- **GitHub → Jenkins → Docker Hub → Kubernetes 파이프라인**
- **멀티 아키텍처(amd64/arm64) Docker 이미지 빌드**
- **Ansible + kubectl을 통한 무중단 Rolling Update 배포**

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

**Authentication & Security**

[![OAuth2](https://img.shields.io/badge/OAuth2.0-4285F4?style=for-the-badge&logo=google&logoColor=white)]()
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)]()
[![Google API](https://img.shields.io/badge/Google_API-4285F4?style=for-the-badge&logo=google&logoColor=white)]()

**DevOps & Infrastructure**

[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)]()
[![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)]()
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)]()
[![Ansible](https://img.shields.io/badge/Ansible-EE0000?style=for-the-badge&logo=ansible&logoColor=white)]()

**Documentation & Testing**

[![Swagger](https://img.shields.io/badge/Swagger-85ea2d?style=for-the-badge&logo=swagger&logoColor=black)]()
[![JUnit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=java&logoColor=white)]()
[![Spring Boot Test](https://img.shields.io/badge/Spring_Boot_Test-6db33f?style=for-the-badge&logo=spring&logoColor=white)]()

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

# 문의 및 지원

## 📧 Contact
- **이메일**: beoduelchi05@gmail.com
- **GitHub Issues**: [이슈 등록](https://github.com/Beoduelchi05/TravelOnNa_BackEnd/issues)

## 🤝 Support
프로젝트에 대한 질문이나 제안사항이 있으시면 언제든지 연락해 주세요!

---

**Built with ❤️ by Beoduelchi Team**