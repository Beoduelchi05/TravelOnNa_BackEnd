package com.travelonna.demo.domain.search.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.search.dto.SearchResponseDto;
import com.travelonna.demo.domain.search.service.SearchService;
import com.travelonna.demo.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "검색 API")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "장소 이름 또는 닉네임으로 검색", description = "장소 이름 또는 사용자 닉네임으로 검색합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponseDto>> search(
            @Parameter(description = "검색어 (장소 이름 또는 닉네임)", required = true)
            @RequestParam(required = false) String keyword) {
        
        log.info("검색 요청: keyword={}", keyword);
        
        // 검색어 유효성 검사
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("검색어가 입력되지 않았습니다.", new SearchResponseDto()));
        }
        
        // 검색어 길이 제한 (2자 이상 50자 이하)
        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() < 2) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("검색어는 2자 이상 입력해주세요."));
        }
        if (trimmedKeyword.length() > 50) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("검색어는 50자 이하로 입력해주세요."));
        }
        
        // 검색어 정규화 (연속된 공백을 하나로 통합)
        String normalizedKeyword = trimmedKeyword.replaceAll("\\s+", " ");
        
        log.info("검색어 정규화: '{}' -> '{}'", trimmedKeyword, normalizedKeyword);
        
        SearchResponseDto result = searchService.search(normalizedKeyword);
        return ResponseEntity.ok(ApiResponse.success("검색 결과입니다.", result));
    }
} 