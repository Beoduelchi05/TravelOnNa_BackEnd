package com.travelonna.demo.domain.group.dto;

import com.travelonna.demo.domain.group.entity.GroupEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponseDto {
    @Schema(description = "그룹 ID", example = "1")
    private Integer id;
    
    @Schema(description = "그룹 고유 URL", example = "a1b2c3d4")
    private String url;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
    
    @Schema(description = "생성 일시", example = "2024-03-21T14:30:00")
    private LocalDateTime createdDate;
    
    @Schema(description = "그룹 주최자 ID", example = "1")
    private Integer hostId;

    public static GroupResponseDto fromEntity(GroupEntity entity) {
        return GroupResponseDto.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .isPublic(entity.getIsPublic())
                .createdDate(entity.getCreatedDate())
                .hostId(entity.getHost().getUserId())
                .build();
    }
} 