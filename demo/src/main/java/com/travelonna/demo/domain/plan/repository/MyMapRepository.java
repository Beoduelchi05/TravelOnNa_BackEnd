package com.travelonna.demo.domain.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.plan.entity.MyMap;

@Repository
public interface MyMapRepository extends JpaRepository<MyMap, Integer> {
    
    // 로그 ID로 MyMap 조회
    Optional<MyMap> findByLogLogId(Integer logId);
    
    // 사용자 ID로 MyMap 목록 조회
    List<MyMap> findByUserUserId(Integer userId);
    
    // 로그 ID로 MyMap 삭제
    void deleteByLogLogId(Integer logId);
    
    // 사용자 ID와 로그 ID로 조회
    Optional<MyMap> findByUserUserIdAndLogLogId(Integer userId, Integer logId);
    
    // 지역 코드로 MyMap 목록 조회
    @Query("SELECT mm FROM MyMap mm WHERE mm.mapCode.mapCodeId = :mapCodeId")
    List<MyMap> findByMapCodeId(@Param("mapCodeId") Long mapCodeId);
} 