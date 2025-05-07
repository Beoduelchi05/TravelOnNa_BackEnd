package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.travelonna.demo.domain.plan.entity.Plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
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
        
        try {
            // 리플렉션 대신 직접 getter 메소드 호출
            dto.planId = plan.getPlanId();
            dto.userId = plan.getUserId();
            dto.title = plan.getTitle();
            dto.location = plan.getLocation();
            dto.startDate = plan.getStartDate();
            dto.endDate = plan.getEndDate();
            
            if (plan.getTransportInfo() != null) {
                dto.transportInfo = plan.getTransportInfo().toString();
            }
            
            dto.isPublic = plan.getIsPublic();
            dto.totalCost = plan.getTotalCost();
            dto.memo = plan.getMemo();
            dto.createdAt = plan.getCreatedAt();
            dto.updatedAt = plan.getUpdatedAt();
            
            // 그룹 ID 및 그룹 일정 여부 설정
            dto.groupId = plan.getGroupId();
            dto.isGroup = (dto.groupId != null);
            
            // 장소 목록 설정
            dto.places = places;
            
            // 디버깅 로그
            System.out.println("DTO 변환 완료: planId=" + dto.planId + 
                              ", groupId=" + dto.groupId + 
                              ", isGroup=" + dto.isGroup);
        } catch (Exception e) {
            System.out.println("DTO 변환 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 오류 발생 시 기본값 설정
            if (dto.isGroup == null) {
                dto.isGroup = false;
            }
        }
        
        return dto;
    }
} 