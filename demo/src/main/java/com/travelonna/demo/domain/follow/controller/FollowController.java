package com.travelonna.demo.domain.follow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.follow.dto.FollowRequestDto;
import com.travelonna.demo.domain.follow.dto.FollowResponseDto;
import com.travelonna.demo.domain.follow.service.FollowService;
import com.travelonna.demo.domain.user.service.ProfileService;
import com.travelonna.demo.global.common.ApiResponse;
import com.travelonna.demo.global.security.jwt.JwtUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관련 API")
@SecurityRequirement(name = "bearerAuth") // Swagger UI에 인증 필요 표시
public class FollowController {

    private final FollowService followService;
    private final ProfileService profileService;

    // 현재 인증된 사용자의 ID를 가져오는 헬퍼 메서드
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserDetails) {
            return ((JwtUserDetails) principal).getUserId();
        }
        
        return null;
    }

    @Operation(summary = "프로필 팔로우", description = "특정 프로필을 팔로우합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "팔로우 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<FollowResponseDto>> followProfile(
            @Parameter(description = "팔로우 요청 정보", required = true, schema = @Schema(implementation = FollowRequestDto.class))
            @RequestBody FollowRequestDto requestDto) {
        
        Integer fromUser = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (fromUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        Integer toUser = requestDto.getToUser();
        
        log.info("팔로우 요청: 사용자 ID {}, 팔로우할 사용자 ID {}", fromUser, toUser);
        FollowResponseDto responseDto = followService.followProfile(fromUser, toUser);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("프로필 팔로우에 성공했습니다.", responseDto));
    }

    @Operation(summary = "프로필 언팔로우", description = "특정 프로필을 언팔로우합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "언팔로우 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/{toUser}")
    public ResponseEntity<ApiResponse<Void>> unfollowProfile(
            @Parameter(name = "toUser", description = "언팔로우할 사용자 ID", required = true, example = "10", in = ParameterIn.PATH)
            @PathVariable("toUser") Integer toUser) {
        
        Integer fromUser = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (fromUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        log.info("언팔로우 요청: 사용자 ID {}, 언팔로우할 사용자 ID {}", fromUser, toUser);
        followService.unfollowProfile(fromUser, toUser);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success("프로필 언팔로우에 성공했습니다.", null));
    }

    @Operation(summary = "팔로우 상태 확인", description = "특정 프로필에 대한 팔로우 상태를 확인합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로우 상태 확인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/status/{toUser}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFollowStatus(
            @Parameter(name = "toUser", description = "팔로우 상태를 확인할 사용자 ID", required = true, example = "10", in = ParameterIn.PATH)
            @PathVariable("toUser") Integer toUser) {
        
        Integer fromUser = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (fromUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        log.info("팔로우 상태 확인 요청: 사용자 ID {}, 대상 사용자 ID {}", fromUser, toUser);
        boolean isFollowing = followService.isFollowing(fromUser, toUser);
        
        Map<String, Boolean> result = Map.of("isFollowing", isFollowing);
        return ResponseEntity.ok(ApiResponse.success("팔로우 상태 확인에 성공했습니다.", result));
    }

    @Operation(summary = "프로필 팔로워 목록 조회", description = "특정 프로필의 팔로워 목록을 조회합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/followers/{profileId}")
    public ResponseEntity<ApiResponse<List<FollowResponseDto>>> getProfileFollowers(
            @Parameter(name = "profileId", description = "팔로워 목록을 조회할 프로필 ID", required = true, example = "6", in = ParameterIn.PATH)
            @PathVariable("profileId") Integer profileId) {
        
        Integer loggedInUserId = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        List<FollowResponseDto> followers = followService.getProfileFollowers(profileId, loggedInUserId);
        return ResponseEntity.ok(ApiResponse.success("팔로워 목록 조회에 성공했습니다.", followers));
    }

    @Operation(summary = "프로필 팔로잉 목록 조회", description = "특정 프로필의 팔로잉 목록을 조회합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/followings/{profileId}")
    public ResponseEntity<ApiResponse<List<FollowResponseDto>>> getProfileFollowings(
            @Parameter(name = "profileId", description = "팔로잉 목록을 조회할 프로필 ID", required = true, example = "6", in = ParameterIn.PATH)
            @PathVariable("profileId") Integer profileId) {
        
        Integer loggedInUserId = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        List<FollowResponseDto> followings = followService.getProfileFollowings(profileId, loggedInUserId);
        return ResponseEntity.ok(ApiResponse.success("팔로잉 목록 조회에 성공했습니다.", followings));
    }

    @Operation(summary = "프로필 팔로워 수 조회", description = "특정 프로필의 팔로워 수를 조회합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로워 수 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/count/followers/{profileId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countProfileFollowers(
            @Parameter(name = "profileId", description = "팔로워 수를 조회할 프로필 ID", required = true, example = "6", in = ParameterIn.PATH)
            @PathVariable("profileId") Integer profileId) {
        
        Integer loggedInUserId = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        long count = followService.countProfileFollowers(profileId);
        return ResponseEntity.ok(ApiResponse.success("팔로워 수 조회에 성공했습니다.", Map.of("count", count)));
    }

    @Operation(summary = "프로필 팔로잉 수 조회", description = "특정 프로필의 팔로잉 수를 조회합니다. 로그인이 필요한 기능입니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로잉 수 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/count/followings/{profileId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countProfileFollowings(
            @Parameter(name = "profileId", description = "팔로잉 수를 조회할 프로필 ID", required = true, example = "6", in = ParameterIn.PATH)
            @PathVariable("profileId") Integer profileId) {
        
        Integer loggedInUserId = getCurrentUserId();
        
        // 인증 정보가 없으면 401 Unauthorized 반환
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
        }
        
        long count = followService.countProfileFollowings(profileId);
        return ResponseEntity.ok(ApiResponse.success("팔로잉 수 조회에 성공했습니다.", Map.of("count", count)));
    }
} 