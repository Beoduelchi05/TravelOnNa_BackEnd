package com.travelonna.demo.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.search.dto.SearchResponseDto;
import com.travelonna.demo.domain.search.repository.SearchRepository;
import com.travelonna.demo.domain.user.entity.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;

    /**
     * 검색어를 정규화하는 메서드
     * - 앞뒤 공백 제거
     * - 연속된 공백을 하나로 통합
     * - 특수문자 정리
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) return "";
        
        return keyword.trim()
                     .replaceAll("\\s+", " ")  // 연속된 공백을 하나로
                     .replaceAll("[\\-_]+", ""); // 하이픈, 언더스코어 제거
    }
    
    /**
     * 검색어를 단어별로 분리하는 메서드
     */
    private List<String> splitKeywords(String keyword) {
        List<String> keywords = new ArrayList<>();
        
        // 원본 키워드 추가
        keywords.add(keyword);
        
        // 공백으로 분리된 단어들 추가
        String[] words = keyword.split("\\s+");
        if (words.length > 1) {
            for (String word : words) {
                if (word.length() >= 2) { // 2자 이상인 단어만
                    keywords.add(word);
                }
            }
        }
        
        return keywords;
    }

    @Override
    public SearchResponseDto search(String keyword) {
        System.out.println("검색 시작: keyword=" + keyword);
        
        // 키워드로 장소 검색
        List<Place> places = searchRepository.searchPlacesByKeyword(keyword);
        System.out.println("장소 검색 결과: " + places.size() + " 건");
        
        // 키워드로 사용자 검색 (닉네임)
        List<Profile> profiles = searchRepository.searchProfilesByKeyword(keyword);
        System.out.println("사용자 검색 결과: " + profiles.size() + " 건");
        
        // 키워드로 여행 기록 검색
        List<Log> logs = searchRepository.searchLogsByKeyword(keyword);
        System.out.println("여행 기록 검색 결과: " + logs.size() + " 건");
        
        // 결과 DTO 구성
        SearchResponseDto response = new SearchResponseDto();
        
        // 직접 필드에 값 설정
        try {
            java.lang.reflect.Field placesField = response.getClass().getDeclaredField("places");
            placesField.setAccessible(true);
            placesField.set(response, places.stream()
                    .map(SearchResponseDto.PlaceDto::fromEntity)
                    .collect(Collectors.toList()));
                    
            java.lang.reflect.Field usersField = response.getClass().getDeclaredField("users");
            usersField.setAccessible(true);
            usersField.set(response, profiles.stream()
                    .map(SearchResponseDto.UserDto::fromEntity)
                    .collect(Collectors.toList()));
                    
            java.lang.reflect.Field logsField = response.getClass().getDeclaredField("logs");
            logsField.setAccessible(true);
            logsField.set(response, logs.stream()
                    .map(SearchResponseDto.LogDto::fromEntity)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            System.out.println("DTO 설정 중 오류: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public SearchResponseDto search(String keyword, Integer page, Integer size, Integer limit) {
        log.info("페이지네이션 검색 시작: keyword={}, page={}, size={}, limit={}", keyword, page, size, limit);
        
        long startTime = System.currentTimeMillis();
        
        // 각 카테고리별로 제한된 수만큼 검색 (성능 최적화)
        // 페이지네이션은 전체 결과에 대해 적용하지 않고, 각 카테고리별로 limit만큼만 가져옴
        
        // 키워드로 장소 검색 (제한된 수)
        List<Place> places = searchRepository.searchPlacesByKeyword(keyword, 0, limit);
        log.info("장소 검색 결과: {} 건", places.size());
        
        // 키워드로 사용자 검색 (제한된 수)
        List<Profile> profiles = searchRepository.searchProfilesByKeyword(keyword, 0, limit);
        log.info("사용자 검색 결과: {} 건", profiles.size());
        
        // 키워드로 여행 기록 검색 (제한된 수)
        List<Log> logs = searchRepository.searchLogsByKeyword(keyword, 0, limit);
        log.info("여행 기록 검색 결과: {} 건", logs.size());
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // 결과 DTO 구성
        SearchResponseDto response = new SearchResponseDto();
        
        // 메타 정보 설정
        SearchResponseDto.SearchMetaDto meta = SearchResponseDto.SearchMetaDto.builder()
                .keyword(keyword)
                .normalizedKeyword(keyword.replaceAll("\\s+", " "))
                .totalPlaces(places.size())
                .totalUsers(profiles.size())
                .totalLogs(logs.size())
                .limitPerCategory(limit)
                .executionTimeMs(executionTime)
                .enhancedSearchApplied(true) // 개선된 검색 로직 적용됨
                .build();
        response.setMeta(meta);
        
        // 직접 필드에 값 설정
        try {
            java.lang.reflect.Field placesField = response.getClass().getDeclaredField("places");
            placesField.setAccessible(true);
            placesField.set(response, places.stream()
                    .map(SearchResponseDto.PlaceDto::fromEntity)
                    .collect(Collectors.toList()));
                    
            java.lang.reflect.Field usersField = response.getClass().getDeclaredField("users");
            usersField.setAccessible(true);
            usersField.set(response, profiles.stream()
                    .map(SearchResponseDto.UserDto::fromEntity)
                    .collect(Collectors.toList()));
                    
            java.lang.reflect.Field logsField = response.getClass().getDeclaredField("logs");
            logsField.setAccessible(true);
            logsField.set(response, logs.stream()
                    .map(SearchResponseDto.LogDto::fromEntity)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("DTO 설정 중 오류: {}", e.getMessage());
        }
        
        log.info("페이지네이션 검색 완료: 장소 {}, 사용자 {}, 로그 {} (실행시간: {}ms)", 
                places.size(), profiles.size(), logs.size(), executionTime);
        
        return response;
    }
} 