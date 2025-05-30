# TravelOnNa UserAction & 추천 시스템 API 문서

## 개요

TravelOnNa 백엔드에 사용자 행동 추적 시스템이 추가되었습니다. 이 시스템은 사용자의 **공개 여행 기록**에 대한 행동을 자동으로 수집하여 개인화된 추천 시스템에 활용합니다.

**⚠️ 개인정보 보호**: 비공개 기록(`is_public=0`)에 대한 사용자 행동은 추적되지 않습니다.

## 자동 추적되는 사용자 행동

### 1. POST 액션 (여행 기록 생성)
- **API**: `POST /api/v1/logs`
- **기록 시점**: 여행 기록 생성 성공 시 (공개/비공개 구분 없음)
- **기록 내용**: `user_id`, `target_id(log_id)`, `action_type='POST'`, `target_type='LOG'`

### 2. VIEW 액션 (여행 기록 조회)
- **API**: `GET /api/v1/logs/{logId}`
- **기록 시점**: **공개 여행 기록** 조회 시 (로그인한 사용자만)
- **중복 방지**: 동일 사용자가 1시간 내 같은 기록을 다시 조회해도 중복 기록되지 않음
- **기록 내용**: `user_id`, `target_id(log_id)`, `action_type='VIEW'`, `target_type='LOG'`

### 3. LIKE 액션 (좋아요 추가)
- **API**: `POST /api/v1/logs/{logId}/likes`
- **기록 시점**: **공개 여행 기록**에 좋아요 추가 시에만 (취소 시에는 기록되지 않음)
- **기록 내용**: `user_id`, `target_id(log_id)`, `action_type='LIKE'`, `target_type='LOG'`

### 4. COMMENT 액션 (댓글 작성)
- **API**: `POST /api/logs/{logId}/comments`
- **기록 시점**: **공개 여행 기록**에 댓글/대댓글 생성 성공 시
- **기록 내용**: `user_id`, `target_id(log_id)`, `action_type='COMMENT'`, `target_type='LOG'`

## 데이터베이스 스키마

### user_actions 테이블
```sql
CREATE TABLE user_actions (
    action_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    target_id INT NOT NULL,
    action_type ENUM('POST', 'LIKE', 'COMMENT', 'VIEW') NOT NULL,
    target_type ENUM('LOG', 'PLACE', 'PLAN') NOT NULL,
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, action_time),
    INDEX idx_target (target_id, target_type),
    INDEX idx_action_type (action_type),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

### recommendations 테이블
```sql
CREATE TABLE recommendations (
    recommendation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    item_type ENUM('log', 'place', 'plan') DEFAULT 'log',
    score FLOAT NOT NULL,
    rank_position INT NOT NULL,
    algorithm_type ENUM('als', 'popularity', 'hybrid') DEFAULT 'als',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_rank (user_id, rank_position),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

## 추천 시스템 배치 처리

### 배치 스케줄
- **전체 배치**: 매일 새벽 2:00 AM (전체 모델 재학습)
- **증분 배치**: 6시간마다 (08:00, 14:00, 20:00) (신규 데이터만 처리)

### 추천 알고리즘
- **ALS (Alternating Least Squares)** 협업 필터링
- **설정**: 64 factors, 20 iterations, regularization 0.05
- **평점 가중치**: POST=5.0, LIKE=4.0, COMMENT=3.0, VIEW=1.0

### 폴백 전략
- 신규 사용자 또는 추천 데이터가 부족한 경우 인기도 기반 추천 제공

## API 응답 변경사항

기존 API의 응답 형태는 변경되지 않았으며, UserAction 기록은 내부적으로만 수행됩니다.

**오류 처리**: UserAction 기록 실패 시에도 메인 API 동작에는 영향을 주지 않으며, 경고 로그만 기록됩니다.

## Swagger 문서 업데이트

- 각 API 설명에 UserAction 자동 기록 정보 추가
- 추천 시스템 개요 및 배치 처리 정보 추가
- 사용자 행동 추적 시스템 설명 추가

## 개발팀 참고사항

1. **의존성**: UserActionService가 실패해도 메인 기능은 정상 동작
2. **로깅**: 액션 기록 실패 시 WARN 레벨로 로그 기록
3. **성능**: 비동기 처리를 통해 메인 API 응답 시간에 영향 최소화
4. **데이터 정합성**: 트랜잭션 롤백 시 UserAction도 함께 롤백됨 