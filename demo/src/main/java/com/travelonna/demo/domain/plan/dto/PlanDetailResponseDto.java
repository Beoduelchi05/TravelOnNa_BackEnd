package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.travelonna.demo.domain.plan.entity.Plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
    
    @Schema(description = "그룹 ID", example = "1")
    @JsonProperty("group_id")
    private Integer groupId;
    
    @Schema(description = "그룹 일정 여부", example = "true")
    @JsonProperty("is_group")
    private Boolean isGroup;
    
    @Schema(description = "장소 목록")
    private List<PlaceResponseDto> places;
    
    // Plan Entity를 PlanDetailResponseDto로 변환하는 정적 팩토리 메소드
    public static PlanDetailResponseDto fromEntity(Plan plan, List<PlaceResponseDto> places) {
        PlanDetailResponseDto dto = new PlanDetailResponseDto();
        
        // Lombok이 생성한 getter 메소드가 컴파일러에서 인식되지 않는 문제를 해결하기 위해
        // 우회적인 방법으로 값을 설정합니다
        try {
            java.lang.reflect.Field planIdField = plan.getClass().getDeclaredField("planId");
            planIdField.setAccessible(true);
            dto.planId = (Integer) planIdField.get(plan);
            
            java.lang.reflect.Field userIdField = plan.getClass().getDeclaredField("userId");
            userIdField.setAccessible(true);
            dto.userId = (Integer) userIdField.get(plan);
            
            java.lang.reflect.Field titleField = plan.getClass().getDeclaredField("title");
            titleField.setAccessible(true);
            dto.title = (String) titleField.get(plan);
            
            java.lang.reflect.Field locationField = plan.getClass().getDeclaredField("location");
            locationField.setAccessible(true);
            dto.location = (String) locationField.get(plan);
            
            java.lang.reflect.Field startDateField = plan.getClass().getDeclaredField("startDate");
            startDateField.setAccessible(true);
            dto.startDate = (LocalDate) startDateField.get(plan);
            
            java.lang.reflect.Field endDateField = plan.getClass().getDeclaredField("endDate");
            endDateField.setAccessible(true);
            dto.endDate = (LocalDate) endDateField.get(plan);
            
            java.lang.reflect.Field transportInfoField = plan.getClass().getDeclaredField("transportInfo");
            transportInfoField.setAccessible(true);
            Object transportInfo = transportInfoField.get(plan);
            if (transportInfo != null) {
                dto.transportInfo = transportInfo.toString();
            }
            
            java.lang.reflect.Field isPublicField = plan.getClass().getDeclaredField("isPublic");
            isPublicField.setAccessible(true);
            dto.isPublic = (Boolean) isPublicField.get(plan);
            
            java.lang.reflect.Field totalCostField = plan.getClass().getDeclaredField("totalCost");
            totalCostField.setAccessible(true);
            dto.totalCost = (Integer) totalCostField.get(plan);
            
            java.lang.reflect.Field memoField = plan.getClass().getDeclaredField("memo");
            memoField.setAccessible(true);
            dto.memo = (String) memoField.get(plan);
            
            java.lang.reflect.Field createdAtField = plan.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            dto.createdAt = (LocalDateTime) createdAtField.get(plan);
            
            java.lang.reflect.Field updatedAtField = plan.getClass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            dto.updatedAt = (LocalDateTime) updatedAtField.get(plan);
            
            java.lang.reflect.Field groupIdField = plan.getClass().getDeclaredField("groupId");
            groupIdField.setAccessible(true);
            dto.groupId = (Integer) groupIdField.get(plan);
            
            dto.isGroup = (dto.groupId != null);
            dto.places = places;
        } catch (Exception e) {
            // 리플렉션 실패 시 기본값으로 설정
            dto.isGroup = false;
        }
        
        return dto;
    }
} 