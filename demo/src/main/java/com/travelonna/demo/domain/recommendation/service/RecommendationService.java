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
        
        // 추천 데이터 조회
        List<RecommendationProjection> projections;
        if (limit != null && limit > 0) {
            projections = recommendationRepository.findRecommendationsWithLogInfoLimit(
                userId, itemType, limit);
        } else {
            projections = recommendationRepository.findRecommendationsWithLogInfo(
                userId, itemType);
        }
        
        // DTO 변환
        List<RecommendationItemDto> recommendationItems = projections.stream()
                .map(this::convertToRecommendationItemDto)
                .collect(Collectors.toList());
        
        log.debug("추천 목록 조회 완료: userId={}, 결과 수={}", userId, recommendationItems.size());
        
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
    
    public boolean hasRecommendations(Integer userId, String type) {
        try {
            ItemType itemType = ItemType.valueOf(type.toUpperCase());
            return recommendationRepository.existsByUserUserIdAndItemType(userId, itemType);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public long getRecommendationCount(Integer userId, String type) {
        try {
            ItemType itemType = ItemType.valueOf(type.toUpperCase());
            return recommendationRepository.countByUserUserIdAndItemType(userId, itemType);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
} 