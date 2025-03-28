package com.travelonna.demo.domain.plan.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.plan.entity.Plan;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Integer> {
    
    List<Place> findByPlanOrderByOrder(Plan plan);
    
    @Query("SELECT p FROM Place p WHERE p.plan.planId = :planId ORDER BY p.order")
    List<Place> findByPlanIdOrderByOrder(@Param("planId") Integer planId);
    
    Optional<Place> findByPlaceIdAndPlan_PlanId(Integer placeId, Integer planId);
    
    @Query("SELECT MAX(p.order) FROM Place p WHERE p.plan.planId = :planId")
    Integer findMaxOrderByPlanId(@Param("planId") Integer planId);
    
    // 특정 일차에 대한 최대 순서 조회
    @Query("SELECT MAX(p.order) FROM Place p WHERE p.plan.planId = :planId " +
           "AND FUNCTION('DATEDIFF', p.visitDate, p.plan.startDate) = :dayNumber - 1")
    Integer findMaxOrderByPlanIdAndDay(@Param("planId") Integer planId, @Param("dayNumber") Integer dayNumber);
    
    // 특정 일차의 모든 장소 조회
    @Query("SELECT p FROM Place p WHERE p.plan.planId = :planId " +
           "AND FUNCTION('DATEDIFF', p.visitDate, p.plan.startDate) = :dayNumber - 1 " +
           "ORDER BY p.order")
    List<Place> findByPlanIdAndDayOrderByOrder(@Param("planId") Integer planId, @Param("dayNumber") Integer dayNumber);
    
    // 특정 일차를 계산하여 필터링하는 네이티브 쿼리 (MySQL 기준)
    @Query(value = "SELECT p.* FROM place p JOIN plan pl ON p.plan_id = pl.plan_id " +
                  "WHERE p.plan_id = :planId " +
                  "AND DATEDIFF(p.visit_date, pl.start_date) = :dayNumber - 1 " +
                  "ORDER BY p.p_order", nativeQuery = true)
    List<Place> findByPlanIdAndDayNative(@Param("planId") Integer planId, @Param("dayNumber") Integer dayNumber);
} 