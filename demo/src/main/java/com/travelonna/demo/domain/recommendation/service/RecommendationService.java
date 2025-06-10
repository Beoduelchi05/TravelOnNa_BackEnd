package com.travelonna.demo.domain.recommendation.service;

import java.util.List;
import java.util.Set;
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
            log.info("✅ 개인화 추천 사용: userId={}, 결과 수={}", userId, projections.size());
            
            List<RecommendationItemDto> recommendationItems = projections.stream()
                    .map(this::convertToRecommendationItemDto)
                    .collect(Collectors.toList());
            
            // **하이브리드 로직**: 배치 데이터가 요청한 limit보다 적으면 추가로 채우기
            int effectiveLimit = (limit != null && limit > 0) ? limit : 10;
            int remaining = effectiveLimit - recommendationItems.size();
            if (remaining > 0) {
                log.info("🔄 배치 데이터 부족 ({}/{}개) - {}개 추가로 채우기", 
                        recommendationItems.size(), effectiveLimit, remaining);
                
                // 이미 추천된 아이템 ID들을 제외 목록으로 만들기
                Set<Integer> excludeIds = recommendationItems.stream()
                        .map(RecommendationItemDto::getItemId)
                        .collect(Collectors.toSet());
                
                // AI 서비스로 추가 추천 시도
                try {
                    AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(
                            userId, type, remaining);
                    
                    if (aiResponse.getRecommendations() != null) {
                        final Set<Integer> finalExcludeIds = excludeIds;
                        List<RecommendationItemDto> additionalItems = aiResponse.getRecommendations().stream()
                                .filter(aiItem -> !finalExcludeIds.contains(aiItem.getItemId())) // 중복 제거
                                .map(aiItem -> convertAIItemToDto(aiItem, type))
                                .limit(remaining) // 필요한 개수만
                                .collect(Collectors.toList());
                        
                        recommendationItems.addAll(additionalItems);
                        remaining -= additionalItems.size();
                        
                        log.info("✅ AI 추천 추가: {}개 (남은 개수: {}개)", additionalItems.size(), remaining);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ AI 추천 추가 실패: {}", e.getMessage());
                }
                
                // 여전히 부족하면 콜드스타트로 채우기
                if (remaining > 0) {
                    log.info("🔄 여전히 부족 - 콜드스타트로 {}개 채우기", remaining);
                    
                    // 현재 추천된 아이템들 업데이트
                    Set<Integer> finalExcludeIds = recommendationItems.stream()
                            .map(RecommendationItemDto::getItemId)
                            .collect(Collectors.toSet());
                    
                    List<com.travelonna.demo.domain.log.dto.LogResponseDto> coldStartLogs = 
                        logService.getRandomPublicLogsWithPagination(userId, remaining, 0);
                    
                    List<RecommendationItemDto> coldStartItems = coldStartLogs.stream()
                            .filter(logDto -> !finalExcludeIds.contains(logDto.getLogId())) // 중복 제거
                            .map(logDto -> RecommendationItemDto.builder()
                                    .itemId(logDto.getLogId())
                                    .score(0.3f) // 콜드스타트는 낮은 점수
                                    .logId(logDto.getLogId())
                                    .userId(logDto.getUserId())
                                    .planId(logDto.getPlan() != null ? logDto.getPlan().getPlanId() : null)
                                    .comment(logDto.getComment())
                                    .createdAt(logDto.getCreatedAt())
                                    .isPublic(logDto.getIsPublic())
                                    .build())
                            .limit(remaining)
                            .collect(Collectors.toList());
                    
                    recommendationItems.addAll(coldStartItems);
                    
                    log.info("✅ 콜드스타트 추가: {}개", coldStartItems.size());
                }
                
                log.info("🎯 하이브리드 추천 완료: 총 {}개 (배치 {}개 + 추가 {}개)", 
                        recommendationItems.size(), projections.size(), 
                        recommendationItems.size() - projections.size());
            }
            
            return RecommendationResponseDto.builder()
                    .userId(userId)
                    .itemType(type.toLowerCase())
                    .recommendations(recommendationItems)
                    .build();
        }
        
        // 2. 배치 데이터가 없으면 AI 서비스 실시간 호출 시도
        log.info("배치 데이터 없음, AI 서비스 실시간 호출: userId={}", userId);
        
        int effectiveLimit = (limit != null && limit > 0) ? limit : 10;
        AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(userId, type, effectiveLimit);
        
        // AI 서비스 응답이 충분하면 사용
        if (aiResponse.getRecommendations() != null && !aiResponse.getRecommendations().isEmpty()) {
            log.info("✅ AI 실시간 추천 사용: userId={}, 결과 수={}", userId, aiResponse.getRecommendations().size());
            
            List<RecommendationItemDto> recommendationItems = aiResponse.getRecommendations().stream()
                    .map(aiItem -> convertAIItemToDto(aiItem, type))
                    .collect(Collectors.toList());
            
            return RecommendationResponseDto.builder()
                    .userId(userId)
                    .itemType(type.toLowerCase())
                    .recommendations(recommendationItems)
                    .build();
        }
        
        // 3. AI 서비스도 실패하면 콜드스타트 추천으로 자동 전환
        log.info("⚠️ AI 추천 실패 - 콜드스타트 추천으로 자동 전환: userId={}", userId);
        
        List<com.travelonna.demo.domain.log.dto.LogResponseDto> coldStartLogs = 
            logService.getRandomPublicLogsWithPagination(userId, effectiveLimit, 0);
        
        // 콜드스타트 로그를 RecommendationItemDto 형식으로 변환
        List<RecommendationItemDto> recommendationItems = coldStartLogs.stream()
                .map(logDto -> RecommendationItemDto.builder()
                        .itemId(logDto.getLogId())
                        .score(0.5f) // 콜드스타트는 기본 점수
                        .logId(logDto.getLogId())
                        .userId(logDto.getUserId())
                        .planId(logDto.getPlan() != null ? logDto.getPlan().getPlanId() : null)
                        .comment(logDto.getComment())
                        .createdAt(logDto.getCreatedAt())
                        .isPublic(logDto.getIsPublic())
                        .build())
                .collect(Collectors.toList());
        
        log.info("✅ 콜드스타트 추천 제공: userId={}, 결과 수={}", userId, recommendationItems.size());
        
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
     * 콜드스타트용 무작위 공개 기록 추천 (페이지네이션 방식)
     */
    public ColdStartRecommendationResponseDto getColdStartRecommendations(Integer userId, Integer limit, Integer offset) {
        log.info("콜드스타트 추천 요청: userId={}, limit={}, offset={}", 
                userId, limit, offset);
        
        // 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ID=" + userId));
        
        // LogService를 통해 무작위 공개 로그 조회 (페이지네이션)
        List<com.travelonna.demo.domain.log.dto.LogResponseDto> randomLogs = 
            logService.getRandomPublicLogsWithPagination(userId, limit, offset);
        
        log.info("콜드스타트 추천 완료: userId={}, 추천 수={}", userId, randomLogs.size());
        
        return ColdStartRecommendationResponseDto.builder()
                .userId(userId)
                .recommendationType("coldstart")
                .logs(randomLogs)
                .hasMore(randomLogs.size() == limit) // 더 많은 데이터가 있는지 여부
                .build();
    }
} 