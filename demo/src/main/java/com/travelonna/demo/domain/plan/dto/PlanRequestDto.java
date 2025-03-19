package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.travelonna.demo.domain.plan.entity.TransportInfo;

public class PlanRequestDto {
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePlanDto {
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
        
        @Schema(description = "공개 여부", example = "true")
        private Boolean isPublic;
        
        @Schema(description = "총 비용", example = "500000")
        private Integer totalCost;
        
        @Schema(description = "메모", example = "준비물: 수영복, 선글라스")
        private String memo;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateLocationDto {
        @Schema(description = "여행지", example = "제주도")
        private String location;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateTransportDto {
        @Schema(description = "이동 수단", example = "car")
        private TransportInfo transportInfo;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateCostDto {
        @Schema(description = "총 비용", example = "500000")
        private Integer totalCost;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchTransportationDto {
        @Schema(description = "출발지", example = "서울")
        private String source;
        
        @Schema(description = "도착지", example = "부산")
        private String destination;
        
        @Schema(description = "출발 날짜", example = "2024-04-01")
        private LocalDate departureDate;
        
        @Schema(description = "이동 수단", example = "car")
        private TransportInfo transportType;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdatePlanDto {
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
        
        @Schema(description = "공개 여부", example = "true")
        private Boolean isPublic;
        
        @Schema(description = "총 비용", example = "500000")
        private Integer totalCost;
        
        @Schema(description = "메모", example = "준비물: 수영복, 선글라스")
        private String memo;
    }
} 