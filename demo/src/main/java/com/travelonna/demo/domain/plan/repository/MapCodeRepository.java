package com.travelonna.demo.domain.plan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.plan.entity.MapCode;

@Repository
public interface MapCodeRepository extends JpaRepository<MapCode, Long> {
    
    // 도시와 구/군으로 지역 코드 조회
    Optional<MapCode> findByCityAndDistrict(String city, String district);
    
    // 도시만으로 조회 (구/군이 null인 경우)
    @Query("SELECT mc FROM MapCode mc WHERE mc.city = :city AND mc.district IS NULL")
    Optional<MapCode> findByCityOnly(@Param("city") String city);
    
    // 중복 체크
    boolean existsByCityAndDistrict(String city, String district);
} 