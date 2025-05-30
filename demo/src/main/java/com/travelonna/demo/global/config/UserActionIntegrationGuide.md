# UserAction 통합 가이드

## 📋 개요
기존 서비스들에 UserAction 추적 기능을 통합하여 추천 시스템이 사용자 행동 데이터를 수집할 수 있도록 합니다.

## 🔧 통합 방법

### 1. LogService 수정

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogService {
    
    // 기존 필드들...
    private final UserActionService userActionService; // 추가
    
    // 기록 작성 시 액션 추가
    @Transactional
    public LogResponseDto createLog(Integer userId, LogRequestDto requestDto) {
        // 기존 로직...
        LogResponseDto result = // 기록 생성 로직
        
        // UserAction 기록 추가
        userActionService.recordLogCreation(userId, result.getLogId());
        
        return result;
    }
    
    // 기록 조회 시 VIEW 액션 추가
    public LogResponseDto getLog(Integer logId, Integer userId) {
        // 기존 로직...
        LogResponseDto result = // 기록 조회 로직
        
        // 사용자가 로그인한 경우에만 VIEW 액션 기록
        if (userId != null) {
            userActionService.recordView(userId, logId, TargetType.LOG);
        }
        
        return result;
    }
    
    // 좋아요 토글 시 LIKE 액션 추가
    @Transactional
    public boolean toggleLike(Integer logId, Integer userId) {
        // 기존 로직...
        boolean isLiked = // 좋아요 토글 로직
        
        // 좋아요가 추가된 경우에만 LIKE 액션 기록
        if (isLiked) {
            userActionService.recordLike(userId, logId);
        }
        
        return isLiked;
    }
}
```

### 2. LogCommentService 수정

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogCommentService {
    
    // 기존 필드들...
    private final UserActionService userActionService; // 추가
    
    // 댓글 생성 시 COMMENT 액션 추가
    @Transactional
    public LogCommentResponseDto createComment(Integer logId, Integer userId, LogCommentRequestDto requestDto) {
        // 기존 로직...
        LogCommentResponseDto result = // 댓글 생성 로직
        
        // UserAction 기록 추가
        userActionService.recordComment(userId, logId);
        
        return result;
    }
}
```

### 3. PlanService 수정 (필요시)

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {
    
    // 기존 필드들...
    private final UserActionService userActionService; // 추가
    
    // 계획 생성 시 POST 액션 추가
    @Transactional
    public PlanResponseDto createPlan(Integer userId, PlanRequestDto requestDto) {
        // 기존 로직...
        PlanResponseDto result = // 계획 생성 로직
        
        // UserAction 기록 추가
        userActionService.recordPlanCreation(userId, result.getPlanId());
        
        return result;
    }
    
    // 계획 조회 시 VIEW 액션 추가
    public PlanResponseDto getPlan(Integer planId, Integer userId) {
        // 기존 로직...
        PlanResponseDto result = // 계획 조회 로직
        
        // 사용자가 로그인한 경우에만 VIEW 액션 기록
        if (userId != null) {
            userActionService.recordView(userId, planId, TargetType.PLAN);
        }
        
        return result;
    }
}
```

## 🗄️ 데이터베이스 테이블 생성

```sql
CREATE TABLE user_actions (
    action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    target_id INT NOT NULL,
    action_type ENUM('POST', 'LIKE', 'COMMENT', 'VIEW') NOT NULL,
    target_type ENUM('LOG', 'PLACE', 'PLAN') NOT NULL,
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_target_id_type (target_id, target_type),
    INDEX idx_action_time (action_time DESC),
    INDEX idx_user_recent (user_id, action_time DESC),
    
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);
```

## 📊 추천 시스템 연동

### UserAction 데이터 조회 API (추천 서비스용)

```java
@RestController
@RequestMapping("/api/v1/user-actions")
public class UserActionController {
    
    @Autowired
    private UserActionService userActionService;
    
    @GetMapping("/recent")
    public ResponseEntity<List<UserAction>> getRecentActions() {
        List<UserAction> actions = userActionService.getRecentActionsForRecommendations();
        return ResponseEntity.ok(actions);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAction>> getUserActions(@PathVariable Integer userId) {
        List<UserAction> actions = userActionService.getUserActionHistory(userId);
        return ResponseEntity.ok(actions);
    }
}
```

## 🔄 기존 데이터 마이그레이션

### 기존 좋아요/댓글 데이터를 user_actions로 마이그레이션

```sql
-- 기존 좋아요 데이터 마이그레이션
INSERT INTO user_actions (user_id, target_id, action_type, target_type, action_time)
SELECT 
    l.user_id, 
    l.log_id, 
    'LIKE', 
    'LOG',
    NOW() -- 정확한 시간이 없으므로 현재 시간 사용
FROM likes l;

-- 기존 댓글 데이터 마이그레이션
INSERT INTO user_actions (user_id, target_id, action_type, target_type, action_time)
SELECT 
    lc.user_id, 
    lc.log_id, 
    'COMMENT', 
    'LOG',
    lc.created_at
FROM log_comment lc;

-- 기존 게시물 작성 데이터 마이그레이션
INSERT INTO user_actions (user_id, target_id, action_type, target_type, action_time)
SELECT 
    l.user_id, 
    l.log_id, 
    'POST', 
    'LOG',
    l.created_at
FROM log l;
```

## ⚡ 성능 최적화

### 배치 처리로 대량 삽입
```java
@Service
public class UserActionBatchService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Transactional
    public void batchInsertActions(List<UserAction> actions) {
        String sql = "INSERT INTO user_actions (user_id, target_id, action_type, target_type, action_time) VALUES (?, ?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, actions, actions.size(),
            (PreparedStatement ps, UserAction action) -> {
                ps.setInt(1, action.getUserId());
                ps.setInt(2, action.getTargetId());
                ps.setString(3, action.getActionType().name());
                ps.setString(4, action.getTargetType().name());
                ps.setTimestamp(5, Timestamp.valueOf(action.getActionTime()));
            });
    }
}
```

## 🚨 주의사항

1. **중복 방지**: VIEW 액션은 1시간 내 중복 기록 방지
2. **비동기 처리**: 대량 트래픽 시 비동기로 처리 고려
3. **데이터 정리**: 오래된 액션 데이터 정기적 정리
4. **인덱스 관리**: 쿼리 성능을 위한 적절한 인덱스 설정 