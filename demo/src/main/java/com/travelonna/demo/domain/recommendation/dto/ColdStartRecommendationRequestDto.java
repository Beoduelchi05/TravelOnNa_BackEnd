package com.travelonna.demo.domain.recommendation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    
    @Schema(description = "제외할 로그 ID 목록 (중복 방지용)", example = "[101, 102, 103]")
    private List<Integer> excludeLogIds;
} 