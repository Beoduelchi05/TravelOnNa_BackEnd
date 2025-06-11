package com.travelonna.demo.domain.plan.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.plan.entity.MyMap;
import com.travelonna.demo.domain.plan.repository.MyMapRepository;
import com.travelonna.demo.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/mymap")
@Tag(name = "MyMap", description = "지역별 로그 매핑 관리 API")
public class MyMapController {
    
    private static final Logger logger = LoggerFactory.getLogger(MyMapController.class);
    
    @Autowired
    private MyMapRepository myMapRepository;
    
    @Operation(summary = "사용자별 MyMap 조회", description = "특정 사용자의 모든 MyMap 데이터를 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyMapsByUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Integer userId) {
        
        logger.info("사용자별 MyMap 조회 시작: userId={}", userId);
        
        try {
            // 전체 MyMap 개수 먼저 확인
            long totalCount = myMapRepository.count();
            logger.info("전체 MyMap 개수: {}", totalCount);
            
            List<MyMap> myMaps = myMapRepository.findByUserUserId(userId);
            logger.info("사용자 {}의 MyMap 개수: {}", userId, myMaps.size());
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("userMyMapCount", myMaps.size());
            result.put("totalMyMapCount", totalCount);
            
            if (myMaps.isEmpty()) {
                result.put("message", "해당 사용자의 MyMap 데이터가 없습니다");
                result.put("data", new ArrayList<>());
            } else {
                result.put("message", "사용자별 MyMap 조회 성공");
                
                // 간단한 데이터 정보만 포함
                List<Map<String, Object>> simpleData = new ArrayList<>();
                for (int i = 0; i < myMaps.size(); i++) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("index", i);
                    item.put("hasData", "MyMap 객체 존재");
                    simpleData.add(item);
                }
                result.put("data", simpleData);
            }
            
            return ResponseEntity.ok(ApiResponse.success("사용자별 MyMap 조회 완료", result));
            
        } catch (Exception e) {
            logger.error("사용자별 MyMap 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("userId", userId);
            errorResult.put("error", e.getMessage());
            errorResult.put("userMyMapCount", 0);
            
            return ResponseEntity.ok(ApiResponse.success("사용자별 MyMap 조회 오류", errorResult));
        }
    }
} 