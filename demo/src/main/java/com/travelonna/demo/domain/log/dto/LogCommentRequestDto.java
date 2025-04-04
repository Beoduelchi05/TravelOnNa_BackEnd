package com.travelonna.demo.domain.log.dto;

import jakarta.validation.constraints.NotBlank;

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
public class LogCommentRequestDto {
    
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String comment;
    
    private Integer parentId;  // 답글인 경우 부모 댓글 ID, 최상위 댓글인 경우 null
} 