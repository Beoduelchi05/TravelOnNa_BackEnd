package com.travelonna.demo.domain.follow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.follow.dto.FollowRequestDto;
import com.travelonna.demo.domain.follow.dto.FollowResponseDto;
import com.travelonna.demo.domain.follow.service.FollowService;
import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.service.ProfileService;
import com.travelonna.demo.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관련 API")
public class FollowController {

    private final FollowService followService;
    private final ProfileService profileService;

    @Operation(summary = "프로필 팔로우", description = "특정 프로필을 팔로우합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<FollowResponseDto>> followProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FollowRequestDto requestDto) {
        
        // 인증 정보가 있으면 사용하고, 없으면 요청 본문의 fromUser 사용
        Integer fromUser = userDetails != null ? 
                Integer.parseInt(userDetails.getUsername()) : 
                (requestDto.getFromUser() != null ? requestDto.getFromUser() : 1);
        
        Integer toUser = requestDto.getToUser();
        
        log.info("팔로우 요청: 사용자 ID {}, 팔로우할 사용자 ID {}", fromUser, toUser);
        FollowResponseDto responseDto = followService.followProfile(fromUser, toUser);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("프로필 팔로우에 성공했습니다.", responseDto));
    }

    @Operation(summary = "프로필 언팔로우", description = "특정 프로필을 언팔로우합니다.")
    @DeleteMapping("/{toUser}")
    public ResponseEntity<ApiResponse<Void>> unfollowProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer fromUser,
            @PathVariable Integer toUser) {
        
        // 인증 정보가 있으면 사용하고, 없으면 요청 파라미터의 fromUser 사용
        Integer userFromUser = userDetails != null ? 
                Integer.parseInt(userDetails.getUsername()) : 
                (fromUser != null ? fromUser : 1);
        
        log.info("언팔로우 요청: 사용자 ID {}, 언팔로우할 사용자 ID {}", userFromUser, toUser);
        followService.unfollowProfile(userFromUser, toUser);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 언팔로우에 성공했습니다.", null));
    }

    @Operation(summary = "팔로우 상태 확인", description = "특정 프로필에 대한 팔로우 상태를 확인합니다.")
    @GetMapping("/status/{toUser}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFollowStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer fromUser,
            @PathVariable Integer toUser) {
        
        // 인증 정보가 있으면 사용하고, 없으면 요청 파라미터의 fromUser 사용
        Integer userFromUser = userDetails != null ? 
                Integer.parseInt(userDetails.getUsername()) : 
                (fromUser != null ? fromUser : 1);
        
        log.info("팔로우 상태 확인 요청: 사용자 ID {}, 대상 사용자 ID {}", userFromUser, toUser);
        boolean isFollowing = followService.isFollowing(userFromUser, toUser);
        
        Map<String, Boolean> result = Map.of("isFollowing", isFollowing);
        return ResponseEntity.ok(ApiResponse.success("팔로우 상태 확인에 성공했습니다.", result));
    }

    @Operation(summary = "프로필 팔로워 목록 조회", description = "특정 프로필의 팔로워 목록을 조회합니다.")
    @GetMapping("/followers/{profileId}")
    public ResponseEntity<ApiResponse<List<FollowResponseDto>>> getProfileFollowers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer currentUserId,
            @PathVariable Integer profileId) {
        
        // 인증 정보가 있으면 사용하고, 없으면 요청 파라미터의 currentUserId 사용
        Integer loggedInUserId = userDetails != null ? 
                Integer.parseInt(userDetails.getUsername()) : 
                currentUserId;
        
        List<FollowResponseDto> followers = followService.getProfileFollowers(profileId, loggedInUserId);
        return ResponseEntity.ok(ApiResponse.success("팔로워 목록 조회에 성공했습니다.", followers));
    }

    @Operation(summary = "프로필 팔로잉 목록 조회", description = "특정 프로필의 팔로잉 목록을 조회합니다.")
    @GetMapping("/followings/{profileId}")
    public ResponseEntity<ApiResponse<List<FollowResponseDto>>> getProfileFollowings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer currentUserId,
            @PathVariable Integer profileId) {
        
        // 인증 정보가 있으면 사용하고, 없으면 요청 파라미터의 currentUserId 사용
        Integer loggedInUserId = userDetails != null ? 
                Integer.parseInt(userDetails.getUsername()) : 
                currentUserId;
        
        List<FollowResponseDto> followings = followService.getProfileFollowings(profileId, loggedInUserId);
        return ResponseEntity.ok(ApiResponse.success("팔로잉 목록 조회에 성공했습니다.", followings));
    }

    @Operation(summary = "프로필 팔로워 수 조회", description = "특정 프로필의 팔로워 수를 조회합니다.")
    @GetMapping("/count/followers/{profileId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countProfileFollowers(
            @PathVariable Integer profileId) {
        
        long count = followService.countProfileFollowers(profileId);
        return ResponseEntity.ok(ApiResponse.success("팔로워 수 조회에 성공했습니다.", Map.of("count", count)));
    }

    @Operation(summary = "프로필 팔로잉 수 조회", description = "특정 프로필의 팔로잉 수를 조회합니다.")
    @GetMapping("/count/followings/{profileId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countProfileFollowings(
            @PathVariable Integer profileId) {
        
        long count = followService.countProfileFollowings(profileId);
        return ResponseEntity.ok(ApiResponse.success("팔로잉 수 조회에 성공했습니다.", Map.of("count", count)));
    }
} 