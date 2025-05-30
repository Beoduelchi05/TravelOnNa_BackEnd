# TravelOnNa 추천 API 구현 가이드

## 개요

TravelOnNa 백엔드에 AI 기반 개인화 추천 API가 추가되었습니다. 이 API는 Python 추천 서비스에서 생성된 `recommendations` 테이블의 데이터를 조회하여 사용자에게 맞춤형 여행 기록을 제공합니다.

## API 명세

### GET /api/v1/recommendations

개인화된 추천 목록을 조회합니다.

**쿼리 파라미터:**
- `userId` (필수): 사용자 ID
- `type` (선택): 추천 타입 (기본값: "log", 현재 "log"만 지원)
- `limit` (선택): 조회할 추천 개수 (기본값: 20, 최대: 50)

**응답 예시:**
```json
{
  "success": true,
  "message": "개인화 추천 목록을 성공적으로 조회했습니다 (3건)",
  "data": {
    "userId": 123,
    "itemType": "log",
    "recommendations": [
      {
        "itemId": 456,
        "score": 0.92,
        "logId": 456,
        "userId": 88,
        "planId": 1003,
        "comment": "수원화성 야경 정말 최고였어요!",
        "createdAt": "2025-05-26 14:03:22",
        "isPublic": true
      },
      {
        "itemId": 789,
        "score": 0.85,
        "logId": 789,
        "userId": 57,
        "planId": 998,
        "comment": "제주 오설록 티뮤지엄에서 힐링~",
        "createdAt": "2025-04-11 09:41:07",
        "isPublic": true
      }
    ]
  }
}
```

### GET /api/v1/recommendations/exists

추천 데이터 존재 여부를 확인합니다.

**쿼리 파라미터:**
- `userId` (필수): 사용자 ID
- `type` (선택): 추천 타입 (기본값: "log")

### GET /api/v1/recommendations/count

추천 데이터 개수를 조회합니다.

**쿼리 파라미터:**
- `userId` (필수): 사용자 ID
- `type` (선택): 추천 타입 (기본값: "log")

## 구현된 컴포넌트

### 1. 엔티티
- `Recommendation`: recommendations 테이블 매핑
- `ItemType` enum: log, place, plan
- `AlgorithmType` enum: als, popularity, hybrid

### 2. Repository
- `RecommendationRepository`: JPA 리포지토리
- `RecommendationProjection`: 조인 쿼리 결과 매핑

### 3. Service
- `RecommendationService`: 비즈니스 로직 처리

### 4. Controller
- `RecommendationController`: REST API 엔드포인트

### 5. DTO
- `RecommendationResponseDto`: API 응답 구조

## 데이터베이스 요구사항

### 필수 테이블 생성
```sql
-- 1. user_actions 테이블 (사용자 행동 추적)
-- 2. recommendations 테이블 (추천 결과)
-- 3. recommendation_batch_logs 테이블 (배치 로그)
```

**📁 실행 방법:**
```bash
mysql -u [username] -p [database] < src/main/resources/sql/create_recommendation_tables.sql
```

## 추천 시스템 아키텍처

```
사용자 행동 → user_actions 테이블 
     ↓
Python AI 서비스 (배치 처리)
     ↓  
recommendations 테이블
     ↓
Spring Boot API → 프론트엔드
```

### 1. 데이터 수집
- 여행 기록 생성/조회/좋아요/댓글 시 `user_actions` 테이블에 자동 기록
- 공개 기록에 대해서만 추적 (개인정보 보호)

### 2. 추천 생성
- **전체 배치**: 매일 새벽 2시
- **증분 배치**: 6시간마다
- **알고리즘**: ALS + 인기도 하이브리드

### 3. 추천 제공
- `recommendations` 테이블에서 실시간 조회
- `log` 테이블과 JOIN하여 상세 정보 포함

## 에러 처리

### 주요 예외 상황
- **사용자 없음**: 404 ResourceNotFoundException
- **지원하지 않는 타입**: 400 IllegalArgumentException
- **잘못된 limit 값**: 400 IllegalArgumentException
- **추천 데이터 없음**: 빈 배열 반환 (정상 응답)

### 폴백 정책
- 추천 데이터가 없는 신규 사용자: 빈 배열 반환
- 향후 인기 기록 또는 랜덤 추천으로 확장 가능

## 성능 최적화

### 데이터베이스 인덱스
- `(user_id, rank_position)`: 사용자별 순위 조회
- `(user_id, item_type)`: 타입별 필터링
- `(score DESC)`: 점수순 정렬

### 캐싱 전략
- 추천 결과는 배치 처리로 미리 계산되어 있음
- 실시간 조회는 단순 SELECT만 수행

## Swagger 문서

추천 API는 Swagger UI에서 확인 가능합니다:
- URL: `http://localhost:8080/swagger-ui/index.html`
- 태그: "추천" - AI 기반 개인화 추천 API

## 배포 체크리스트

### 데이터베이스
- [ ] `user_actions` 테이블 생성
- [ ] `recommendations` 테이블 생성  
- [ ] `recommendation_batch_logs` 테이블 생성
- [ ] 기존 데이터 마이그레이션 (선택)

### 백엔드
- [ ] Spring Boot 재시작
- [ ] API 테스트 (`/api/v1/recommendations?userId=1`)
- [ ] Swagger 문서 확인

### Python 추천 서비스
- [ ] 배치 서비스 배포
- [ ] 초기 배치 실행
- [ ] 스케줄러 설정

## 모니터링

### 확인 방법
```sql
-- 추천 데이터 현황
SELECT 
    item_type,
    algorithm_type,
    COUNT(*) as total_recommendations,
    COUNT(DISTINCT user_id) as unique_users,
    AVG(score) as avg_score
FROM recommendations 
GROUP BY item_type, algorithm_type;

-- 최근 배치 처리 로그
SELECT * FROM recommendation_batch_logs 
ORDER BY start_time DESC LIMIT 5;
```

### 성능 메트릭
- API 응답 시간
- 추천 데이터 생성률  
- 사용자 커버리지
- 배치 처리 성공률

## 향후 확장

### 추가 기능
- [ ] 장소 추천 (`type=place`)
- [ ] 일정 추천 (`type=plan`)
- [ ] 실시간 추천 갱신
- [ ] A/B 테스트 프레임워크

### 알고리즘 개선
- [ ] 딥러닝 모델 적용
- [ ] 콘텐츠 기반 필터링 추가
- [ ] 지역별/시즌별 가중치 적용 