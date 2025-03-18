package com.travelonna.demo.domain.group.dto;

import com.travelonna.demo.domain.group.entity.GroupEntity;
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
    private Integer id;
    private String url;
    private Boolean isPublic;
    private LocalDateTime createdDate;
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