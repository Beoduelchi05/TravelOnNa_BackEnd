package com.travelonna.demo.domain.log.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.log.entity.Likes;
import com.travelonna.demo.domain.log.entity.LikesId;

@Repository
public interface LikesRepository extends JpaRepository<Likes, LikesId> {
    
    // 특정 기록의 좋아요 개수 조회
    long countByLogLogId(Integer logId);
    
    // 특정 사용자가 특정 기록에 좋아요를 눌렀는지 확인
    Optional<Likes> findByLogLogIdAndUserUserId(Integer logId, Integer userId);
    
    // 특정 사용자가 좋아요 한 모든 기록 ID 조회
    @Query("SELECT l.log.logId FROM Likes l WHERE l.user.userId = :userId")
    List<Integer> findLogIdsByUserUserId(@Param("userId") Integer userId);
    
    // 특정 기록의 모든 좋아요 삭제
    void deleteByLogLogId(Integer logId);
    
    // 특정 사용자의 모든 좋아요 삭제
    void deleteByUserUserId(Integer userId);
} 