package com.travelonna.demo.domain.recommendation.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.log.repository.LogRepository;
import com.travelonna.demo.domain.log.service.LogService;
import com.travelonna.demo.domain.recommendation.dto.ColdStartRecommendationResponseDto;
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
    private final LogRepository logRepository;
    private final LogService logService;
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
     * AI 서비스 응답을 RecommendationItemDto로 변환 (실제 로그 정보 보강)
     */
    private RecommendationItemDto convertAIItemToDto(AIRecommendationItem aiItem, String type) {
        if (!type.equals("log")) {
            // log 타입이 아닌 경우 기본 정보만 반환
            return RecommendationItemDto.builder()
                    .itemId(aiItem.getItemId())
                    .score(aiItem.getScore() != null ? aiItem.getScore().floatValue() : 0.0f)
                    .build();
        }
        
        // log 타입인 경우 실제 로그 정보 조회
        Integer logId = aiItem.getItemId();
        try {
            Log logEntity = logRepository.findByIdWithDetails(logId).orElse(null);
            
            if (logEntity != null && logEntity.getIsPublic()) {
                // 공개 로그인 경우 상세 정보 포함
                return RecommendationItemDto.builder()
                        .itemId(logId)
                        .score(aiItem.getScore() != null ? aiItem.getScore().floatValue() : 0.0f)
                        .logId(logId)
                        .userId(logEntity.getUser().getUserId())
                        .planId(logEntity.getPlan() != null ? logEntity.getPlan().getPlanId() : null)
                        .comment(logEntity.getComment())
                        .createdAt(logEntity.getCreatedAt())
                        .isPublic(logEntity.getIsPublic())
                        .build();
            } else {
                // 비공개 로그이거나 로그가 없는 경우 기본 정보만 반환
                log.warn("추천된 로그가 비공개이거나 존재하지 않음: logId={}", logId);
                return RecommendationItemDto.builder()
                        .itemId(logId)
                        .score(aiItem.getScore() != null ? aiItem.getScore().floatValue() : 0.0f)
                        .logId(logId)
                        .userId(null)
                        .planId(null)
                        .comment(null)
                        .createdAt(null)
                        .isPublic(null)
                        .build();
            }
        } catch (Exception e) {
            log.error("로그 정보 조회 실패: logId={}, error={}", logId, e.getMessage());
            // 에러 발생 시 기본 정보만 반환
            return RecommendationItemDto.builder()
                    .itemId(logId)
                    .score(aiItem.getScore() != null ? aiItem.getScore().floatValue() : 0.0f)
                    .logId(logId)
                    .userId(null)
                    .planId(null)
                    .comment(null)
                    .createdAt(null)
                    .isPublic(null)
                    .build();
        }
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
    
    /**
     * 콜드스타트용 무작위 공개 기록 추천
     */
    public ColdStartRecommendationResponseDto getColdStartRecommendations(Integer userId, Integer limit, List<Integer> excludeLogIds) {
        log.info("콜드스타트 추천 요청: userId={}, limit={}, 제외 로그 수={}", 
                userId, limit, excludeLogIds != null ? excludeLogIds.size() : 0);
        
        // 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ID=" + userId));
        
        // LogService를 통해 무작위 공개 로그 조회
        List<com.travelonna.demo.domain.log.dto.LogResponseDto> randomLogs = 
            logService.getRandomPublicLogs(userId, limit, excludeLogIds);
        
        // 로그 ID 목록 추출 (다음 요청 시 중복 방지용)
        List<Integer> logIds = randomLogs.stream()
                .map(com.travelonna.demo.domain.log.dto.LogResponseDto::getLogId)
                .collect(Collectors.toList());
        
        log.info("콜드스타트 추천 완료: userId={}, 추천 수={}", userId, randomLogs.size());
        
        return ColdStartRecommendationResponseDto.builder()
                .userId(userId)
                .recommendationType("coldstart")
                .logs(randomLogs)
                .logIds(logIds)
                .build();
    }
} 