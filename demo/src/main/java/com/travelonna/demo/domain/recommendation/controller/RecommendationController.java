package com.travelonna.demo.domain.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.recommendation.dto.RecommendationResponseDto;
import com.travelonna.demo.domain.recommendation.service.RecommendationService;
import com.travelonna.demo.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "추천", description = "AI 기반 개인화 추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @Operation(
        summary = "개인화 추천 목록 조회", 
        description = "사용자의 행동 데이터를 기반으로 생성된 개인화 추천 목록을 조회합니다.\n\n" +
                     "**추천 알고리즘**: ALS(Alternating Least Squares) 협업 필터링과 인기도 기반 알고리즘을 결합한 하이브리드 방식\n\n" +
                     "**데이터 소스**: user_actions 테이블의 사용자 행동 데이터 (POST, LIKE, COMMENT, VIEW)\n\n" +
                     "**업데이트 주기**: 매일 새벽 2시 전체 배치, 6시간마다 증분 배치\n\n" +
                     "**현재 지원 타입**: log (여행 기록 추천)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "추천 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (지원하지 않는 타입 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<RecommendationResponseDto>> getRecommendations(
            @Parameter(description = "사용자 ID", required = true, example = "123")
            @RequestParam Integer userId,
            
            @Parameter(description = "추천 타입 (현재 'log'만 지원)", example = "log")
            @RequestParam(defaultValue = "log") String type,
            
            @Parameter(description = "조회할 추천 개수 (기본값: 20, 최대: 50)", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("추천 목록 API 호출: userId={}, type={}, limit={}", userId, type, limit);
        
        // 파라미터 검증
        if (limit != null && (limit <= 0 || limit > 50)) {
            throw new IllegalArgumentException("Limit must be between 1 and 50");
        }
        
        RecommendationResponseDto responseDto = recommendationService.getRecommendations(userId, type, limit);
        
        String message = responseDto.getRecommendations().isEmpty() 
            ? "추천 데이터가 없습니다. 더 많은 활동을 통해 개인화된 추천을 받아보세요!" 
            : String.format("개인화 추천 목록을 성공적으로 조회했습니다 (%d건)", responseDto.getRecommendations().size());
        
        return ResponseEntity.ok(ApiResponse.success(message, responseDto));
    }
    
    @Operation(
        summary = "추천 데이터 존재 여부 확인",
        description = "특정 사용자의 추천 데이터가 생성되어 있는지 확인합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "확인 성공"
        )
    })
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasRecommendations(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Integer userId,
            
            @Parameter(description = "추천 타입", example = "log")
            @RequestParam(defaultValue = "log") String type) {
        
        boolean exists = recommendationService.hasRecommendations(userId, type);
        String message = exists 
            ? "추천 데이터가 존재합니다" 
            : "추천 데이터가 아직 생성되지 않았습니다";
        
        return ResponseEntity.ok(ApiResponse.success(message, exists));
    }
    
    @Operation(
        summary = "추천 데이터 개수 조회",
        description = "특정 사용자의 추천 데이터 개수를 조회합니다."
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getRecommendationCount(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Integer userId,
            
            @Parameter(description = "추천 타입", example = "log")
            @RequestParam(defaultValue = "log") String type) {
        
        long count = recommendationService.getRecommendationCount(userId, type);
        String message = String.format("사용자의 추천 데이터 개수: %d건", count);
        
        return ResponseEntity.ok(ApiResponse.success(message, count));
    }
} 