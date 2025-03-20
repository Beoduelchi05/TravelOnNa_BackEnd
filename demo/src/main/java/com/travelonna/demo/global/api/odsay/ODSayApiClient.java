package com.travelonna.demo.global.api.odsay;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ODSayApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${odsay.api.key.server}")
    private String apiKey;
    
    private static final String BASE_URL = "https://api.odsay.com/v1/api";
    
    /**
     * 출발지와 도착지로 대중교통 정보 조회
     * 
     * @param srcName 출발지 이름
     * @param dstName 도착지 이름
     * @param date 출발 날짜
     * @param transportType 이동 수단 타입(car, bus, train, etc)
     * @return API 응답 (JSON)
     */
    public JsonNode searchTransportation(String srcName, String dstName, LocalDate date, String transportType) {
        log.info("ODSay API 호출: 출발지 {}, 도착지 {}, 날짜 {}, 이동수단 {}", srcName, dstName, date, transportType);
        
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/searchPubTransPath")
                .queryParam("apiKey", apiKey)
                .queryParam("SX", "") // 출발지 X좌표(경도) - 실제 사용 시 좌표 변환 필요
                .queryParam("SY", "") // 출발지 Y좌표(위도) - 실제 사용 시 좌표 변환 필요
                .queryParam("EX", "") // 도착지 X좌표(경도) - 실제 사용 시 좌표 변환 필요
                .queryParam("EY", "") // 도착지 Y좌표(위도) - 실제 사용 시 좌표 변환 필요
                .queryParam("OPT", getOptByTransportType(transportType)) // 대중교통 수단 옵션
                .queryParam("SearchPathType", "0") // 경로 검색 유형
                .queryParam("SearchDate", formattedDate) // 출발 날짜
                .build()
                .toUri();
        
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);
        
        return response.getBody();
    }
    
    /**
     * 교통 수단 타입에 따른 ODSay API 옵션값 반환
     */
    private String getOptByTransportType(String transportType) {
        if (transportType == null) {
            return "0"; // 전체
        }
        
        return switch (transportType) {
            case "bus" -> "1"; // 버스
            case "train" -> "2"; // 기차
            case "car" -> "4"; // 자동차
            default -> "0"; // 기타(전체)
        };
    }
} 