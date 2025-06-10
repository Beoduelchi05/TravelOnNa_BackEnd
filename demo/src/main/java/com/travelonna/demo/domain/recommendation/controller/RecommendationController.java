package com.travelonna.demo.domain.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.recommendation.dto.ColdStartRecommendationRequestDto;
import com.travelonna.demo.domain.recommendation.dto.ColdStartRecommendationResponseDto;
import com.travelonna.demo.domain.recommendation.dto.RecommendationRequestDto;
import com.travelonna.demo.domain.recommendation.dto.RecommendationResponseDto;
import com.travelonna.demo.domain.recommendation.service.AIRecommendationClient;
import com.travelonna.demo.domain.recommendation.service.RecommendationService;
import com.travelonna.demo.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "추천", description = "AI 기반 개인화 추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    private final AIRecommendationClient aiRecommendationClient;
    
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
    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationResponseDto>> getRecommendations(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "추천 요청 정보",
                content = @Content(schema = @Schema(implementation = RecommendationRequestDto.class))
            )
            @Valid @RequestBody RecommendationRequestDto requestDto) {
        
        log.info("추천 목록 API 호출: userId={}, type={}, limit={}", 
                requestDto.getUser_id(), requestDto.getRec_type(), requestDto.getRec_limit());
        
        RecommendationResponseDto responseDto = recommendationService.getRecommendations(
                requestDto.getUser_id(), 
                requestDto.getRec_type(), 
                requestDto.getRec_limit());
        
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
        summary = "추천 개수 조회",
        description = "사용자별 특정 타입의 추천 개수를 조회합니다"
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getRecommendationCount(
            @RequestParam Integer userId,
            @RequestParam String type) {
        
        long count = recommendationService.getRecommendationCount(userId, type);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("추천 개수를 성공적으로 조회했습니다 (%d개)", count),
            count
        ));
    }
    
    /**
     * 콜드스타트용 무작위 공개 기록 추천
     */
    @PostMapping("/coldstart")
    @Operation(summary = "콜드스타트 추천", description = "신규 사용자를 위한 무작위 공개 기록 추천 (페이지네이션 방식)")
    public ResponseEntity<ApiResponse<ColdStartRecommendationResponseDto>> getColdStartRecommendations(
            @RequestBody @Valid ColdStartRecommendationRequestDto requestDto) {
        
        ColdStartRecommendationResponseDto responseDto = recommendationService.getColdStartRecommendations(
            requestDto.getUserId(), 
            requestDto.getLimit(), 
            requestDto.getOffset()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("콜드스타트 추천을 성공적으로 조회했습니다 (%d건)", responseDto.getLogs().size()),
            responseDto
        ));
    }
    
    // ===== 배치 관리 API =====
    
    @PostMapping("/batch/trigger")
    @Operation(
        summary = "AI 추천 배치 처리 트리거", 
        description = "AI 추천 서비스의 배치 처리를 수동으로 시작합니다.\n\n" +
                     "**배치 타입**:\n" +
                     "- `full`: 전체 사용자 대상 배치 (새벽 2시 실행되는 것과 동일)\n" +
                     "- `incremental`: 최근 활동 사용자만 대상 (6시간마다 실행되는 것과 동일)\n\n" +
                     "**주의**: 배치 처리는 시간이 오래 걸릴 수 있습니다 (수 분 소요)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "배치 처리 트리거 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 배치 타입"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "AI 서비스 호출 실패"
        )
    })
    public ResponseEntity<ApiResponse<AIRecommendationClient.BatchTriggerResponse>> triggerBatch(
            @Parameter(description = "배치 타입", example = "full", required = true)
            @RequestParam String batchType) {
        
        log.info("배치 처리 트리거 API 호출: batchType={}", batchType);
        
        // 배치 타입 유효성 검사
        if (!batchType.equals("full") && !batchType.equals("incremental")) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("배치 타입은 'full' 또는 'incremental'이어야 합니다")
            );
        }
        
        try {
            AIRecommendationClient.BatchTriggerResponse response = aiRecommendationClient.triggerBatch(batchType);
            
            if (response.isSuccess()) {
                log.info("배치 처리 트리거 성공: batchType={}, duration={}초", 
                        batchType, response.getDurationSeconds());
                
                return ResponseEntity.ok(ApiResponse.success(
                    String.format("%s 배치 처리를 성공적으로 시작했습니다 (소요시간: %.1f초)", 
                                 batchType, response.getDurationSeconds() != null ? response.getDurationSeconds() : 0.0),
                    response
                ));
            } else {
                log.error("배치 처리 트리거 실패: batchType={}, message={}", batchType, response.getMessage());
                
                return ResponseEntity.ok(ApiResponse.error(
                    String.format("배치 처리 트리거 실패: %s", response.getMessage())
                ));
            }
            
        } catch (Exception e) {
            log.error("배치 처리 트리거 중 예외 발생: batchType={}, error={}", batchType, e.getMessage());
            
            return ResponseEntity.status(500).body(
                ApiResponse.error("배치 처리 트리거 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
    
    @GetMapping("/batch/status")
    @Operation(
        summary = "AI 추천 배치 상태 조회",
        description = "최근 실행된 배치 처리의 상태를 조회합니다.\n\n" +
                     "**상태 정보**:\n" +
                     "- 배치 ID, 타입, 처리된 사용자 수, 생성된 추천 수\n" +
                     "- 시작/종료 시간, 실행 상태, 오류 메시지 등"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "배치 상태 조회 성공"
        )
    })
    public ResponseEntity<ApiResponse<AIRecommendationClient.BatchStatusResponse>> getBatchStatus() {
        
        log.info("배치 상태 조회 API 호출");
        
        try {
            AIRecommendationClient.BatchStatusResponse response = aiRecommendationClient.getBatchStatus();
            
            String message = response.getRecentBatches() != null && !response.getRecentBatches().isEmpty()
                ? String.format("최근 배치 상태를 성공적으로 조회했습니다 (%d개)", response.getRecentBatches().size())
                : "최근 배치 실행 기록이 없습니다";
            
            return ResponseEntity.ok(ApiResponse.success(message, response));
            
        } catch (Exception e) {
            log.error("배치 상태 조회 중 예외 발생: error={}", e.getMessage());
            
            return ResponseEntity.status(500).body(
                ApiResponse.error("배치 상태 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
} 