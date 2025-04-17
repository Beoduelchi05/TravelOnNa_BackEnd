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
            // 교통 수단이 train인 경우 역 ID로 열차 시간표 조회
            if (transportType == TransportInfo.train) {
                log.info("열차 시간표 조회 시작");
                return searchTrainSchedule(source, destination, date);
            }
            
            // train이 아닌 경우에는 현재 지원하지 않음
            log.warn("지원하지 않는 교통 수단: {}", transportTypeStr);
            return createEmptyResponse(source, destination, date, transportTypeStr);
            
        } catch (Exception e) {
            log.error("교통편 검색 중 오류 발생", e);
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
        log.info("열차 시간표 검색: 출발역 {}, 도착역 {}, 날짜 {}", source, destination, date);
        
        // 출발역 ID 조회
        Map<String, Object> sourceStationInfo = getStationIdByName(source);
        if (sourceStationInfo.isEmpty() || !sourceStationInfo.containsKey("stationID")) {
            log.warn("출발역 ID 조회 실패: {}", source);
            return createEmptyResponse(source, destination, date, "train");
        }
        String sourceStationId = sourceStationInfo.get("stationID").toString();
        log.info("출발역 ID 조회 성공: {} -> {}", source, sourceStationId);
        
        // 도착역 ID 조회
        Map<String, Object> destStationInfo = getStationIdByName(destination);
        if (destStationInfo.isEmpty() || !destStationInfo.containsKey("stationID")) {
            log.warn("도착역 ID 조회 실패: {}", destination);
            return createEmptyResponse(source, destination, date, "train");
        }
        String destStationId = destStationInfo.get("stationID").toString();
        log.info("도착역 ID 조회 성공: {} -> {}", destination, destStationId);
        
        // 열차 시간표 조회
        List<Map<String, Object>> trainSchedules = getTrainServiceTime(sourceStationId, destStationId);
        log.info("열차 시간표 조회 결과: {}개의 운행정보", trainSchedules.size());
        
        if (trainSchedules.isEmpty()) {
            log.warn("열차 시간표 없음");
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
                        log.warn("운행시간 형식 오류: {}", runningTime);
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
                JsonNode resultNode = response.get("result");
                
                if (resultNode == null || resultNode.isNull()) {
                    log.warn("역 ID 조회 실패: result 노드가 null입니다");
                    return result;
                }
                
                if (!resultNode.isArray()) {
                    log.warn("역 ID 조회 실패: result 노드가 배열이 아닙니다");
                    return result;
                }
                
                int stationCount = resultNode.size();
                log.info("조회된 역 수: {}", stationCount);
                
                if (stationCount > 0) {
                    JsonNode station = resultNode.get(0);
                    
                    if (!station.has("stationID") || !station.has("stationName") || 
                        !station.has("x") || !station.has("y")) {
                        log.warn("역 ID 조회 실패: 필수 필드(stationID, stationName, x, y)가 없습니다");
                        return result;
                    }
                    
                    result.put("stationID", station.get("stationID").asText());
                    result.put("stationName", station.get("stationName").asText());
                    result.put("x", station.get("x").asDouble());
                    result.put("y", station.get("y").asDouble());
                    
                    log.info("첫 번째 역 정보: ID={}, 이름={}", 
                        station.get("stationID").asText(), 
                        station.get("stationName").asText());
                    
                    // arrivalTerminals 정보 처리는 제외 (사용하지 않음)
                } else {
                    log.warn("역 ID 조회 실패: 조회된 역 정보가 없습니다");
                }
            } else {
                log.warn("역 ID 조회 실패: result 필드가 없습니다");
            }
        } catch (Exception e) {
            log.error("역 ID 조회 중 오류 발생", e);
            e.printStackTrace();
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
                JsonNode resultNode = response.get("result");
                
                // station 필드 확인 (신규 API 응답 형식)
                if (resultNode.has("station") && resultNode.get("station").isArray()) {
                    JsonNode stations = resultNode.get("station");
                    log.info("조회된 기차 시간표 수: {}", stations.size());
                    
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
                                log.warn("요금 파싱 오류: {}", station.get("fare").get("general").asText());
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
                        
                        log.info("기차 시간표 항목: {} {}→{} (소요: {}, 요금: {}원)", 
                            trainClass, departureTime, arrivalTime, wasteTime, fare);
                        
                        result.add(serviceInfo);
                    }
                    return result;
                }
                
                // trainServices 필드 확인 (기존 API 응답 형식 처리)
                if (resultNode.has("trainServices")) {
                    JsonNode trainServices = resultNode.get("trainServices");
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
                    log.warn("기차 시간표 조회 실패: trainServices나 station 필드가 없습니다");
                }
            } else {
                log.warn("기차 시간표 조회 실패: result 필드가 없습니다");
            }
        } catch (Exception e) {
            log.error("기차 시간표 조회 중 오류 발생", e);
            e.printStackTrace();
        }
        
        log.info("기차 시간표 조회 완료: 총 {}개 항목", result.size());
        return result;
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