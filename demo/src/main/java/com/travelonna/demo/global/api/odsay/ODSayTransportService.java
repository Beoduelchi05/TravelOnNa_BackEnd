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
        
        try {
            // 교통 수단이 train인 경우 역 ID로 열차 시간표 조회
            if (transportType == TransportInfo.train) {
                return searchTrainSchedule(source, destination, date);
            }
            
            // train이 아닌 경우에는 현재 지원하지 않음
            return createEmptyResponse(source, destination, date, transportTypeStr);
            
        } catch (Exception e) {
            return createEmptyResponse(source, destination, date, transportTypeStr);
        }
    }
    
    /**
     * 열차 시간표 검색 (역 ID 조회 -> 시간표 조회)
     * 
     * @param source 출발역 이름
     * @param destination 도착역 이름
     * @param date 출발 날짜
     * @return 열차 시간표 정보
     */
    private TransportationResponseDto searchTrainSchedule(String source, String destination, LocalDate date) {
        // 출발역 ID 조회
        Map<String, Object> sourceStationInfo = getStationIdByName(source);
        if (sourceStationInfo.isEmpty() || !sourceStationInfo.containsKey("stationID")) {
            return createEmptyResponse(source, destination, date, "train");
        }
        String sourceStationId = sourceStationInfo.get("stationID").toString();
        
        // 도착역 ID 조회
        Map<String, Object> destStationInfo = getStationIdByName(destination);
        if (destStationInfo.isEmpty() || !destStationInfo.containsKey("stationID")) {
            return createEmptyResponse(source, destination, date, "train");
        }
        String destStationId = destStationInfo.get("stationID").toString();
        
        // 열차 시간표 조회
        List<Map<String, Object>> trainSchedules = getTrainServiceTime(sourceStationId, destStationId);
        
        if (trainSchedules.isEmpty()) {
            return createEmptyResponse(source, destination, date, "train");
        }
        
        // 응답 DTO에 매핑
        List<TransportationOption> options = new ArrayList<>();
        for (Map<String, Object> schedule : trainSchedules) {
            String traingradeName = (String) schedule.getOrDefault("traingradeName", "");
            String departureTime = (String) schedule.getOrDefault("departureTime", "");
            String arrivalTime = (String) schedule.getOrDefault("arrivalTime", "");
            String runningTime = (String) schedule.getOrDefault("runningTime", "");
            Integer fare = schedule.containsKey("fare") ? ((Number) schedule.get("fare")).intValue() : 0;
            
            // 추가 정보를 routeInfo에 포함
            StringBuilder routeInfoBuilder = new StringBuilder();
            routeInfoBuilder.append(traingradeName);
            
            if (schedule.containsKey("railName")) {
                routeInfoBuilder.append(" (").append(schedule.get("railName")).append(")");
            }
            
            if (schedule.containsKey("trainNo")) {
                routeInfoBuilder.append(" ").append(schedule.get("trainNo")).append("호");
            }
            
            // 운행일 정보 추가
            if (schedule.containsKey("runDay")) {
                routeInfoBuilder.append(" [").append(schedule.get("runDay")).append("]");
            }
            
            String routeInfo = routeInfoBuilder.toString();
            
            // 운행시간을 분으로 변환 (HH:MM 형식)
            Integer totalMinutes = 0;
            if (runningTime != null && !runningTime.isEmpty()) {
                String[] parts = runningTime.split(":");
                if (parts.length == 2) {
                    try {
                        int hours = Integer.parseInt(parts[0]);
                        int minutes = Integer.parseInt(parts[1]);
                        totalMinutes = hours * 60 + minutes;
                    } catch (NumberFormatException e) {
                        // 운행시간 형식 오류
                    }
                }
            }
            
            TransportationOption option = TransportationOption.builder()
                    .type("train")
                    .departureTime(departureTime)
                    .arrivalTime(arrivalTime)
                    .totalTime(totalMinutes)
                    .price(fare)
                    .routeInfo(routeInfo)
                    .build();
            
            options.add(option);
        }
        
        return TransportationResponseDto.builder()
                .source(source)
                .destination(destination)
                .departureDate(date.format(DateTimeFormatter.ISO_DATE))
                .transportType("train")
                .options(options)
                .build();
    }
    
    /**
     * 역 이름으로 역 ID 조회
     * 
     * @param terminalName 역 이름(예: "대구", "서울")
     * @return 역 ID, 역 이름, 좌표 등의 정보를 포함한 Map (역을 찾을 수 없는 경우 빈 Map 반환)
     */
    public Map<String, Object> getStationIdByName(String terminalName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JsonNode response = odSayApiClient.getTrainTerminals(terminalName, "0");
            
            if (response == null) {
                return result;
            }
            
            if (response.has("error")) {
                return result;
            }
            
            if (response.has("result")) {
                JsonNode resultNode = response.get("result");
                
                if (resultNode == null || resultNode.isNull()) {
                    return result;
                }
                
                if (!resultNode.isArray()) {
                    return result;
                }
                
                int stationCount = resultNode.size();
                
                if (stationCount > 0) {
                    JsonNode station = resultNode.get(0);
                    
                    if (!station.has("stationID") || !station.has("stationName") || 
                        !station.has("x") || !station.has("y")) {
                        return result;
                    }
                    
                    result.put("stationID", station.get("stationID").asText());
                    result.put("stationName", station.get("stationName").asText());
                    result.put("x", station.get("x").asDouble());
                    result.put("y", station.get("y").asDouble());
                    
                    // arrivalTerminals 정보 처리는 제외 (사용하지 않음)
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 빈 결과 반환
        }
        
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
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            JsonNode response = odSayApiClient.getTrainServiceTime(startStationID, endStationID, "0");
            
            if (response == null) {
                return result;
            }
            
            if (response.has("error")) {
                return result;
            }
            
            if (response.has("result")) {
                JsonNode resultNode = response.get("result");
                
                // station 필드 확인 (신규 API 응답 형식)
                if (resultNode.has("station") && resultNode.get("station").isArray()) {
                    JsonNode stations = resultNode.get("station");
                    
                    for (JsonNode station : stations) {
                        Map<String, Object> serviceInfo = new HashMap<>();
                        
                        // 필수 필드 추출
                        String trainClass = station.has("trainClass") ? station.get("trainClass").asText() : "";
                        String departureTime = station.has("departureTime") ? station.get("departureTime").asText() : "";
                        String arrivalTime = station.has("arrivalTime") ? station.get("arrivalTime").asText() : "";
                        String wasteTime = station.has("wasteTime") ? station.get("wasteTime").asText() : "";
                        
                        // fare 필드에서 일반석(general) 요금 정보 추출
                        int fare = 0;
                        if (station.has("fare") && station.get("fare").has("general")) {
                            try {
                                fare = Integer.parseInt(station.get("fare").get("general").asText());
                            } catch (NumberFormatException e) {
                                // 요금 파싱 오류
                            }
                        }
                        
                        // 추가 정보
                        String railName = station.has("railName") ? station.get("railName").asText() : "";
                        String trainNo = station.has("trainNo") ? station.get("trainNo").asText() : "";
                        String runDay = station.has("runDay") ? station.get("runDay").asText() : "";
                        
                        serviceInfo.put("traingradeName", trainClass);
                        serviceInfo.put("departureTime", departureTime);
                        serviceInfo.put("arrivalTime", arrivalTime);
                        serviceInfo.put("runningTime", wasteTime);
                        serviceInfo.put("fare", fare);
                        
                        // 추가 정보
                        serviceInfo.put("railName", railName);
                        serviceInfo.put("trainNo", trainNo);
                        serviceInfo.put("runDay", runDay);
                        
                        result.add(serviceInfo);
                    }
                    return result;
                }
                
                // trainServices 필드 확인 (기존 API 응답 형식 처리)
                if (resultNode.has("trainServices")) {
                    JsonNode trainServices = resultNode.get("trainServices");
                    
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
                            
                            result.add(serviceInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 빈 결과 반환
        }
        
        return result;
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