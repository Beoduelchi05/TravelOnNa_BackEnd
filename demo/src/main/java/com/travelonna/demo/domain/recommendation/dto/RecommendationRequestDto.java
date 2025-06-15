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
    
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
    private Integer page = 1;
    
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다")
    @Schema(description = "페이지 크기 (기본값: 20, 최대: 50)", example = "20", defaultValue = "20")
    private Integer size = 20;
    
    // 기존 rec_limit 필드는 하위 호환성을 위해 유지하되 deprecated
    @Deprecated
    @Min(value = 1, message = "조회할 추천 개수는 1 이상이어야 합니다")
    @Max(value = 50, message = "조회할 추천 개수는 50 이하여야 합니다")
    @Schema(description = "조회할 추천 개수 (deprecated: size 사용 권장)", example = "20", deprecated = true)
    private Integer rec_limit;
    
    // 편의 메소드: rec_limit가 설정되어 있으면 size를 덮어씀 (하위 호환성)
    public Integer getEffectiveSize() {
        return rec_limit != null ? rec_limit : size;
    }
    
    // 편의 메소드: 0 기반 offset 계산
    public Integer getOffset() {
        return (page - 1) * getEffectiveSize();
    }
} 