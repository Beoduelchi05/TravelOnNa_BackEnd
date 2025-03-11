package com.travelonna.demo.domain.follow.dto;

import com.travelonna.demo.domain.follow.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowResponseDto {
    private Integer id;
    private Integer fromUser;
    private Integer toUser;
    private Integer profileId;
    private boolean isFollowing;
    
    public static FollowResponseDto fromEntity(Follow follow, boolean isFollowing) {
        return FollowResponseDto.builder()
                .id(follow.getId())
                .fromUser(follow.getFromUser())
                .toUser(follow.getToUser())
                .profileId(follow.getProfileId())
                .isFollowing(isFollowing)
                .build();
    }
} 