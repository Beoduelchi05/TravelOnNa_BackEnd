package com.travelonna.demo.domain.search.service;

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
} 