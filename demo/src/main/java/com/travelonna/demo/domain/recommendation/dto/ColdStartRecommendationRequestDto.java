package com.travelonna.demo.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "콜드스타트 추천 요청")
public class ColdStartRecommendationRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    @Schema(description = "사용자 ID", example = "73051")
    private Integer userId;
    
    @Schema(description = "추천 개수 (기본값: 10)", example = "10", defaultValue = "10")
    private Integer limit = 10;
    
    @PositiveOrZero(message = "오프셋은 0 이상이어야 합니다")
    @Schema(description = "페이지 오프셋 (기본값: 0)", example = "0", defaultValue = "0")
    private Integer offset = 0;
} 