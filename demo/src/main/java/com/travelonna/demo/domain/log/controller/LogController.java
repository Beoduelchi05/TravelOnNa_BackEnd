package com.travelonna.demo.domain.log.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.log.dto.LogRequestDto;
import com.travelonna.demo.domain.log.dto.LogResponseDto;
import com.travelonna.demo.domain.log.service.LogService;
import com.travelonna.demo.domain.user.repository.UserRepository;
import com.travelonna.demo.global.common.ApiResponse;
import com.travelonna.demo.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "여행 기록", description = "여행 기록 CRUD 및 좋아요/댓글 관리 API")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {
    
    private final LogService logService;
    private final UserRepository userRepository;
    
    @Operation(summary = "여행 기록 생성", description = "여행 일정 정보를 바탕으로 새로운 여행 기록을 생성합니다. 글(comment)은 필수이며, 이미지는 선택적으로 최대 10개까지 첨부 가능합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "기록 생성 성공", 
                 content = @Content(schema = @Schema(implementation = LogResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<LogResponseDto>> createLog(
            @Valid @RequestBody LogRequestDto requestDto) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogResponseDto responseDto = logService.createLog(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("기록이 성공적으로 생성되었습니다.", responseDto));
    }
    
    @Operation(summary = "여행 기록 상세 조회", description = "특정 여행 기록의 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<LogResponseDto>> getLog(
            @Parameter(description = "조회할 기록 ID", required = true) 
            @PathVariable Integer logId) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogResponseDto responseDto = logService.getLog(logId, userId);
        return ResponseEntity.ok(ApiResponse.success("기록을 성공적으로 조회했습니다.", responseDto));
    }
    
    @Operation(summary = "사용자별 여행 기록 목록 조회", description = "특정 사용자의 여행 기록 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<LogResponseDto>>> getLogsByUser(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Integer userId) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer currentUserId = getCurrentUserId();
        
        List<LogResponseDto> responseDtoList = logService.getLogsByUser(userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("사용자별 기록을 성공적으로 조회했습니다.", responseDtoList));
    }
    
    @Operation(summary = "일정별 여행 기록 목록 조회", description = "특정 여행 일정에 연결된 모든 기록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정별 기록 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @GetMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<List<LogResponseDto>>> getLogsByPlan(
            @Parameter(description = "조회할 일정 ID", required = true)
            @PathVariable Integer planId) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        List<LogResponseDto> responseDtoList = logService.getLogsByPlan(planId, userId);
        return ResponseEntity.ok(ApiResponse.success("일정별 기록을 성공적으로 조회했습니다.", responseDtoList));
    }
    
    @Operation(summary = "공개 여행 기록 목록 조회", description = "모든 공개 여행 기록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공개 기록 목록 조회 성공")
    })
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<LogResponseDto>>> getPublicLogs() {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        List<LogResponseDto> responseDtoList = logService.getPublicLogs(userId);
        return ResponseEntity.ok(ApiResponse.success("공개 기록을 성공적으로 조회했습니다.", responseDtoList));
    }
    
    @Operation(summary = "여행 기록 수정", description = "기존 여행 기록의 내용과 이미지를 수정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록을 찾을 수 없음")
    })
    @PutMapping("/{logId}")
    public ResponseEntity<ApiResponse<LogResponseDto>> updateLog(
            @Parameter(description = "수정할 기록 ID", required = true)
            @PathVariable Integer logId,
            @Valid @RequestBody LogRequestDto requestDto) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogResponseDto responseDto = logService.updateLog(logId, userId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("기록이 성공적으로 수정되었습니다.", responseDto));
    }
    
    @Operation(summary = "여행 기록 삭제", description = "여행 기록을 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기록 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록을 찾을 수 없음")
    })
    @DeleteMapping("/{logId}")
    public ResponseEntity<ApiResponse<Void>> deleteLog(
            @Parameter(description = "삭제할 기록 ID", required = true)
            @PathVariable Integer logId) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        logService.deleteLog(logId, userId);
        return ResponseEntity.ok(ApiResponse.success("기록이 성공적으로 삭제되었습니다.", null));
    }
    
    @Operation(summary = "여행 기록 좋아요 토글", description = "여행 기록에 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록을 찾을 수 없음")
    })
    @PostMapping("/{logId}/likes")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
            @Parameter(description = "좋아요 토글할 기록 ID", required = true)
            @PathVariable Integer logId) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        boolean isLiked = logService.toggleLike(logId, userId);
        return ResponseEntity.ok(ApiResponse.success(
                isLiked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.", 
                isLiked));
    }
    
    // TODO: 인증 구현 시 대체
    private Integer getCurrentUserId() {
        try {
            // 실제 환경에서는 SecurityContext에서 사용자 정보를 가져와야 함
            // 임시 구현: 데이터베이스에서 첫 번째 사용자 검색
            log.debug("사용자 ID 조회 시도 - 테스트 모드");
            
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                User user = users.get(0);
                log.debug("DB에서 첫 번째 사용자 조회 성공: userId={}", user.getUserId());
                return user.getUserId();
            } else {
                log.warn("DB에 사용자가 없음, 기본 테스트 ID 12 사용");
                return 12; // 테스트용 ID (실제 DB에 존재해야 함)
            }
        } catch (Exception e) {
            log.error("사용자 ID 조회 중 오류 발생: {}", e.getMessage(), e);
            return 12; // 오류 발생 시 기본값
        }
    }
} 