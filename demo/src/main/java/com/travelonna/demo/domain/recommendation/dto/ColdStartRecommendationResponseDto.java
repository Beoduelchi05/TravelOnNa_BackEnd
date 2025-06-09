package com.travelonna.demo.domain.recommendation.dto;

import java.util.List;

import com.travelonna.demo.domain.log.dto.LogResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콜드스타트 추천 응답")
public class ColdStartRecommendationResponseDto {
    
    @Schema(description = "사용자 ID", example = "73051")
    private Integer userId;
    
    @Schema(description = "추천 유형", example = "coldstart")
    private String recommendationType;
    
    @Schema(description = "무작위 추천된 공개 로그 목록")
    private List<LogResponseDto> logs;
    
    @Schema(description = "추천된 로그 ID 목록 (중복 방지용)", example = "[101, 102, 103]")
    private List<Integer> logIds;
} 