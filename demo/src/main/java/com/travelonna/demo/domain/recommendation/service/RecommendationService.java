package com.travelonna.demo.domain.recommendation.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.log.repository.LogRepository;
import com.travelonna.demo.domain.log.service.LogService;
import com.travelonna.demo.domain.recommendation.dto.ColdStartRecommendationResponseDto;
import com.travelonna.demo.domain.recommendation.dto.RecommendationResponseDto;
import com.travelonna.demo.domain.recommendation.dto.RecommendationResponseDto.PageInfo;
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
    
    /**
     * 페이지네이션을 지원하는 추천 목록 조회 (신규 메소드)
     */
    public RecommendationResponseDto getRecommendationsPaginated(Integer userId, String type, Integer page, Integer size) {
        log.debug("페이지네이션 추천 목록 조회 시작: userId={}, type={}, page={}, size={}", userId, type, page, size);
        
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
        
        // Pageable 객체 생성 (0 기반이므로 page-1)
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 1. 먼저 배치 데이터 확인 (recommendations 테이블) - 페이지네이션
        Page<RecommendationProjection> pagedProjections = 
            recommendationRepository.findRecommendationsWithLogInfoPaginated(userId, itemType, pageable);
        
        // 배치 데이터가 있으면 사용 (페이지네이션 적용)
        if (pagedProjections.hasContent()) {
            log.info("✅ 개인화 추천 사용 (페이지네이션): userId={}, 페이지={}/{}, 결과 수={}", 
                    userId, page, pagedProjections.getTotalPages(), pagedProjections.getContent().size());
            
            List<RecommendationItemDto> recommendationItems = pagedProjections.getContent().stream()
                    .map(this::convertToRecommendationItemDto)
                    .collect(Collectors.toList());
            
            // 페이지 정보 생성
            PageInfo pageInfo = PageInfo.of(page, size, (int) pagedProjections.getTotalElements());
            
            return RecommendationResponseDto.builder()
                    .userId(userId)
                    .itemType(type.toLowerCase())
                    .recommendations(recommendationItems)
                    .pageInfo(pageInfo)
                    .build();
        }
        
        // 2. 배치 데이터가 없으면 AI 서비스 실시간 호출 시도 (전체 조회 후 페이지네이션 시뮬레이션)
        log.info("배치 데이터 없음, AI 서비스 실시간 호출: userId={}", userId);
        
        // AI 서비스에서 더 많은 데이터를 가져와서 페이지네이션 시뮬레이션
        int maxAIResults = Math.max(50, page * size); // 최소 50개 또는 현재 페이지까지 필요한 만큼
        AIRecommendationResponse aiResponse = aiRecommendationClient.getRecommendations(userId, type, maxAIResults);
        
        if (aiResponse.getRecommendations() != null && !aiResponse.getRecommendations().isEmpty()) {
            log.info("✅ AI 실시간 추천 사용 (페이지네이션 시뮬레이션): userId={}, 전체 수={}", 
                    userId, aiResponse.getRecommendations().size());
            
            List<RecommendationItemDto> allItems = aiResponse.getRecommendations().stream()
                    .map(aiItem -> convertAIItemToDto(aiItem, type))
                    .collect(Collectors.toList());
            
            // 수동 페이지네이션
            int totalElements = allItems.size();
            int offset = (page - 1) * size;
            int endIndex = Math.min(offset + size, totalElements);
            
            List<RecommendationItemDto> pagedItems = offset < totalElements 
                ? allItems.subList(offset, endIndex)
                : List.of();
            
            // 페이지 정보 생성
            PageInfo pageInfo = PageInfo.of(page, size, totalElements);
            
            return RecommendationResponseDto.builder()
                    .userId(userId)
                    .itemType(type.toLowerCase())
                    .recommendations(pagedItems)
                    .pageInfo(pageInfo)
                    .build();
        }
        
        // 3. AI 서비스도 실패하면 콜드스타트 추천으로 자동 전환 (페이지네이션)
        log.info("⚠️ AI 추천 실패 - 콜드스타트 추천으로 자동 전환: userId={}, page={}, size={}", userId, page, size);
        
        int offset = (page - 1) * size;
        List<com.travelonna.demo.domain.log.dto.LogResponseDto> coldStartLogs = 
            logService.getRandomPublicLogsWithPagination(userId, size, offset);
        
        // 전체 콜드스타트 추천 개수 추정 (실제로는 매우 많을 수 있음)
        int estimatedTotal = Math.max(100, page * size); // 최소 100개로 추정
        
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
        
        // 페이지 정보 생성 (다음 페이지가 있는지는 실제 조회 결과로 판단)
        boolean hasNext = coldStartLogs.size() == size; // 요청한 만큼 조회되면 다음 페이지가 있을 가능성
        int adjustedTotal = hasNext ? estimatedTotal : offset + coldStartLogs.size();
        
        PageInfo pageInfo = PageInfo.of(page, size, adjustedTotal);
        
        log.info("✅ 콜드스타트 추천 제공 (페이지네이션): userId={}, page={}, 결과 수={}", 
                userId, page, recommendationItems.size());
        
        return RecommendationResponseDto.builder()
                .userId(userId)
                .itemType(type.toLowerCase())
                .recommendations(recommendationItems)
                .pageInfo(pageInfo)
                .build();
    }
    
    /**
     * 기존 메소드 (하위 호환성을 위해 유지) - 내부적으로 새로운 페이지네이션 메소드 호출
     */
    public RecommendationResponseDto getRecommendations(Integer userId, String type, Integer limit) {
        log.debug("기존 추천 목록 조회 (호환성): userId={}, type={}, limit={}", userId, type, limit);
        
        // 기존 limit 방식을 첫 번째 페이지로 변환
        int size = limit != null && limit > 0 ? limit : 20;
        return getRecommendationsPaginated(userId, type, 1, size);
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