package com.travelonna.demo.domain.plan.repository;

import java.util.List;
import java.util.Optional;

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
} 