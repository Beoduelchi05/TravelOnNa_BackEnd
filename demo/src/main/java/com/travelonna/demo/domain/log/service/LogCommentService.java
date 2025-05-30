package com.travelonna.demo.domain.log.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.log.dto.LogCommentRequestDto;
import com.travelonna.demo.domain.log.dto.LogCommentResponseDto;
import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.log.entity.LogComment;
import com.travelonna.demo.domain.log.repository.LogCommentRepository;
import com.travelonna.demo.domain.log.repository.LogRepository;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;
import com.travelonna.demo.domain.user.service.UserActionService;
import com.travelonna.demo.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LogCommentService {
    
    private final LogCommentRepository logCommentRepository;
    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final UserActionService userActionService;
    
    // 댓글 생성
    @Transactional
    public LogCommentResponseDto createComment(Integer logId, Integer userId, LogCommentRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Log logEntity = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        
        // 비공개 기록인 경우 권한 확인
        if (!logEntity.getIsPublic() && !logEntity.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to comment on this log");
        }
        
        // comment가 null이거나 비어있는지 확인 (@Valid로 검증되지만 추가 검증)
        if (requestDto.getComment() == null || requestDto.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment is required");
        }
        
        LogComment comment = LogComment.builder()
                .log(logEntity)
                .user(user)
                .locoComment(requestDto.getComment())
                .build();
        
        // children 필드가 null인 경우 초기화
        if (comment.getChildren() == null) {
            comment.setChildren(new ArrayList<>());
        }
        
        // 답글인 경우 부모 댓글 설정
        if (requestDto.getParentId() != null) {
            LogComment parentComment = logCommentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            
            // 부모 댓글의 children 필드가 null인 경우 초기화
            if (parentComment.getChildren() == null) {
                parentComment.setChildren(new ArrayList<>());
            }
            
            // 부모 댓글이 해당 기록에 속하는지 확인
            if (!parentComment.getLog().getLogId().equals(logId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this log");
            }
            
            // 답글의 답글은 허용하지 않음 (2단계까지만 허용)
            if (parentComment.getParent() != null) {
                throw new IllegalArgumentException("Nested replies are not allowed");
            }
            
            parentComment.addReply(comment);
        } else {
            logEntity.addComment(comment);
        }
        
        LogComment savedComment = logCommentRepository.save(comment);
        
        // UserAction 기록 - COMMENT 액션 (공개 기록인 경우만)
        if (logEntity.getIsPublic()) {
            try {
                userActionService.recordComment(userId, logId);
            } catch (Exception e) {
                log.warn("사용자 액션 기록 실패: userId={}, logId={}", userId, logId, e);
            }
        }
        
        return LogCommentResponseDto.fromEntity(savedComment);
    }
    
    // 댓글 조회
    public List<LogCommentResponseDto> getCommentsByLogId(Integer logId) {
        List<LogComment> comments = logCommentRepository.findCommentsByLogIdWithReplies(logId);
        return comments.stream()
                .map(LogCommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 댓글 수정
    @Transactional
    public LogCommentResponseDto updateComment(Integer commentId, Integer userId, LogCommentRequestDto requestDto) {
        LogComment comment = logCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // 댓글 작성자 확인
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to update this comment");
        }
        
        comment.updateComment(requestDto.getComment());
        
        return LogCommentResponseDto.fromEntity(comment);
    }
    
    // 댓글 삭제
    @Transactional
    public void deleteComment(Integer commentId, Integer userId) {
        LogComment comment = logCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // 댓글 작성자 또는 기록 작성자인지 확인
        if (!comment.getUser().getUserId().equals(userId) && 
            !comment.getLog().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }
        
        logCommentRepository.delete(comment);
    }
} 