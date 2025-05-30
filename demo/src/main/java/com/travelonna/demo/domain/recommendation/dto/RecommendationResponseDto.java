package com.travelonna.demo.domain.recommendation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecommendationResponseDto {
    
    private Integer userId;
    private String itemType;
    private List<RecommendationItemDto> recommendations;
    
    @Getter
    @Setter
    @Builder
    public static class RecommendationItemDto {
        private Integer itemId;
        private Float score;
        
        // Log 관련 정보
        private Integer logId;
        private Integer userId;
        private Integer planId;
        private String comment;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        private Boolean isPublic;
    }
} 