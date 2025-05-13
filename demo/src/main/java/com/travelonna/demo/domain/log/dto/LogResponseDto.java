package com.travelonna.demo.domain.log.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.plan.dto.PlanSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogResponseDto {
    
    private Integer logId;
    private Integer userId;
    private String userName;
    private String userProfileImage;
    private String comment;
    private LocalDateTime createdAt;
    private Boolean isPublic;
    private List<String> imageUrls;
    private int likeCount;
    private int commentCount;
    private Boolean isLiked;
    private PlanSummaryDto plan;
    private List<String> placeNames;
    
    public static LogResponseDto fromEntity(Log log, boolean isLiked) {
        return LogResponseDto.builder()
                .logId(log.getLogId())
                .userId(log.getUser().getUserId())
                .userName(log.getUser().getName())
                .comment(log.getComment())
                .createdAt(log.getCreatedAt())
                .isPublic(log.getIsPublic())
                .likeCount(log.getLikes().size())
                .commentCount(log.getComments().size())
                .isLiked(isLiked)
                .plan(PlanSummaryDto.fromEntity(log.getPlan()))
                .build();
    }
} 