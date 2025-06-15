package com.travelonna.demo.domain.recommendation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecommendationResponseDto {
    
    private Integer userId;
    private String itemType;
    private List<RecommendationItemDto> recommendations;
    
    // 페이지네이션 정보 추가
    private PageInfo pageInfo;
    
    @Getter
    @Setter
    @Builder
    public static class RecommendationItemDto {
        private Integer itemId;
        private Float score;
        
        // Log 관련 정보
        private Integer logId;
        private Integer userId;
        private Integer planId;
        private String comment;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        private Boolean isPublic;
    }
    
    @Getter
    @Setter
    @Builder
    public static class PageInfo {
        private Integer currentPage;      // 현재 페이지 (1부터 시작)
        private Integer pageSize;         // 페이지 크기
        private Integer totalElements;    // 전체 요소 수
        private Integer totalPages;       // 전체 페이지 수
        private Boolean hasNext;          // 다음 페이지 존재 여부
        private Boolean hasPrevious;      // 이전 페이지 존재 여부
        private Boolean isFirst;          // 첫 번째 페이지 여부
        private Boolean isLast;           // 마지막 페이지 여부
        
        public static PageInfo of(int currentPage, int pageSize, int totalElements) {
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            
            return PageInfo.builder()
                    .currentPage(currentPage)
                    .pageSize(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .hasNext(currentPage < totalPages)
                    .hasPrevious(currentPage > 1)
                    .isFirst(currentPage == 1)
                    .isLast(currentPage >= totalPages || totalPages == 0)
                    .build();
        }
    }
} 