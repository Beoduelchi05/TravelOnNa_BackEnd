package com.travelonna.demo.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "추천 요청 정보")
public class RecommendationRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer user_id;
    
    @Schema(description = "추천 타입 (현재 'log'만 지원)", example = "log", defaultValue = "log")
    private String rec_type = "log";
    
    @Min(value = 1, message = "조회할 추천 개수는 1 이상이어야 합니다")
    @Max(value = 50, message = "조회할 추천 개수는 50 이하여야 합니다")
    @Schema(description = "조회할 추천 개수 (기본값: 20, 최대: 50)", example = "20", defaultValue = "20")
    private Integer rec_limit = 20;
} 