package com.travelonna.demo.domain.search.service;

import com.travelonna.demo.domain.search.dto.SearchResponseDto;

public interface SearchService {
    
    /**
     * 키워드로 장소와 사용자를 검색합니다.
     * 
     * @param keyword 검색 키워드
     * @return 검색 결과 DTO
     */
    SearchResponseDto search(String keyword);
    
    /**
     * 키워드로 장소와 사용자를 검색합니다. (페이지네이션 지원)
     * 
     * @param keyword 검색 키워드
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @param limit 각 카테고리별 최대 결과 수
     * @return 검색 결과 DTO
     */
    SearchResponseDto search(String keyword, Integer page, Integer size, Integer limit);
} 