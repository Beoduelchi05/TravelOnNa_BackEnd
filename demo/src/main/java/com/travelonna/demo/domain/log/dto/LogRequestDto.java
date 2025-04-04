package com.travelonna.demo.domain.log.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class LogRequestDto {
    
    @NotNull(message = "일정 ID는 필수입니다")
    private Integer planId;
    
    @NotBlank(message = "기록 내용은 필수입니다")
    private String comment;
    
    private Boolean isPublic;
    
    private List<String> imageUrls;  // 이미지 URL 목록 (최대 10개, 선택 사항)
} 