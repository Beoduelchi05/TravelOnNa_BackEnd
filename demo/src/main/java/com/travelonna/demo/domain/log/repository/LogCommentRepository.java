package com.travelonna.demo.domain.log.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.log.entity.LogComment;

@Repository
public interface LogCommentRepository extends JpaRepository<LogComment, Integer> {
    
    // 특정 기록의 최상위 댓글 조회
    @Query("SELECT c FROM LogComment c WHERE c.log.logId = :logId AND c.parent IS NULL ORDER BY c.createdAt")
    List<LogComment> findTopLevelCommentsByLogId(@Param("logId") Integer logId);
    
    // 특정 댓글의 답글 조회
    List<LogComment> findByParentLocoIdOrderByCreatedAt(Integer parentId);
    
    // 특정 사용자의 댓글 조회
    List<LogComment> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    
    // 특정 기록의 모든 댓글 조회 (부모-자식 관계 유지)
    @Query("SELECT DISTINCT c FROM LogComment c LEFT JOIN FETCH c.children WHERE c.log.logId = :logId AND c.parent IS NULL ORDER BY c.createdAt")
    List<LogComment> findCommentsByLogIdWithReplies(@Param("logId") Integer logId);
} 