package com.travelonna.demo.global.api.odsay;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 역 이름으로 역 ID 조회
     * 
     * @param terminalName 역 이름(예: "대구", "서울")
     * @return 역 ID, 역 이름, 좌표 등의 정보를 포함한 Map (역을 찾을 수 없는 경우 빈 Map 반환)
     */
    public Map<String, Object> getStationIdByName(String terminalName) {
        log.info("역 ID 조회 시작: {}", terminalName);
        Map<String, Object> result = new HashMap<>();
        
        try {
            JsonNode response = odSayApiClient.getTrainTerminals(terminalName, "0");
            log.info("역 ID 조회 응답: {}", response);
            
            if (response == null) {
                log.warn("역 ID 조회 실패: 응답이 null입니다");
                return result;
            }
            
            if (response.has("error")) {
                log.warn("역 ID 조회 실패: API 에러 - {}", response.get("error"));
                return result;
            }
            
            if (response.has("result")) {
                JsonNode stations = response.get("result").get("stations");
                log.info("조회된 역 수: {}", stations.size());
                
                if (stations != null && stations.isArray() && stations.size() > 0) {
                    JsonNode station = stations.get(0);
                    
                    result.put("stationID", station.get("stationID").asText());
                    result.put("stationName", station.get("stationName").asText());
                    result.put("x", station.get("x").asDouble());
                    result.put("y", station.get("y").asDouble());
                    
                    log.info("첫 번째 역 정보: ID={}, 이름={}", 
                        station.get("stationID").asText(), 
                        station.get("stationName").asText());
                    
                    // 도착 터미널 정보가 있는 경우 추가
                    if (station.has("haveDestinationTerminals") && 
                        station.get("haveDestinationTerminals").asBoolean() && 
                        station.has("arrivalTerminals")) {
                        
                        List<Map<String, Object>> arrivalTerminals = new ArrayList<>();
                        for (JsonNode arrivalTerminal : station.get("arrivalTerminals")) {
                            Map<String, Object> terminalInfo = new HashMap<>();
                            terminalInfo.put("stationID", arrivalTerminal.get("stationID").asText());
                            terminalInfo.put("stationName", arrivalTerminal.get("stationName").asText());
                            terminalInfo.put("x", arrivalTerminal.get("x").asDouble());
                            terminalInfo.put("y", arrivalTerminal.get("y").asDouble());
                            arrivalTerminals.add(terminalInfo);
                        }
                        result.put("arrivalTerminals", arrivalTerminals);
                        log.info("도착 가능 역 수: {}", arrivalTerminals.size());
                    }
                } else {
                    log.warn("역 ID 조회 실패: 조회된 역 정보가 없습니다");
                }
            } else {
                log.warn("역 ID 조회 실패: result 필드가 없습니다");
            }
        } catch (Exception e) {
            log.error("역 ID 조회 중 오류 발생", e);
        }
        
        log.info("역 ID 조회 완료: {} -> {}", terminalName, result);
        return result;
    }
    
    /**
     * 출발역과 도착역 ID로 기차 시간표 조회
     * 
     * @param startStationID 출발역 ID
     * @param endStationID 도착역 ID
     * @return 기차 시간표 정보를 포함한 Map
     */
    public List<Map<String, Object>> getTrainServiceTime(String startStationID, String endStationID) {
        log.info("기차 시간표 조회 시작: 출발역 ID {}, 도착역 ID {}", startStationID, endStationID);
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            JsonNode response = odSayApiClient.getTrainServiceTime(startStationID, endStationID, "0");
            log.info("기차 시간표 조회 응답: {}", response);
            
            if (response == null) {
                log.warn("기차 시간표 조회 실패: 응답이 null입니다");
                return result;
            }
            
            if (response.has("error")) {
                log.warn("기차 시간표 조회 실패: API 에러 - {}", response.get("error"));
                return result;
            }
            
            if (response.has("result")) {
                if (!response.get("result").has("trainServices")) {
                    log.warn("기차 시간표 조회 실패: trainServices 필드가 없습니다");
                    return result;
                }
                
                JsonNode trainServices = response.get("result").get("trainServices");
                log.info("조회된 기차 시간표 수: {}", trainServices.size());
                
                if (trainServices != null && trainServices.isArray()) {
                    for (JsonNode service : trainServices) {
                        Map<String, Object> serviceInfo = new HashMap<>();
                        
                        String traingradeName = service.has("traingradeName") ? service.get("traingradeName").asText() : "";
                        String departureTime = service.has("departureTime") ? service.get("departureTime").asText() : "";
                        String arrivalTime = service.has("arrivalTime") ? service.get("arrivalTime").asText() : "";
                        String runningTime = service.has("runningTime") ? service.get("runningTime").asText() : "";
                        int fare = service.has("fare") ? service.get("fare").asInt() : 0;
                        
                        serviceInfo.put("traingradeName", traingradeName);
                        serviceInfo.put("departureTime", departureTime);
                        serviceInfo.put("arrivalTime", arrivalTime);
                        serviceInfo.put("runningTime", runningTime);
                        serviceInfo.put("fare", fare);
                        
                        log.info("기차 시간표 항목: {} {}→{} (소요: {}, 요금: {}원)", 
                            traingradeName, departureTime, arrivalTime, runningTime, fare);
                        
                        result.add(serviceInfo);
                    }
                } else {
                    log.warn("기차 시간표 조회 실패: 조회된 시간표가 없거나 배열이 아닙니다");
                }
            } else {
                log.warn("기차 시간표 조회 실패: result 필드가 없습니다");
            }
        } catch (Exception e) {
            log.error("기차 시간표 조회 중 오류 발생", e);
        }
        
        log.info("기차 시간표 조회 완료: 총 {}개 항목", result.size());
        return result;
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