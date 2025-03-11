package com.travelonna.demo.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowRequestDto {
    @Schema(description = "팔로우할 대상의 사용자 ID", example = "2")
    private Integer toUser; // 팔로우할 대상의 사용자 ID
    
    @Schema(description = "팔로우를 요청하는 사용자 ID", example = "1")
    private Integer fromUser; // 팔로우를 요청하는 사용자 ID
}