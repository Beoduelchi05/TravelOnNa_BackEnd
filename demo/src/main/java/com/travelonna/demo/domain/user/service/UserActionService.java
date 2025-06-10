package com.travelonna.demo.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.user.entity.UserAction;
import com.travelonna.demo.domain.user.entity.UserAction.ActionType;
import com.travelonna.demo.domain.user.entity.UserAction.TargetType;
import com.travelonna.demo.domain.user.repository.UserActionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionService {

    private final UserActionRepository userActionRepository;

    /**
     * 사용자 액션 기록
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAction(Integer userId, Integer targetId, ActionType actionType, TargetType targetType) {
        try {
            UserAction userAction = UserAction.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .actionType(actionType)
                    .targetType(targetType)
                    .build();

            userActionRepository.save(userAction);
            
            log.debug("사용자 액션 기록: userId={}, targetId={}, actionType={}, targetType={}", 
                     userId, targetId, actionType, targetType);
                     
        } catch (Exception e) {
            log.error("사용자 액션 기록 실패: userId={}, targetId={}, actionType={}, targetType={}", 
                     userId, targetId, actionType, targetType, e);
        }
    }

    /**
     * 여행 기록 작성 액션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogCreation(Integer userId, Integer logId) {
        recordAction(userId, logId, ActionType.POST, TargetType.LOG);
    }

    /**
     * 좋아요 액션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLike(Integer userId, Integer logId) {
        recordAction(userId, logId, ActionType.LIKE, TargetType.LOG);
    }

    /**
     * 댓글 작성 액션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordComment(Integer userId, Integer logId) {
        recordAction(userId, logId, ActionType.COMMENT, TargetType.LOG);
    }

    /**
     * 조회 액션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordView(Integer userId, Integer targetId, TargetType targetType) {
        // 중복 조회 방지: 같은 사용자가 같은 대상을 최근 1시간 내에 조회한 경우 기록하지 않음
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<UserAction> recentViews = userActionRepository.findByUserIdAndActionTimeAfterOrderByActionTimeDesc(
            userId, oneHourAgo);
        
        boolean hasRecentView = recentViews.stream()
            .anyMatch(action -> 
                action.getTargetId().equals(targetId) && 
                action.getTargetType() == targetType &&
                action.getActionType() == ActionType.VIEW);
        
        if (!hasRecentView) {
            recordAction(userId, targetId, ActionType.VIEW, targetType);
        }
    }

    /**
     * 여행 계획 작성 액션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPlanCreation(Integer userId, Integer planId) {
        recordAction(userId, planId, ActionType.POST, TargetType.PLAN);
    }

    /**
     * 장소 조회 액션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPlaceView(Integer userId, Integer placeId) {
        recordView(userId, placeId, TargetType.PLACE);
    }

    /**
     * 특정 사용자의 액션 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<UserAction> getUserActionHistory(Integer userId) {
        return userActionRepository.findByUserIdOrderByActionTimeDesc(userId);
    }

    /**
     * 특정 사용자의 최근 액션 조회 (추천 시스템용)
     */
    @Transactional(readOnly = true)
    public List<UserAction> getRecentUserActions(Integer userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return userActionRepository.findByUserIdAndActionTimeAfterOrderByActionTimeDesc(userId, since);
    }

    /**
     * 추천 시스템용 전체 사용자 액션 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<UserAction> getRecentActionsForRecommendations() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        return userActionRepository.findRecentActionsForRecommendations(sixMonthsAgo);
    }
} 