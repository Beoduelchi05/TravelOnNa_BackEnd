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
import com.fasterxml.jackson.databind.ObjectMapper;

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
        log.info("API Key(원본): {}", apiKey);
        
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        try {
            // API Key 인코딩 - 특수문자 처리 강화
            String encodedApiKey = apiKey.replace("+", "%2B").replace("/", "%2F").replace("=", "%3D");
            String encodedSrcName = java.net.URLEncoder.encode(srcName, "UTF-8");
            String encodedDstName = java.net.URLEncoder.encode(dstName, "UTF-8");
            
            log.info("API Key(인코딩): {}", encodedApiKey);
            
            // URL 생성
            String url = BASE_URL + "/searchPubTransPath" + 
                         "?apiKey=" + encodedApiKey + 
                         "&SX=" + "" + 
                         "&SY=" + "" + 
                         "&EX=" + "" + 
                         "&EY=" + "" + 
                         "&OPT=" + getOptByTransportType(transportType) + 
                         "&SearchPathType=0" + 
                         "&SearchDate=" + formattedDate;
            
            log.info("전체 API URL: {}", url);
            
            // HTTP 요청 헤더 추가
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36");
            headers.set("Accept", "application/json");
            
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // API 호출
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
            );
            
            if (rawResponse.getStatusCode().is2xxSuccessful()) {
                log.info("ODSay API 응답(원본): {}", rawResponse.getBody());
                
                // 응답이 null이나 비어있는지 확인
                if (rawResponse.getBody() == null || rawResponse.getBody().isEmpty()) {
                    log.error("응답 본문이 비어 있습니다.");
                    return null;
                }
                
                // ObjectMapper를 사용하여 String을 JsonNode로 변환
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(rawResponse.getBody());
                
                // 에러 응답인지 확인
                if (jsonResponse.has("error")) {
                    log.error("ODSay API 에러 응답: {}", jsonResponse.get("error"));
                }
                
                return jsonResponse;
            } else {
                log.error("ODSay API 응답 코드 오류: {}", rawResponse.getStatusCodeValue());
                return null;
            }
        } catch (Exception e) {
            log.error("대중교통 정보 조회 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 터미널/역 ID 조회
     * 
     * @param terminalName 터미널/역 이름 (예: "대구", "서울", "동대구" 등)
     * @param lang 언어 코드 (0: 국문)
     * @return API 응답 (JSON)
     */
    public JsonNode getTrainTerminals(String terminalName, String lang) {
        log.info("ODSay API 호출: 터미널/역 검색 {}, 언어 {}", terminalName, lang);
        log.info("API Key(원본): {}", apiKey);
        
        try {
            // 로컬 IP 정보 가져오기
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String hostAddress = localHost.getHostAddress();
            log.info("호출 호스트: {} ({})", hostName, hostAddress);
            
            // API Key 인코딩 - 특수문자 처리 강화
            String encodedApiKey = apiKey.replace("+", "%2B").replace("/", "%2F").replace("=", "%3D");
            
            log.info("API Key(인코딩): {}", encodedApiKey);
            
            // URL 생성 - terminalName 인코딩 제거
            String urlStr = BASE_URL + "/trainTerminals" + 
                        "?apiKey=" + encodedApiKey + 
                        "&terminalName=" + terminalName +
                        "&lang=" + lang;
            
            log.info("전체 API URL: {}", urlStr);
            
            // HTTP 요청 헤더 추가
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36");
            headers.set("Accept", "application/json");
            headers.set("X-Forwarded-For", hostAddress);
            headers.set("Origin", "http://travelonna.shop");
            headers.set("Referer", "http://travelonna.shop/");
            
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // API 호출
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                urlStr,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
            );
            
            if (rawResponse.getStatusCode().is2xxSuccessful()) {
                log.info("ODSay API 응답(원본): {}", rawResponse.getBody());
                
                // 응답이 null이나 비어있는지 확인
                if (rawResponse.getBody() == null || rawResponse.getBody().isEmpty()) {
                    log.error("응답 본문이 비어 있습니다.");
                    return null;
                }
                
                // ObjectMapper를 사용하여 String을 JsonNode로 변환
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(rawResponse.getBody());
                
                // 에러 응답인지 확인
                if (jsonResponse.has("error")) {
                    log.error("ODSay API 에러 응답: {}", jsonResponse.get("error"));
                }
                
                return jsonResponse;
            } else {
                log.error("ODSay API 응답 코드 오류: {}", rawResponse.getStatusCodeValue());
                return null;
            }
        } catch (Exception e) {
            log.error("터미널/역 검색 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 기차 시간표 조회
     * 
     * @param startStationID 출발역 ID
     * @param endStationID 도착역 ID
     * @param lang 언어 코드 (0: 국문)
     * @return API 응답 (JSON)
     */
    public JsonNode getTrainServiceTime(String startStationID, String endStationID, String lang) {
        log.info("ODSay API 호출: 기차 시간표 조회 출발역ID {}, 도착역ID {}, 언어 {}", startStationID, endStationID, lang);
        log.info("API Key(원본): {}", apiKey);
        
        try {
            // API Key 인코딩 - 특수문자 처리 강화
            String encodedApiKey = apiKey.replace("+", "%2B").replace("/", "%2F").replace("=", "%3D");
            
            log.info("API Key(인코딩): {}", encodedApiKey);
            
            // URL 생성
            String url = BASE_URL + "/trainServiceTime" + 
                         "?apiKey=" + encodedApiKey + 
                         "&startStationID=" + startStationID + 
                         "&endStationID=" + endStationID;
            
            log.info("전체 API URL: {}", url);
            
            // HTTP 요청 헤더 추가
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36");
            headers.set("Accept", "application/json");
            
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // API 호출
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
            );
            
            if (rawResponse.getStatusCode().is2xxSuccessful()) {
                log.info("ODSay API 응답(원본): {}", rawResponse.getBody());
                
                // 응답이 null이나 비어있는지 확인
                if (rawResponse.getBody() == null || rawResponse.getBody().isEmpty()) {
                    log.error("응답 본문이 비어 있습니다.");
                    return null;
                }
                
                // ObjectMapper를 사용하여 String을 JsonNode로 변환
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(rawResponse.getBody());
                
                // 에러 응답인지 확인
                if (jsonResponse.has("error")) {
                    log.error("ODSay API 에러 응답: {}", jsonResponse.get("error"));
                }
                
                return jsonResponse;
            } else {
                log.error("ODSay API 응답 코드 오류: {}", rawResponse.getStatusCodeValue());
                return null;
            }
        } catch (Exception e) {
            log.error("기차 시간표 조회 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
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