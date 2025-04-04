package com.travelonna.demo.domain.log.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.travelonna.demo.domain.log.entity.LogComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogCommentResponseDto {
    
    private Integer commentId;
    private Integer userId;
    private String userName;
    private String userProfileImage;
    private String comment;
    private LocalDateTime createdAt;
    private Integer parentId;
    private List<LogCommentResponseDto> replies = new ArrayList<>();
    
    public static LogCommentResponseDto fromEntity(LogComment comment) {
        LogCommentResponseDto dto = LogCommentResponseDto.builder()
                .commentId(comment.getLocoId())
                .userId(comment.getUser().getUserId())
                .userName(comment.getUser().getName())
                .comment(comment.getLocoComment())
                .createdAt(comment.getCreatedAt())
                .parentId(comment.getParent() != null ? comment.getParent().getLocoId() : null)
                .build();
        
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            dto.setReplies(comment.getChildren().stream()
                    .map(LogCommentResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 