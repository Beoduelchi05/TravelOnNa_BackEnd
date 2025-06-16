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
} 