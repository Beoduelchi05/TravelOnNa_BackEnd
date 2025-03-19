package com.travelonna.demo.global.api.odsay.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportationResponseDto {
    
    private String source; // 출발지
    private String destination; // 목적지
    private String departureDate; // 출발 날짜
    private String transportType; // 교통 수단 타입
    private List<TransportationOption> options; // 교통편 옵션 목록
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransportationOption {
        private String type; // 교통 수단 유형 (버스, 기차 등)
        private String departureTime; // 출발 시간
        private String arrivalTime; // 도착 시간
        private Integer totalTime; // 총 소요 시간 (분)
        private Integer price; // 요금
        private String routeInfo; // 경로 정보 요약
    }
} 