package com.travelonna.demo.global.api.odsay;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.travelonna.demo.domain.plan.entity.TransportInfo;
import com.travelonna.demo.global.api.odsay.dto.TransportationResponseDto;
import com.travelonna.demo.global.api.odsay.dto.TransportationResponseDto.TransportationOption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ODSayTransportService {

    private final ODSayApiClient odSayApiClient;
    
    /**
     * 출발지와 도착지, 날짜, 교통수단으로 교통편 검색
     * 
     * @param source 출발지
     * @param destination 도착지
     * @param date 출발 날짜
     * @param transportType 교통 수단 유형
     * @return 교통편 검색 결과
     */
    public TransportationResponseDto searchTransportation(String source, String destination, LocalDate date, TransportInfo transportType) {
        String transportTypeStr = transportType != null ? transportType.name() : null;
        
        log.info("교통편 검색: 출발지 {}, 도착지 {}, 날짜 {}, 교통수단 {}", source, destination, date, transportTypeStr);
        
        try {
            JsonNode response = odSayApiClient.searchTransportation(source, destination, date, transportTypeStr);
            
            if (response == null || !response.has("result")) {
                log.warn("교통편 검색 실패: 응답 없음 또는, result 필드 없음");
                return createEmptyResponse(source, destination, date, transportTypeStr);
            }
            
            JsonNode result = response.get("result");
            List<TransportationOption> options = parseTransportationOptions(result, transportTypeStr);
            
            return TransportationResponseDto.builder()
                    .source(source)
                    .destination(destination)
                    .departureDate(date.format(DateTimeFormatter.ISO_DATE))
                    .transportType(transportTypeStr)
                    .options(options)
                    .build();
            
        } catch (Exception e) {
            log.error("교통편 검색 중 오류 발생", e);
            return createEmptyResponse(source, destination, date, transportTypeStr);
        }
    }
    
    /**
     * API 응답에서 교통편 옵션 파싱
     */
    private List<TransportationOption> parseTransportationOptions(JsonNode result, String transportType) {
        List<TransportationOption> options = new ArrayList<>();
        
        if (result.has("path")) {
            JsonNode paths = result.get("path");
            
            for (JsonNode path : paths) {
                TransportationOption option = parseTransportationOption(path, transportType);
                options.add(option);
            }
        }
        
        return options;
    }
    
    /**
     * 단일 교통편 옵션 파싱
     */
    private TransportationOption parseTransportationOption(JsonNode path, String transportType) {
        JsonNode info = path.get("info");
        
        int totalTime = info.has("totalTime") ? info.get("totalTime").asInt() : 0;
        int payment = info.has("payment") ? info.get("payment").asInt() : 0;
        
        StringBuilder routeInfo = new StringBuilder();
        
        if (path.has("subPath")) {
            for (JsonNode subPath : path.get("subPath")) {
                if (subPath.has("trafficType")) {
                    int trafficType = subPath.get("trafficType").asInt();
                    String trafficName = getTrafficTypeName(trafficType);
                    
                    if (subPath.has("sectionTime")) {
                        int sectionTime = subPath.get("sectionTime").asInt();
                        routeInfo.append(trafficName).append(" (").append(sectionTime).append("분), ");
                    }
                }
            }
        }
        
        if (routeInfo.length() > 2) {
            routeInfo.delete(routeInfo.length() - 2, routeInfo.length());
        }
        
        return TransportationOption.builder()
                .type(transportType)
                .departureTime("N/A") // API 응답에서 출발 시간 파싱 필요
                .arrivalTime("N/A") // API 응답에서 도착 시간 파싱 필요
                .totalTime(totalTime)
                .price(payment)
                .routeInfo(routeInfo.toString())
                .build();
    }
    
    /**
     * 교통 수단 유형 번호에 따른 이름 반환
     */
    private String getTrafficTypeName(int trafficType) {
        return switch (trafficType) {
            case 1 -> "지하철";
            case 2 -> "버스";
            case 3 -> "도보";
            case 4 -> "기차";
            case 5 -> "항공";
            case 6 -> "페리";
            case 7 -> "택시";
            default -> "기타";
        };
    }
    
    /**
     * 빈 응답 생성
     */
    private TransportationResponseDto createEmptyResponse(String source, String destination, LocalDate date, String transportType) {
        return TransportationResponseDto.builder()
                .source(source)
                .destination(destination)
                .departureDate(date.format(DateTimeFormatter.ISO_DATE))
                .transportType(transportType)
                .options(new ArrayList<>())
                .build();
    }
} 