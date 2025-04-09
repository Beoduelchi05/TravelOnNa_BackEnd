package com.travelonna.demo.domain.log.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.log.entity.LogImage;

@Repository
public interface LogImageRepository extends JpaRepository<LogImage, Integer> {
    
    // 특정 기록의 모든 이미지 조회 (순서대로)
    List<LogImage> findByLogLogIdOrderByOrderNum(Integer logId);
    
    // 특정 기록의 모든 이미지 삭제
    void deleteByLogLogId(Integer logId);
} 