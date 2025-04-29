package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.lang.reflect.Field;

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
        PlanResponseDto dto = new PlanResponseDto();
        
        try {
            // 리플렉션을 사용하여 Plan 필드 접근
            Field planIdField = plan.getClass().getDeclaredField("planId");
            planIdField.setAccessible(true);
            dto.planId = (Integer) planIdField.get(plan);
            
            Field userIdField = plan.getClass().getDeclaredField("userId");
            userIdField.setAccessible(true);
            dto.userId = (Integer) userIdField.get(plan);
            
            Field titleField = plan.getClass().getDeclaredField("title");
            titleField.setAccessible(true);
            dto.title = (String) titleField.get(plan);
            
            Field startDateField = plan.getClass().getDeclaredField("startDate");
            startDateField.setAccessible(true);
            dto.startDate = (LocalDate) startDateField.get(plan);
            
            Field endDateField = plan.getClass().getDeclaredField("endDate");
            endDateField.setAccessible(true);
            dto.endDate = (LocalDate) endDateField.get(plan);
            
            Field locationField = plan.getClass().getDeclaredField("location");
            locationField.setAccessible(true);
            dto.location = (String) locationField.get(plan);
            
            Field transportInfoField = plan.getClass().getDeclaredField("transportInfo");
            transportInfoField.setAccessible(true);
            dto.transportInfo = (TransportInfo) transportInfoField.get(plan);
            
            Field groupIdField = plan.getClass().getDeclaredField("groupId");
            groupIdField.setAccessible(true);
            dto.groupId = (Integer) groupIdField.get(plan);
            
            Field isPublicField = plan.getClass().getDeclaredField("isPublic");
            isPublicField.setAccessible(true);
            dto.isPublic = (Boolean) isPublicField.get(plan);
            
            Field totalCostField = plan.getClass().getDeclaredField("totalCost");
            totalCostField.setAccessible(true);
            dto.totalCost = (Integer) totalCostField.get(plan);
            
            Field memoField = plan.getClass().getDeclaredField("memo");
            memoField.setAccessible(true);
            dto.memo = (String) memoField.get(plan);
            
            Field createdAtField = plan.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            dto.createdAt = (LocalDateTime) createdAtField.get(plan);
            
            Field updatedAtField = plan.getClass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            dto.updatedAt = (LocalDateTime) updatedAtField.get(plan);
            
        } catch (Exception e) {
            // 에러 처리
            e.printStackTrace();
        }
        
        return dto;
    }
} 