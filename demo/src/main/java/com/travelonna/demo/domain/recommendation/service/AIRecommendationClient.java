package com.travelonna.demo.domain.recommendation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${ai.recommendation.service.url:http://travelonna-ai-recommendation-service:8000}")
    private String aiServiceUrl;
    
    /**
     * AI 추천 서비스에서 실시간 추천 조회
     */
    public AIRecommendationResponse getRecommendations(Integer userId, String type, Integer limit) {
        try {
            String url = String.format("%s/api/v1/recommendations?userId=%d&type=%s&limit=%d", 
                                     aiServiceUrl, userId, type, limit);
            
            log.info("AI 추천 서비스 호출: {}", url);
            
            AIRecommendationResponse response = restTemplate.getForObject(url, AIRecommendationResponse.class);
            
            if (response != null && response.getRecommendations() != null) {
                log.info("AI 추천 서비스 응답 성공: userId={}, 추천 수={}", 
                        userId, response.getRecommendations().size());
                return response;
            } else {
                log.warn("AI 추천 서비스에서 빈 응답: userId={}", userId);
                return new AIRecommendationResponse(userId, type, List.of());
            }
            
        } catch (Exception e) {
            log.error("AI 추천 서비스 호출 실패: userId={}, error={}", userId, e.getMessage());
            // fallback: 빈 추천 목록 반환
            return new AIRecommendationResponse(userId, type, List.of());
        }
    }
    
    /**
     * AI 추천 서비스에서 배치 처리 트리거
     */
    public BatchTriggerResponse triggerBatch(String batchType) {
        try {
            String url = String.format("%s/api/v1/batch/trigger?batch_type=%s", aiServiceUrl, batchType);
            
            log.info("AI 추천 배치 트리거 호출: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BatchTriggerResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, BatchTriggerResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI 추천 배치 트리거 성공: batchType={}, success={}", 
                        batchType, response.getBody().isSuccess());
                return response.getBody();
            } else {
                log.warn("AI 추천 배치 트리거 응답 실패: batchType={}", batchType);
                return new BatchTriggerResponse(false, "AI 서비스 응답 오류", batchType, null, null, 0.0);
            }
            
        } catch (Exception e) {
            log.error("AI 추천 배치 트리거 실패: batchType={}, error={}", batchType, e.getMessage());
            return new BatchTriggerResponse(false, "배치 트리거 호출 실패: " + e.getMessage(), 
                                          batchType, null, null, 0.0);
        }
    }
    
    /**
     * AI 추천 서비스에서 배치 상태 조회
     */
    public BatchStatusResponse getBatchStatus() {
        try {
            String url = String.format("%s/api/v1/batch/status", aiServiceUrl);
            
            log.info("AI 추천 배치 상태 조회 호출: {}", url);
            
            BatchStatusResponse response = restTemplate.getForObject(url, BatchStatusResponse.class);
            
            if (response != null) {
                log.info("AI 추천 배치 상태 조회 성공: 최근 배치 수={}", 
                        response.getRecentBatches() != null ? response.getRecentBatches().size() : 0);
                return response;
            } else {
                log.warn("AI 추천 배치 상태 조회 응답 없음");
                return new BatchStatusResponse("배치 상태 조회 실패", List.of());
            }
            
        } catch (Exception e) {
            log.error("AI 추천 배치 상태 조회 실패: error={}", e.getMessage());
            return new BatchStatusResponse("배치 상태 조회 실패: " + e.getMessage(), List.of());
        }
    }
    
    /**
     * AI 추천 서비스 응답 DTO
     */
    public static class AIRecommendationResponse {
        private Integer userId;
        private String itemType;
        private List<AIRecommendationItem> recommendations;
        
        public AIRecommendationResponse() {}
        
        public AIRecommendationResponse(Integer userId, String itemType, List<AIRecommendationItem> recommendations) {
            this.userId = userId;
            this.itemType = itemType;
            this.recommendations = recommendations;
        }
        
        // Getters and Setters
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        
        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
        
        public List<AIRecommendationItem> getRecommendations() { return recommendations; }
        public void setRecommendations(List<AIRecommendationItem> recommendations) { this.recommendations = recommendations; }
    }
    
    /**
     * AI 추천 아이템 DTO
     */
    public static class AIRecommendationItem {
        private Integer itemId;
        private Double score;
        
        public AIRecommendationItem() {}
        
        public AIRecommendationItem(Integer itemId, Double score) {
            this.itemId = itemId;
            this.score = score;
        }
        
        // Getters and Setters
        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
    
    /**
     * 배치 트리거 응답 DTO
     */
    public static class BatchTriggerResponse {
        private boolean success;
        private String message;
        private String batchType;
        private String startTime;
        private String endTime;
        private Double durationSeconds;
        
        public BatchTriggerResponse() {}
        
        public BatchTriggerResponse(boolean success, String message, String batchType, 
                                  String startTime, String endTime, Double durationSeconds) {
            this.success = success;
            this.message = message;
            this.batchType = batchType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationSeconds = durationSeconds;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getBatchType() { return batchType; }
        public void setBatchType(String batchType) { this.batchType = batchType; }
        
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        
        public Double getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(Double durationSeconds) { this.durationSeconds = durationSeconds; }
    }
    
    /**
     * 배치 상태 응답 DTO
     */
    public static class BatchStatusResponse {
        private String message;
        private List<BatchLogInfo> recentBatches;
        
        public BatchStatusResponse() {}
        
        public BatchStatusResponse(String message, List<BatchLogInfo> recentBatches) {
            this.message = message;
            this.recentBatches = recentBatches;
        }
        
        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<BatchLogInfo> getRecentBatches() { return recentBatches; }
        public void setRecentBatches(List<BatchLogInfo> recentBatches) { this.recentBatches = recentBatches; }
        
        /**
         * 배치 로그 정보 DTO
         */
        public static class BatchLogInfo {
            private Integer batchId;
            private String batchType;
            private Integer totalUsers;
            private Integer processedUsers;
            private Integer totalRecommendations;
            private String status;
            private String startTime;
            private String endTime;
            private String errorMessage;
            
            public BatchLogInfo() {}
            
            // Getters and Setters
            public Integer getBatchId() { return batchId; }
            public void setBatchId(Integer batchId) { this.batchId = batchId; }
            
            public String getBatchType() { return batchType; }
            public void setBatchType(String batchType) { this.batchType = batchType; }
            
            public Integer getTotalUsers() { return totalUsers; }
            public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
            
            public Integer getProcessedUsers() { return processedUsers; }
            public void setProcessedUsers(Integer processedUsers) { this.processedUsers = processedUsers; }
            
            public Integer getTotalRecommendations() { return totalRecommendations; }
            public void setTotalRecommendations(Integer totalRecommendations) { this.totalRecommendations = totalRecommendations; }
            
            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
            
            public String getStartTime() { return startTime; }
            public void setStartTime(String startTime) { this.startTime = startTime; }
            
            public String getEndTime() { return endTime; }
            public void setEndTime(String endTime) { this.endTime = endTime; }
            
            public String getErrorMessage() { return errorMessage; }
            public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        }
    }
} 