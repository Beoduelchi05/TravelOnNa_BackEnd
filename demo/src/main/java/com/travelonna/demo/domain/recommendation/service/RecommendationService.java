package com.travelonna.demo.domain.recommendation.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.recommendation.dto.RecommendationResponseDto;
import com.travelonna.demo.domain.recommendation.dto.RecommendationResponseDto.RecommendationItemDto;
import com.travelonna.demo.domain.recommendation.entity.Recommendation.ItemType;
import com.travelonna.demo.domain.recommendation.repository.RecommendationProjection;
import com.travelonna.demo.domain.recommendation.repository.RecommendationRepository;
import com.travelonna.demo.domain.recommendation.service.AIRecommendationClient.AIRecommendationItem;
import com.travelonna.demo.domain.recommendation.service.AIRecommendationClient.AIRecommendationResponse;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;
import com.travelonna.demo.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final AIRecommendationClient aiRecommendationClient;
    
    public RecommendationResponseDto getRecommendations(Integer userId, String type, Integer limit) {
        log.debug("추천 목록 조회 시작: userId={}, type={}, limit={}", userId, type, limit);
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ID=" + userId));
        
        // ItemType 변환
        ItemType itemType;
        try {
            itemType = ItemType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported item type: " + type);
        }
        
        // 현재는 LOG 타입만 지원
        if (itemType != ItemType.LOG) {
            throw new IllegalArgumentException("Currently only 'log' type is supported");
        }
        
        // 1. 먼저 배치 데이터 확인 (recommendations 테이블)
        List<RecommendationProjection> projections;
        if (limit != null && limit > 0) {
            projections = recommendationRepository.findRecommendationsWithLogInfoLimit(
                userId, itemType, limit);
        } else {
            projections = recommendationRepository.findRecommendationsWithLogInfo(
                userId, itemType);
        }
        
        // 배치 데이터가 있으면 사용 (빠른 경로)
        if (!projections.isEmpty()) {
            log.info("배치 데이터 사용: userId={}, 결과 수={}", userId, projections.size());
            
            List<RecommendationItemDto> recommendationItems = projections.stream()
                    .map(this::convertToRecommendationItemDto)
                    .collect(Collectors.toList());
            
            return RecommendationResponseDto.builder()
                    .userId(userId)
                    .itemType(type.toLowerCase())
                    .recommendations(recommendationItems)
                    .build();
        }
        
        // 2. 배치 데이터가 없으면 AI 서비스 실시간 호출
        log.info("배치 데이터 없음, AI 서비스 실시간 호출: userId={}", userId);
        
        int effectiveLimit = (limit != null && limit > 0) ? limit : 10;
        AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(userId, type, effectiveLimit);
        
        // AI 서비스 응답을 DTO로 변환
        List<RecommendationItemDto> recommendationItems = aiResponse.getRecommendations().stream()
                .map(aiItem -> convertAIItemToDto(aiItem, type))
                .collect(Collectors.toList());
        
        log.debug("AI 서비스 기반 추천 완료: userId={}, 결과 수={}", userId, recommendationItems.size());
        
        return RecommendationResponseDto.builder()
                .userId(userId)
                .itemType(type.toLowerCase())
                .recommendations(recommendationItems)
                .build();
    }
    
    private RecommendationItemDto convertToRecommendationItemDto(RecommendationProjection projection) {
        return RecommendationItemDto.builder()
                .itemId(projection.getItemId())
                .score(projection.getScore())
                .logId(projection.getLogId())
                .userId(projection.getLogUserId())
                .planId(projection.getPlanId())
                .comment(projection.getComment())
                .createdAt(projection.getCreatedAt())
                .isPublic(projection.getIsPublic())
                .build();
    }
    
    /**
     * AI 서비스 응답을 RecommendationItemDto로 변환
     */
    private RecommendationItemDto convertAIItemToDto(AIRecommendationItem aiItem, String type) {
        return RecommendationItemDto.builder()
                .itemId(aiItem.getItemId())
                .score(aiItem.getScore() != null ? aiItem.getScore().floatValue() : 0.0f)
                .logId(type.equals("log") ? aiItem.getItemId() : null)
                .userId(null)  // AI 서비스에서는 제공하지 않음
                .planId(null)
                .comment(null)
                .createdAt(null)
                .isPublic(null)
                .build();
    }
    
    public boolean hasRecommendations(Integer userId, String type) {
        try {
            ItemType itemType = ItemType.valueOf(type.toUpperCase());
            
            // 1. 배치 데이터 확인
            boolean hasBatchData = recommendationRepository.existsByUserUserIdAndItemType(userId, itemType);
            if (hasBatchData) {
                return true;
            }
            
            // 2. AI 서비스에서 실시간 확인
            try {
                AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(userId, type, 1);
                return aiResponse.getRecommendations() != null && !aiResponse.getRecommendations().isEmpty();
            } catch (Exception e) {
                log.warn("AI 서비스 호출 실패로 false 반환: userId={}, error={}", userId, e.getMessage());
                return false;
            }
            
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public long getRecommendationCount(Integer userId, String type) {
        try {
            ItemType itemType = ItemType.valueOf(type.toUpperCase());
            
            // 1. 배치 데이터 카운트
            long batchCount = recommendationRepository.countByUserUserIdAndItemType(userId, itemType);
            if (batchCount > 0) {
                return batchCount;
            }
            
            // 2. AI 서비스에서 실시간 확인 (최대 50개로 제한해서 카운트)
            try {
                AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(userId, type, 50);
                return aiResponse.getRecommendations() != null ? aiResponse.getRecommendations().size() : 0;
            } catch (Exception e) {
                log.warn("AI 서비스 호출 실패로 0 반환: userId={}, error={}", userId, e.getMessage());
                return 0;
            }
            
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
} 