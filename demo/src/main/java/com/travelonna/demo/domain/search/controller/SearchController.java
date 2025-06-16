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

    @Operation(summary = "장소 이름 또는 닉네임으로 검색", description = "장소 이름 또는 사용자 닉네임으로 검색합니다. 성능 최적화를 위해 페이지네이션과 결과 개수 제한이 적용됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponseDto>> search(
            @Parameter(description = "검색어 (장소 이름 또는 닉네임)", required = true)
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "페이지 크기 (최대 50)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "각 카테고리별 최대 결과 수 (최대 100)", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        
        log.info("검색 요청: keyword={}, page={}, size={}, limit={}", keyword, page, size, limit);
        
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
        
        // 특수문자만으로 구성된 검색어 방지
        if (normalizedKeyword.matches("^[^a-zA-Z0-9가-힣\\s]+$")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유효한 검색어를 입력해주세요."));
        }
        
        log.info("검색어 정규화: '{}' -> '{}'", trimmedKeyword, normalizedKeyword);
        
        // 페이지 파라미터 유효성 검사
        if (page < 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("페이지 번호는 1 이상이어야 합니다."));
        }
        if (size < 1 || size > 50) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("페이지 크기는 1~50 사이여야 합니다."));
        }
        if (limit < 1 || limit > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("카테고리별 결과 제한은 1~100 사이여야 합니다."));
        }
        
        // 성능 최적화: limit이 너무 클 경우 경고 로그
        if (limit > 50) {
            log.warn("높은 limit 값으로 검색 요청: keyword={}, limit={}", normalizedKeyword, limit);
        }
        
        SearchResponseDto result = searchService.search(normalizedKeyword, page, size, limit);
        return ResponseEntity.ok(ApiResponse.success("검색 결과입니다.", result));
    }
} 