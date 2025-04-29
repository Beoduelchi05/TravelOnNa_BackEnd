package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;
import java.lang.reflect.Field;

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
        PlanSummaryDto dto = new PlanSummaryDto();
        
        try {
            // 리플렉션을 사용하여 Plan 필드에 접근
            Field planIdField = plan.getClass().getDeclaredField("planId");
            planIdField.setAccessible(true);
            dto.planId = (Integer) planIdField.get(plan);
            
            Field startDateField = plan.getClass().getDeclaredField("startDate");
            startDateField.setAccessible(true);
            dto.startDate = (LocalDate) startDateField.get(plan);
            
            Field endDateField = plan.getClass().getDeclaredField("endDate");
            endDateField.setAccessible(true);
            dto.endDate = (LocalDate) endDateField.get(plan);
            
            Field transportInfoField = plan.getClass().getDeclaredField("transportInfo");
            transportInfoField.setAccessible(true);
            dto.transportInfo = (TransportInfo) transportInfoField.get(plan);
            
            Field locationField = plan.getClass().getDeclaredField("location");
            locationField.setAccessible(true);
            dto.location = (String) locationField.get(plan);
            
            Field titleField = plan.getClass().getDeclaredField("title");
            titleField.setAccessible(true);
            dto.title = (String) titleField.get(plan);
            
            Field isPublicField = plan.getClass().getDeclaredField("isPublic");
            isPublicField.setAccessible(true);
            dto.isPublic = (Boolean) isPublicField.get(plan);
            
            Field totalCostField = plan.getClass().getDeclaredField("totalCost");
            totalCostField.setAccessible(true);
            dto.totalCost = (Integer) totalCostField.get(plan);
            
        } catch (Exception e) {
            // 에러 처리
            e.printStackTrace();
        }
        
        return dto;
    }
} 