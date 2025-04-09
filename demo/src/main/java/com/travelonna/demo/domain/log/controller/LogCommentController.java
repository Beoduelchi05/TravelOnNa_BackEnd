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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogCommentController {
    
    private final LogCommentService logCommentService;
    private final UserRepository userRepository;
    
    // 댓글 생성
    @PostMapping("/{logId}/comments")
    public ResponseEntity<ApiResponse<LogCommentResponseDto>> createComment(
            @PathVariable Integer logId,
            @Valid @RequestBody LogCommentRequestDto requestDto) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogCommentResponseDto responseDto = logCommentService.createComment(logId, userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글이 성공적으로 생성되었습니다.", responseDto));
    }
    
    // 특정 기록의 댓글 조회
    @GetMapping("/{logId}/comments")
    public ResponseEntity<ApiResponse<List<LogCommentResponseDto>>> getCommentsByLogId(
            @PathVariable Integer logId) {
        List<LogCommentResponseDto> responseDtoList = logCommentService.getCommentsByLogId(logId);
        return ResponseEntity.ok(ApiResponse.success("댓글 목록을 성공적으로 조회했습니다.", responseDtoList));
    }
    
    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<LogCommentResponseDto>> updateComment(
            @PathVariable Integer commentId,
            @Valid @RequestBody LogCommentRequestDto requestDto) {
        // 현재 로그인한 사용자 ID (인증 구현 필요)
        Integer userId = getCurrentUserId();
        
        LogCommentResponseDto responseDto = logCommentService.updateComment(commentId, userId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("댓글이 성공적으로 수정되었습니다.", responseDto));
    }
    
    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
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