package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.travelonna.demo.domain.plan.entity.Place;

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
@Schema(description = "장소 응답 DTO")
public class PlaceResponseDto {
    
    @Schema(description = "장소 ID", example = "1")
    private Integer id;
    
    @Schema(description = "장소 이름", example = "동대구역")
    private String name;
    
    @Schema(description = "장소 주소", example = "대구광역시 동구 동대구로 550")
    private String address;
    
    @Schema(description = "순서", example = "1")
    private Integer order;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
    
    @Schema(description = "방문 날짜", example = "2023-11-20T00:00:00Z")
    private LocalDateTime visitDate;
    
    @Schema(description = "일차", example = "1")
    private Integer day;
    
    @Schema(description = "장소 비용", example = "40000")
    private Integer cost;
    
    @Schema(description = "메모", example = "장소 메모 내용")
    private String memo;
    
    @Schema(description = "위도", example = "35.855415")
    private String lat;
    
    @Schema(description = "경도", example = "128.492514")
    private String lon;
    
    public static PlaceResponseDto fromEntity(Place place) {
        return PlaceResponseDto.builder()
                .id(place.getPlaceId())
                .name(place.getName())
                .address(place.getPlace())
                .order(place.getOrder())
                .isPublic(place.getIsPublic())
                .visitDate(place.getVisitDate())
                .cost(place.getPlaceCost())
                .memo(place.getMemo())
                .lat(place.getLat())
                .lon(place.getLon())
                .build();
    }
    
    public static PlaceResponseDto fromEntityWithDay(Place place) {
        Integer day = null;
        
        if (place.getPlan().getStartDate() != null && place.getVisitDate() != null) {
            // Plan의 startDate는 LocalDate, Place의 visitDate는 LocalDateTime이므로 변환 필요
            day = (int) ChronoUnit.DAYS.between(
                place.getPlan().getStartDate(), 
                place.getVisitDate().toLocalDate()
            ) + 1; // 시작 날짜를 day 1로 계산
        }
        
        return PlaceResponseDto.builder()
                .id(place.getPlaceId())
                .name(place.getName())
                .address(place.getPlace())
                .order(place.getOrder())
                .isPublic(place.getIsPublic())
                .visitDate(place.getVisitDate())
                .day(day)
                .cost(place.getPlaceCost())
                .memo(place.getMemo())
                .lat(place.getLat())
                .lon(place.getLon())
                .build();
    }
} 