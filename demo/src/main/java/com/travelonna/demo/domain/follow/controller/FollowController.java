package com.travelonna.demo.domain.follow.controller;

import com.travelonna.demo.domain.follow.dto.FollowRequestDto;
import com.travelonna.demo.domain.follow.dto.FollowResponseDto;
import com.travelonna.demo.domain.follow.service.FollowService;
import com.travelonna.demo.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관련 API")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "프로필 팔로우", description = "특정 프로필을 팔로우합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<FollowResponseDto>> followProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FollowRequestDto requestDto) {
        
        FollowResponseDto responseDto = followService.followProfile(
                Integer.parseInt(userDetails.getUsername()), 
                requestDto.getProfileId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("프로필 팔로우에 성공했습니다.", responseDto));
    }

    @Operation(summary = "프로필 언팔로우", description = "특정 프로필을 언팔로우합니다.")
    @DeleteMapping("/{profileId}")
    public ResponseEntity<ApiResponse<Void>> unfollowProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer profileId) {
        
        followService.unfollowProfile(
                Integer.parseInt(userDetails.getUsername()), 
                profileId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 언팔로우에 성공했습니다.", null));
    }

    @Operation(summary = "팔로우 상태 확인", description = "특정 프로필에 대한 팔로우 상태를 확인합니다.")
    @GetMapping("/status/{profileId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFollowStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer profileId) {
        
        boolean isFollowing = followService.isFollowing(
                Integer.parseInt(userDetails.getUsername()), 
                profileId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("팔로우 상태 조회에 성공했습니다.", Map.of("isFollowing", isFollowing)));
    }

    @Operation(summary = "프로필 팔로워 목록 조회", description = "특정 프로필의 팔로워 목록을 조회합니다.")
    @GetMapping("/followers/{profileId}")
    public ResponseEntity<ApiResponse<List<FollowResponseDto>>> getProfileFollowers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer profileId) {
        
        Integer userId = userDetails != null ? Integer.parseInt(userDetails.getUsername()) : null;
        List<FollowResponseDto> followers = followService.getProfileFollowers(profileId, userId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 팔로워 목록 조회에 성공했습니다.", followers));
    }

    @Operation(summary = "사용자 팔로잉 목록 조회", description = "특정 사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/followings/{userId}")
    public ResponseEntity<ApiResponse<List<FollowResponseDto>>> getUserFollowings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer userId) {
        
        Integer currentUserId = userDetails != null ? Integer.parseInt(userDetails.getUsername()) : null;
        List<FollowResponseDto> followings = followService.getUserFollowings(userId, currentUserId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("사용자 팔로잉 목록 조회에 성공했습니다.", followings));
    }

    @Operation(summary = "프로필 팔로워 수 조회", description = "특정 프로필의 팔로워 수를 조회합니다.")
    @GetMapping("/count/followers/{profileId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countProfileFollowers(
            @PathVariable Integer profileId) {
        
        long count = followService.countProfileFollowers(profileId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 팔로워 수 조회에 성공했습니다.", Map.of("count", count)));
    }

    @Operation(summary = "사용자 팔로잉 수 조회", description = "특정 사용자의 팔로잉 수를 조회합니다.")
    @GetMapping("/count/followings/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUserFollowings(
            @PathVariable Integer userId) {
        
        long count = followService.countUserFollowings(userId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("사용자 팔로잉 수 조회에 성공했습니다.", Map.of("count", count)));
    }
} 