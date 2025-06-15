package com.travelonna.demo.domain.recommendation.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.recommendation.entity.Recommendation;
import com.travelonna.demo.domain.recommendation.entity.Recommendation.ItemType;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {
    
    /**
     * 사용자의 추천 목록을 점수 순으로 조회 (Log 정보와 함께)
     */
    @Query("""
        SELECT r.itemId as itemId, r.score as score,
               l.logId as logId, l.user.userId as logUserId, l.plan.planId as planId,
               l.comment as comment, l.createdAt as createdAt, l.isPublic as isPublic
        FROM Recommendation r
        JOIN Log l ON r.itemId = l.logId
        WHERE r.user.userId = :userId 
          AND r.itemType = :itemType
          AND l.isPublic = true
        ORDER BY r.score DESC
        """)
    List<RecommendationProjection> findRecommendationsWithLogInfo(
        @Param("userId") Integer userId, 
        @Param("itemType") ItemType itemType);
    
    /**
     * 사용자의 추천 목록을 점수 순으로 조회 (페이지네이션 지원)
     */
    @Query("""
        SELECT r.itemId as itemId, r.score as score,
               l.logId as logId, l.user.userId as logUserId, l.plan.planId as planId,
               l.comment as comment, l.createdAt as createdAt, l.isPublic as isPublic
        FROM Recommendation r
        JOIN Log l ON r.itemId = l.logId
        WHERE r.user.userId = :userId 
          AND r.itemType = :itemType
          AND l.isPublic = true
        ORDER BY r.score DESC
        """)
    Page<RecommendationProjection> findRecommendationsWithLogInfoPaginated(
        @Param("userId") Integer userId, 
        @Param("itemType") ItemType itemType,
        Pageable pageable);
    
    /**
     * 사용자의 추천 목록을 점수 순으로 조회 (제한된 개수) - 하위 호환성을 위해 유지
     */
    @Query("""
        SELECT r.itemId as itemId, r.score as score,
               l.logId as logId, l.user.userId as logUserId, l.plan.planId as planId,
               l.comment as comment, l.createdAt as createdAt, l.isPublic as isPublic
        FROM Recommendation r
        JOIN Log l ON r.itemId = l.logId
        WHERE r.user.userId = :userId 
          AND r.itemType = :itemType
          AND l.isPublic = true
        ORDER BY r.score DESC
        LIMIT :limit
        """)
    List<RecommendationProjection> findRecommendationsWithLogInfoLimit(
        @Param("userId") Integer userId, 
        @Param("itemType") ItemType itemType,
        @Param("limit") Integer limit);
    
    /**
     * 사용자별 추천 존재 여부 확인
     */
    boolean existsByUserUserIdAndItemType(Integer userId, ItemType itemType);
    
    /**
     * 사용자별 특정 타입 추천 개수 조회
     */
    long countByUserUserIdAndItemType(Integer userId, ItemType itemType);
    
    /**
     * 사용자별 추천 삭제 (새 추천 생성 전)
     */
    void deleteByUserUserIdAndItemType(Integer userId, ItemType itemType);
} 