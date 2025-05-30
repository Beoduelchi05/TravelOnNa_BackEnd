package com.travelonna.demo.domain.user.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.user.entity.UserAction;
import com.travelonna.demo.domain.user.entity.UserAction.ActionType;
import com.travelonna.demo.domain.user.entity.UserAction.TargetType;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Integer> {
    
    // 특정 사용자의 액션 조회
    List<UserAction> findByUserIdOrderByActionTimeDesc(Integer userId);
    
    // 특정 기간 내 사용자 액션 조회
    List<UserAction> findByUserIdAndActionTimeAfterOrderByActionTimeDesc(
        Integer userId, LocalDateTime after);
    
    // 특정 사용자의 특정 타입 액션 조회
    List<UserAction> findByUserIdAndActionTypeOrderByActionTimeDesc(
        Integer userId, ActionType actionType);
    
    // 특정 대상에 대한 모든 액션 조회
    List<UserAction> findByTargetIdAndTargetTypeOrderByActionTimeDesc(
        Integer targetId, TargetType targetType);
    
    // 특정 사용자가 특정 대상에 대해 특정 액션을 했는지 확인
    boolean existsByUserIdAndTargetIdAndTargetTypeAndActionType(
        Integer userId, Integer targetId, TargetType targetType, ActionType actionType);
    
    // 추천 시스템용 데이터 조회 (최근 6개월)
    @Query("SELECT ua FROM UserAction ua " +
           "WHERE ua.actionTime >= :sixMonthsAgo " +
           "ORDER BY ua.actionTime DESC")
    List<UserAction> findRecentActionsForRecommendations(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);
    
    // 특정 사용자의 최근 활동 조회 (추천용)
    @Query("SELECT ua FROM UserAction ua " +
           "WHERE ua.userId = :userId " +
           "AND ua.actionTime >= :recentTime " +
           "ORDER BY ua.actionTime DESC")
    List<UserAction> findRecentActionsByUser(
        @Param("userId") Integer userId, 
        @Param("recentTime") LocalDateTime recentTime);
} 