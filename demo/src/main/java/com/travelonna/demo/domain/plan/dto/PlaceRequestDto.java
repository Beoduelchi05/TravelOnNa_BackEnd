package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PlaceRequestDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소 생성 요청 DTO")
    public static class CreatePlaceDto {
        
        @Schema(description = "장소 주소", example = "대구광역시 달서구 계대동문로 2")
        private String place;
        
        @Schema(description = "공개 여부", example = "true")
        private Boolean isPublic;
        
        @Schema(description = "방문 날짜", example = "2023-11-20")
        private LocalDate visitDate;
        
        @Schema(description = "장소 비용 (일정 총 비용에 자동 합산됨)", example = "40000")
        private Integer placeCost;
        
        @Schema(description = "메모", example = "장소 메모 내용")
        private String memo;
        
        @Schema(description = "위도", example = "35.855415")
        private String lat;
        
        @Schema(description = "경도", example = "128.492514")
        private String lon;

        @Schema(description = "장소 이름", example = "계명대학교")
        private String name;

        @Schema(description = "순서", example = "1")
        private Integer order;

        @Schema(description = "구글 ID", example = "ChIJCZ4FKFblZTURKU0R_4aNSek")
        private String googleId;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "일정 생성 후 장소 추가 요청 DTO")
    public static class AddPlaceDto {
        
        @Schema(description = "일차", example = "1")
        private Integer dayNumber;
        
        @Schema(description = "장소 이름", example = "동대구역")
        private String name;
        
        @Schema(description = "장소 주소", example = "대구광역시 동구 동대구로 550")
        private String address;
        
        @Schema(description = "순서", example = "1")
        private Integer order;

        @Schema(description = "구글 ID", example = "ChIJCZ4FKFblZTURKU0R_4aNSek")
        private String googleId;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소 수정 요청 DTO")
    public static class UpdatePlaceDto {
        
        @Schema(description = "장소 주소", example = "대구광역시 달서구 계대동문로 2")
        private String place;
        
        @Schema(description = "공개 여부", example = "true")
        private Boolean isPublic;
        
        @Schema(description = "방문 날짜", example = "2023-11-20")
        private LocalDate visitDate;
        
        @Schema(description = "장소 비용 (일정 총 비용에 자동 합산됨)", example = "40000")
        private Integer placeCost;
        
        @Schema(description = "메모", example = "수정된 장소 메모 내용")
        private String memo;
        
        @Schema(description = "위도", example = "35.855415")
        private String lat;
        
        @Schema(description = "경도", example = "128.492514")
        private String lon;
        
        @Schema(description = "장소 이름", example = "동대구역")
        private String name;
        
        @Schema(description = "순서", example = "1")
        private Integer order;

        @Schema(description = "구글 ID", example = "ChIJCZ4FKFblZTURKU0R_4aNSek")
        private String googleId;
    }
} 