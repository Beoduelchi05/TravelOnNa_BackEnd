package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.plan.entity.TransportInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponseDto {
    
    @Schema(description = "일정 ID", example = "1")
    private Integer planId;
    
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;
    
    @Schema(description = "일정 제목", example = "제주도 여행")
    private String title;
    
    @Schema(description = "시작 날짜", example = "2024-04-01")
    private LocalDate startDate;
    
    @Schema(description = "종료 날짜", example = "2024-04-05")
    private LocalDate endDate;
    
    @Schema(description = "여행지", example = "제주도")
    private String location;
    
    @Schema(description = "이동 수단", example = "car")
    private TransportInfo transportInfo;
    
    @Schema(description = "그룹 ID", example = "1")
    private Integer groupId;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
    
    @Schema(description = "총 비용", example = "500000")
    private Integer totalCost;
    
    @Schema(description = "메모", example = "준비물: 수영복, 선글라스")
    private String memo;
    
    @Schema(description = "생성 시간", example = "2024-03-21T14:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정 시간", example = "2024-03-21T15:30:00")
    private LocalDateTime updatedAt;
    
    // Entity to DTO 변환
    public static PlanResponseDto fromEntity(Plan plan) {
        return PlanResponseDto.builder()
                .planId(plan.getPlanId())
                .userId(plan.getUserId()) 
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .location(plan.getLocation())
                .transportInfo(plan.getTransportInfo())
                .groupId(plan.getGroupId())
                .isPublic(plan.getIsPublic())
                .totalCost(plan.getTotalCost())
                .memo(plan.getMemo())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
} 