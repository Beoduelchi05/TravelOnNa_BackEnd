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

import com.travelonna.demo.domain.log.dto.LogCommentRequestDto;
import com.travelonna.demo.domain.log.dto.LogCommentResponseDto;
import com.travelonna.demo.domain.log.service.LogCommentService;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;
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

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "여행 기록 댓글", description = "여행 기록 댓글 관리 API")
public class LogCommentController {
    
    private final LogCommentService logCommentService;
    private final UserRepository userRepository;
    
    // 댓글 생성
    @Operation(summary = "댓글 생성", description = "여행 기록에 댓글을 작성합니다. 대댓글인 경우 parentId를 지정하세요.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "댓글 생성 성공", 
                 content = @Content(schema = @Schema(implementation = LogCommentResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록을 찾을 수 없음")
    })
    @PostMapping("/{logId}/comments")
    public ResponseEntity<ApiResponse<LogCommentResponseDto>> createComment(
            @Parameter(description = "댓글을 작성할 기록 ID", required = true, example = "1")
            @PathVariable Integer logId,
            @Parameter(description = "댓글 정보", example = "{\n  \"comment\": \"정말 멋진 여행이네요!\",\n  \"parentId\": null\n}")
            @Valid @RequestBody LogCommentRequestDto requestDto) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogCommentResponseDto responseDto = logCommentService.createComment(logId, userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글이 성공적으로 생성되었습니다.", responseDto));
    }
    
    // 특정 기록의 댓글 조회
    @Operation(summary = "기록별 댓글 목록 조회", description = "특정 여행 기록의 댓글 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록을 찾을 수 없음")
    })
    @GetMapping("/{logId}/comments")
    public ResponseEntity<ApiResponse<List<LogCommentResponseDto>>> getCommentsByLogId(
            @Parameter(description = "조회할 기록 ID", required = true, example = "1")
            @PathVariable Integer logId) {
        List<LogCommentResponseDto> responseDtoList = logCommentService.getCommentsByLogId(logId);
        return ResponseEntity.ok(ApiResponse.success("댓글 목록을 성공적으로 조회했습니다.", responseDtoList));
    }
    
    // 댓글 수정
    @Operation(summary = "댓글 수정", description = "기존 댓글의 내용을 수정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<LogCommentResponseDto>> updateComment(
            @Parameter(description = "수정할 댓글 ID", required = true, example = "5")
            @PathVariable Integer commentId,
            @Parameter(description = "수정할 댓글 정보", example = "{\n  \"comment\": \"수정된 댓글입니다.\",\n  \"parentId\": null\n}")
            @Valid @RequestBody LogCommentRequestDto requestDto) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogCommentResponseDto responseDto = logCommentService.updateComment(commentId, userId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("댓글이 성공적으로 수정되었습니다.", responseDto));
    }
    
    // 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "삭제할 댓글 ID", required = true, example = "7")
            @PathVariable Integer commentId) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        logCommentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("댓글이 성공적으로 삭제되었습니다.", null));
    }
    
    // TODO: 인증 구현 시 대체
    private Integer getCurrentUserId() {
        try {
            // DB에서 첫 번째 사용자를 찾아 그 ID 반환
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                Integer userId = users.get(0).getUserId();
                log.info("Using existing user ID: {}", userId);
                return userId;
            } else {
                log.warn("No users found in database, using default ID 6");
                return 6; // 기본값
            }
        } catch (Exception e) {
            log.error("Error finding a valid user ID: {}", e.getMessage());
            return 6; // 오류 발생 시 기본값
        }
    }
} 