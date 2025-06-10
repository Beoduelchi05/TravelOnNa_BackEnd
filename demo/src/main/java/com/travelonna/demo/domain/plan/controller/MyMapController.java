package com.travelonna.demo.domain.plan.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.plan.entity.MyMap;
import com.travelonna.demo.domain.plan.repository.MyMapRepository;
import com.travelonna.demo.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/mymap")
@RequiredArgsConstructor
@Tag(name = "MyMap", description = "지역별 로그 매핑 관리 API")
public class MyMapController {
    
    private final MyMapRepository myMapRepository;
    
    @Operation(summary = "사용자별 MyMap 조회", description = "특정 사용자의 모든 MyMap 데이터를 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<MyMap>>> getMyMapsByUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Integer userId) {
        
        log.info("사용자별 MyMap 조회: userId={}", userId);
        List<MyMap> myMaps = myMapRepository.findByUserUserId(userId);
        
        return ResponseEntity.ok(ApiResponse.success("사용자별 MyMap 조회 성공", myMaps));
    }
    
    @Operation(summary = "로그별 MyMap 조회", description = "특정 로그의 MyMap 데이터를 조회합니다.")
    @GetMapping("/log/{logId}")
    public ResponseEntity<ApiResponse<MyMap>> getMyMapByLog(
            @Parameter(description = "로그 ID", example = "1") @PathVariable Integer logId) {
        
        log.info("로그별 MyMap 조회: logId={}", logId);
        return myMapRepository.findByLogLogId(logId)
                .map(myMap -> ResponseEntity.ok(ApiResponse.success("로그별 MyMap 조회 성공", myMap)))
                .orElse(ResponseEntity.ok(ApiResponse.success("해당 로그의 MyMap 데이터가 없습니다.", null)));
    }
    
    @Operation(summary = "지역코드별 MyMap 조회", description = "특정 지역코드의 모든 MyMap 데이터를 조회합니다.")
    @GetMapping("/region/{mapCodeId}")
    public ResponseEntity<ApiResponse<List<MyMap>>> getMyMapsByRegion(
            @Parameter(description = "지역코드 ID", example = "1") @PathVariable Long mapCodeId) {
        
        log.info("지역코드별 MyMap 조회: mapCodeId={}", mapCodeId);
        List<MyMap> myMaps = myMapRepository.findByMapCodeId(mapCodeId);
        
        return ResponseEntity.ok(ApiResponse.success("지역코드별 MyMap 조회 성공", myMaps));
    }
    
    @Operation(summary = "전체 MyMap 조회", description = "모든 MyMap 데이터를 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MyMap>>> getAllMyMaps(
            @Parameter(description = "조회 개수 제한", example = "100") @RequestParam(defaultValue = "100") Integer limit) {
        
        log.info("전체 MyMap 조회: limit={}", limit);
        List<MyMap> myMaps = myMapRepository.findAll();
        
        // 제한 개수만큼만 반환
        if (myMaps.size() > limit) {
            myMaps = myMaps.subList(0, limit);
        }
        
        return ResponseEntity.ok(ApiResponse.success("전체 MyMap 조회 성공", myMaps));
    }
} 