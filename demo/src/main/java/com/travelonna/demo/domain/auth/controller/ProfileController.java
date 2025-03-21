package com.travelonna.demo.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.auth.dto.ProfileRequest;
import com.travelonna.demo.domain.auth.dto.ProfileResponse;
import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 프로필 관리를 위한 REST API 컨트롤러
 * 프로필 생성, 조회, 수정 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관리 API")
public class ProfileController {

    private final ProfileService profileService;
    
    /**
     * 새로운 사용자 프로필을 생성합니다.
     * 
     * @param request 프로필 생성에 필요한 정보(사용자 ID, 닉네임, 프로필 이미지, 소개)를 포함한 요청 객체
     * @return 생성된 프로필 정보와 상태 코드
     * @throws IllegalArgumentException 유효하지 않은 요청 데이터가 제공된 경우
     */
    @Operation(summary = "프로필 생성", description = "새로운 사용자 프로필을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 생성 성공", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @Parameter(description = "프로필 생성 정보", required = true, schema = @Schema(implementation = ProfileRequest.class)) 
            @RequestBody ProfileRequest request) {
        try {
            Profile profile = profileService.createProfile(
                request.getUserId(),
                request.getNickname(),
                request.getProfileImage(),
                request.getIntroduction()
            );
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        }
    }
    
    /**
     * 사용자 ID로 프로필을 조회합니다.
     * 
     * @param userId 조회할 사용자의 ID
     * @return 조회된 프로필 정보와 상태 코드
     * @throws IllegalArgumentException 해당 사용자 ID의 프로필이 존재하지 않는 경우
     */
    @Operation(summary = "사용자 ID로 프로필 조회", description = "사용자 ID에 해당하는 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "프로필이 존재하지 않음", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(
            @Parameter(name = "userId", description = "조회할 사용자 ID", required = true, example = "6", in = ParameterIn.PATH) 
            @PathVariable("userId") Integer userId) {
        try {
            Profile profile = profileService.getProfileByUserId(userId);
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        }
    }
    
    /**
     * 프로필 ID로 프로필을 조회합니다.
     * 
     * @param profileId 조회할 프로필의 ID
     * @return 조회된 프로필 정보와 상태 코드
     * @throws IllegalArgumentException 해당 프로필 ID의 프로필이 존재하지 않는 경우
     */
    @Operation(summary = "프로필 ID로 프로필 조회", description = "프로필 ID에 해당하는 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "프로필이 존재하지 않음", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    })
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getProfile(
            @Parameter(name = "profileId", description = "조회할 프로필 ID", required = true, example = "6", in = ParameterIn.PATH) 
            @PathVariable("profileId") Integer profileId) {
        try {
            Profile profile = profileService.getProfileById(profileId);
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        }
    }
    
    /**
     * 기존 프로필 정보를 수정합니다.
     * 
     * @param profileId 수정할 프로필의 ID
     * @param request 수정할 프로필 정보(닉네임, 프로필 이미지, 소개)를 포함한 요청 객체
     * @return 수정된 프로필 정보와 상태 코드
     * @throws IllegalArgumentException 해당 프로필 ID의 프로필이 존재하지 않거나 유효하지 않은 요청 데이터가 제공된 경우
     */
    @Operation(summary = "프로필 정보 수정", description = "기존 프로필 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 수정 성공", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "프로필이 존재하지 않거나 잘못된 요청 데이터", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    })
    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @Parameter(name = "profileId", description = "수정할 프로필 ID", required = true, example = "6", in = ParameterIn.PATH) 
            @PathVariable("profileId") Integer profileId,
            @Parameter(description = "수정할 프로필 정보", required = true, schema = @Schema(implementation = ProfileRequest.class)) 
            @RequestBody ProfileRequest request) {
        try {
            Profile profile = profileService.updateProfile(
                profileId,
                request.getNickname(),
                request.getProfileImage(),
                request.getIntroduction()
            );
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        }
    }
    
    /**
     * Profile 엔티티를 ProfileResponse DTO로 변환합니다.
     * 
     * @param profile 변환할 Profile 엔티티
     * @return 변환된 ProfileResponse 객체
     */
    private ProfileResponse convertToResponse(Profile profile) {
        // 엔티티를 DTO로 변환
        return new ProfileResponse(
            profile.getProfileId(),
            profile.getUserId(),
            profile.getNickname(),
            profile.getProfileImage(),
            profile.getIntroduction(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}