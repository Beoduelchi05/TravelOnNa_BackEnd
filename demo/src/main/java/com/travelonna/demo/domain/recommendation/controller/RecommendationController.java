package com.travelonna.demo.domain.recommendation.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
        summary = "개인화 추천 목록 조회 (페이지네이션)",
        description = "사용자의 행동 데이터를 기반으로 생성된 개인화 추천 목록을 페이지네이션 방식으로 조회합니다.\n\n" +
                     "**추천 알고리즘**: ALS(Alternating Least Squares) 협업 필터링과 인기도 기반 알고리즘을 결합한 하이브리드 방식\n\n" +
                     "**데이터 소스**: user_actions 테이블의 사용자 행동 데이터 (POST, LIKE, COMMENT, VIEW)\n\n" +
                     "**업데이트 주기**: 매일 새벽 2시 전체 배치, 6시간마다 증분 배치\n\n" +
                     "**현재 지원 타입**: log (여행 기록 추천)\n\n" +
                     "**페이지네이션**: page는 1부터 시작, size는 최대 50까지 지원"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "추천 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (지원하지 않는 타입, 잘못된 페이지 번호 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<RecommendationResponseDto>> getRecommendationsPaginated(
            @Parameter(description = "사용자 ID", required = true, example = "123")
            @RequestParam Integer userId,
            
            @Parameter(description = "추천 타입 (현재 'log'만 지원)", example = "log")
            @RequestParam(defaultValue = "log") String type,
            
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") 
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다") Integer page,
            
            @Parameter(description = "페이지 크기 (최대 50)", example = "20")
            @RequestParam(defaultValue = "20") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다") Integer size) {
        
        log.info("페이지네이션 추천 목록 API 호출: userId={}, type={}, page={}, size={}", 
                userId, type, page, size);
        
        RecommendationResponseDto responseDto = recommendationService.getRecommendationsPaginated(
                userId, type, page, size);
        
        String message = responseDto.getRecommendations().isEmpty() 
            ? "추천 데이터가 없습니다. 더 많은 활동을 통해 개인화된 추천을 받아보세요!" 
            : String.format("개인화 추천 목록을 성공적으로 조회했습니다 (페이지 %d/%d, %d건)", 
                           responseDto.getPageInfo().getCurrentPage(),
                           responseDto.getPageInfo().getTotalPages(),
                           responseDto.getRecommendations().size());
        
        return ResponseEntity.ok(ApiResponse.success(message, responseDto));
    }
    
    @Operation(
        summary = "개인화 추천 목록 조회 (레거시)", 
        description = "사용자의 행동 데이터를 기반으로 생성된 개인화 추천 목록을 조회합니다.\n\n" +
                     "**⚠️ 권장하지 않음**: 이 API는 하위 호환성을 위해 유지됩니다. 새로운 개발에서는 GET /api/v1/recommendations를 사용하세요.\n\n" +
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
    @PostMapping("/legacy")
    public ResponseEntity<ApiResponse<RecommendationResponseDto>> getRecommendationsLegacy(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "추천 요청 정보",
                content = @Content(schema = @Schema(implementation = RecommendationRequestDto.class))
            )
            @Valid @RequestBody RecommendationRequestDto requestDto) {
        
        log.info("레거시 추천 목록 API 호출: userId={}, type={}, limit={}", 
                requestDto.getUser_id(), requestDto.getRec_type(), requestDto.getEffectiveSize());
        
        // 새로운 페이지네이션 메소드 사용 (첫 번째 페이지)
        RecommendationResponseDto responseDto = recommendationService.getRecommendationsPaginated(
                requestDto.getUser_id(), 
                requestDto.getRec_type(), 
                1, // 첫 번째 페이지
                requestDto.getEffectiveSize());
        
        String message = responseDto.getRecommendations().isEmpty() 
            ? "추천 데이터가 없습니다. 더 많은 활동을 통해 개인화된 추천을 받아보세요!" 
            : String.format("개인화 추천 목록을 성공적으로 조회했습니다 (%d건)", responseDto.getRecommendations().size());
        
        return ResponseEntity.ok(ApiResponse.success(message, responseDto));
    }
    
    @Operation(
        summary = "개인화 추천 목록 조회 (POST 방식)",
        description = "기존 POST 방식 API의 하위 호환성을 위해 유지됩니다. 새로운 개발에서는 GET 방식을 권장합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationResponseDto>> getRecommendations(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "추천 요청 정보",
                content = @Content(schema = @Schema(implementation = RecommendationRequestDto.class))
            )
            @Valid @RequestBody RecommendationRequestDto requestDto) {
        
        log.info("POST 추천 목록 API 호출: userId={}, type={}, page={}, size={}", 
                requestDto.getUser_id(), requestDto.getRec_type(), requestDto.getPage(), requestDto.getEffectiveSize());
        
        // 새로운 페이지네이션 메소드 사용
        RecommendationResponseDto responseDto = recommendationService.getRecommendationsPaginated(
                requestDto.getUser_id(), 
                requestDto.getRec_type(), 
                requestDto.getPage(),
                requestDto.getEffectiveSize());
        
        String message = responseDto.getRecommendations().isEmpty() 
            ? "추천 데이터가 없습니다. 더 많은 활동을 통해 개인화된 추천을 받아보세요!" 
            : String.format("개인화 추천 목록을 성공적으로 조회했습니다 (페이지 %d/%d, %d건)", 
                           responseDto.getPageInfo().getCurrentPage(),
                           responseDto.getPageInfo().getTotalPages(),
                           responseDto.getRecommendations().size());
        
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
            "콜드스타트 추천을 성공적으로 조회했습니다",
            responseDto
        ));
    }
    
    // ===== 배치 관리 API =====
    
    @PostMapping("/batch/trigger")
    @Operation(summary = "추천 배치 처리 트리거", 
              description = "AI 추천 서비스의 배치 처리를 수동으로 실행합니다 (비동기 처리)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치 처리 시작 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배치 처리 시작 실패")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerBatch(
            @Parameter(description = "배치 타입 (full: 전체, incremental: 증분)", example = "full")
            @RequestParam(defaultValue = "incremental") String batchType,
            @Parameter(description = "처리할 최대 사용자 수 (full batch 전용, 미지정 시 전체 처리)", example = "100")
            @RequestParam(required = false) Integer userLimit
    ) {
        try {
            log.info("📡 배치 처리 트리거 요청: batchType={}, userLimit={}", batchType, userLimit);
            
            Map<String, Object> result = aiRecommendationClient.triggerBatch(batchType, userLimit);
            
            return ResponseEntity.ok(ApiResponse.success(
                "배치 처리가 백그라운드에서 시작되었습니다", 
                result
            ));
            
        } catch (Exception e) {
            log.error("❌ 배치 처리 트리거 실패: batchType={}, error={}", batchType, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("배치 처리 트리거 실패: " + e.getMessage()));
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