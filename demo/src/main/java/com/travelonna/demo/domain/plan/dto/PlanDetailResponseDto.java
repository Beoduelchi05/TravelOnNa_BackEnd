package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.travelonna.demo.domain.plan.entity.Plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일정 상세 정보 응답 DTO")
public class PlanDetailResponseDto {
    
    @Schema(description = "일정 ID", example = "1")
    private Integer planId;
    
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;
    
    @Schema(description = "일정 제목", example = "대구 여행")
    private String title;
    
    @Schema(description = "여행지", example = "대구")
    private String location;
    
    @Schema(description = "시작 날짜", example = "2023-11-20")
    private LocalDate startDate;
    
    @Schema(description = "종료 날짜", example = "2023-11-22")
    private LocalDate endDate;
    
    @Schema(description = "이동수단", example = "CAR")
    private String transportInfo;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
    
    @Schema(description = "총 비용", example = "150000")
    private Integer totalCost;
    
    @Schema(description = "메모", example = "대구 여행 일정")
    private String memo;
    
    @Schema(description = "생성일", example = "2023-11-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일", example = "2023-11-15T10:30:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "장소 목록")
    private List<PlaceResponseDto> places;
    
    public static PlanDetailResponseDto fromEntity(Plan plan, List<PlaceResponseDto> places) {
        return PlanDetailResponseDto.builder()
                .planId(plan.getPlanId())
                .userId(plan.getUserId())
                .title(plan.getTitle())
                .location(plan.getLocation())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .transportInfo(plan.getTransportInfo() != null ? plan.getTransportInfo().name() : null)
                .isPublic(plan.getIsPublic())
                .totalCost(plan.getTotalCost())
                .memo(plan.getMemo())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .places(places)
                .build();
    }
} 