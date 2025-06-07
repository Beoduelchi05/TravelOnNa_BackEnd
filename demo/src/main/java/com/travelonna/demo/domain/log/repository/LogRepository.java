package com.travelonna.demo.domain.log.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.log.entity.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    
    // 사용자별 기록 조회
    @Query("SELECT l FROM Log l LEFT JOIN FETCH l.place WHERE l.user.userId = :userId ORDER BY l.createdAt DESC")
    List<Log> findByUserUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    // 일정별 기록 조회
    @Query("SELECT l FROM Log l LEFT JOIN FETCH l.place WHERE l.plan.planId = :planId ORDER BY l.createdAt DESC")
    List<Log> findByPlanPlanIdOrderByCreatedAtDesc(@Param("planId") Integer planId);
    
    // 공개된 모든 기록 조회
    @Query("SELECT l FROM Log l LEFT JOIN FETCH l.place WHERE l.isPublic = true ORDER BY l.createdAt DESC")
    List<Log> findByIsPublicTrueOrderByCreatedAtDesc();
    
    // 특정 장소별 기록 조회 - 해당 장소에 직접 연결된 기록들만 조회
    @Query("SELECT l FROM Log l LEFT JOIN FETCH l.place " +
           "WHERE l.place.placeId = :placeId " +
           "ORDER BY l.createdAt DESC")
    List<Log> findByPlacePlaceIdOrderByCreatedAtDesc(@Param("placeId") Integer placeId);
    
    // 팔로잉 사용자의 공개 기록 조회
    @Query("SELECT l FROM Log l LEFT JOIN FETCH l.place WHERE l.user.userId IN :userIds AND l.isPublic = true ORDER BY l.createdAt DESC")
    List<Log> findByUserUserIdInAndIsPublicTrueOrderByCreatedAtDesc(@Param("userIds") List<Integer> userIds);
    
    // ID로 기록 상세 조회 (이미지, 댓글, 좋아요, 장소 포함)
    @Query("SELECT DISTINCT l FROM Log l " +
           "LEFT JOIN FETCH l.images " +
           "LEFT JOIN FETCH l.place " +
           "WHERE l.logId = :logId")
    Optional<Log> findByIdWithDetails(@Param("logId") Integer logId);
} 