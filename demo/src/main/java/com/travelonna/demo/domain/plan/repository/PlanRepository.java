package com.travelonna.demo.domain.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.plan.entity.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {
    
    // 사용자 ID로 일정 목록 조회
    List<Plan> findByUserId(Integer userId);
    
    // 그룹 ID로 일정 목록 조회
    List<Plan> findByGroupId(Integer groupId);
    
    // 일정 ID와 사용자 ID로 일정 조회 (권한 확인용)
    Optional<Plan> findByPlanIdAndUserId(Integer planId, Integer userId);
    
    // 사용자가 그룹 멤버로 참여한 그룹의 모든 일정 조회
    @Query("SELECT p FROM Plan p " +
           "WHERE p.groupId IN (" +
           "    SELECT gm.group.id FROM GroupMember gm " +
           "    WHERE gm.user.userId = :userId AND gm.isActive = true" +
           ")")
    List<Plan> findPlansWhereUserIsGroupMember(@Param("userId") Integer userId);
} 