package com.travelonna.demo.domain.recommendation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
} 