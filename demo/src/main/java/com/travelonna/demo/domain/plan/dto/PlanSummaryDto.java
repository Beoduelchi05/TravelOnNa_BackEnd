package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;

import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.plan.entity.TransportInfo;

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
public class PlanSummaryDto {
    
    private Integer planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private TransportInfo transportInfo;
    private String location;
    private String title;
    private Boolean isPublic;
    private Integer totalCost;
    
    public static PlanSummaryDto fromEntity(Plan plan) {
        if (plan == null) {
            return null;
        }
        
        return PlanSummaryDto.builder()
                .planId(plan.getPlanId())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .transportInfo(plan.getTransportInfo())
                .location(plan.getLocation())
                .title(plan.getTitle())
                .isPublic(plan.getIsPublic())
                .totalCost(plan.getTotalCost())
                .build();
    }
} 