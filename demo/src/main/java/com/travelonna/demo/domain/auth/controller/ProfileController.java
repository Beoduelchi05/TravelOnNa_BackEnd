package com.travelonna.demo.domain.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 프로필 관리를 위한 REST API 컨트롤러
 * 프로필 생성, 조회, 수정 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관리 API")
public class ProfileController {

    private final ProfileService profileService;
    
    /**
     * 새로운 사용자 프로필을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param nickname 사용자 닉네임
     * @param profileImage 프로필 이미지 파일 (권장: 320x320px, 최소: 110x110px)
     * @param profileImageUrl 프로필 이미지 URL (파일 대신 URL 제공 가능)
     * @param introduction 소개글
     * @return 생성된 프로필 정보와 상태 코드
     */
    @Operation(summary = "프로필 생성", 
               description = "새로운 사용자 프로필을 생성합니다. " +
                             "프로필 이미지는 파일 업로드 또는 URL 문자열로 제공할 수 있습니다. " +
                             "파일 업로드 시 권장 크기는 320x320px (1:1 비율), 최소 크기 110x110px이며, " +
                             "원형으로 표시되므로 중요한 내용은 중앙에 배치해주세요.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 생성 성공", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> createProfile(
            @Parameter(description = "사용자 ID (회원 테이블의 ID, 인증된 사용자의 고유 식별자)", required = true, example = "1")
            @RequestParam Integer userId,
            
            @Parameter(description = "사용자 닉네임 (프로필에 표시될 이름, 중복 불가)", required = true, example = "여행왕")
            @RequestParam String nickname,
            
            @Parameter(description = "프로필 이미지 파일 (권장: 320x320px, 최소: 110x110px, 최대 5MB)", required = false)
            @RequestPart(required = false) MultipartFile profileImage,
            
            @Parameter(description = "프로필 이미지 URL (파일 대신 URL로 이미지 제공, 파일과 URL 모두 제공 시 파일이 우선함)", required = false, 
                      example = "https://example.com/images/profile.jpg")
            @RequestParam(required = false) String profileImageUrl,
            
            @Parameter(description = "자기소개 (프로필 소개글, 최대 255자)", required = false, example = "여행을 좋아하는 직장인입니다.")
            @RequestParam(required = false) String introduction) {
        try {
            log.info("Creating profile for user: {}, nickname: {}", userId, nickname);
            
            Profile profile;
            if (profileImage != null && !profileImage.isEmpty()) {
                // 파일 업로드가 제공된 경우
                profile = profileService.createProfileWithImage(userId, nickname, profileImage, introduction);
            } else {
                // URL 또는 null이 제공된 경우
                profile = profileService.createProfile(userId, nickname, profileImageUrl, introduction);
            }
            
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            log.error("Failed to create profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while creating profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ProfileResponse(null, "프로필 생성 중 오류가 발생했습니다: " + e.getMessage()));
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
            @Parameter(name = "userId", description = "조회할 사용자 ID (회원 테이블의 ID)", required = true, example = "6", in = ParameterIn.PATH) 
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
            @Parameter(name = "profileId", description = "조회할 프로필 ID (프로필 테이블의 ID)", required = true, example = "6", in = ParameterIn.PATH) 
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
     * @param nickname 수정할 닉네임
     * @param profileImage 수정할 프로필 이미지 파일 (권장: 320x320px, 최소: 110x110px)
     * @param profileImageUrl 수정할 프로필 이미지 URL (파일 대신 URL 제공 가능)
     * @param introduction 수정할 소개글
     * @return 수정된 프로필 정보와 상태 코드
     */
    @Operation(summary = "프로필 정보 수정", 
               description = "기존 프로필 정보를 수정합니다. " +
                             "프로필 이미지는 파일 업로드 또는 URL 문자열로 제공할 수 있습니다. " +
                             "파일 업로드 시 권장 크기는 320x320px (1:1 비율), 최소 크기 110x110px이며, " +
                             "원형으로 표시되므로 중요한 내용은 중앙에 배치해주세요." +
                             "변경하지 않을 필드는 제공하지 않아도 됩니다(null로 유지).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 수정 성공", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "프로필이 존재하지 않거나 잘못된 요청 데이터", 
                     content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    })
    @PutMapping(value = "/{profileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> updateProfile(
            @Parameter(name = "profileId", description = "수정할 프로필 ID (프로필 테이블의 ID)", required = true, example = "6", in = ParameterIn.PATH)
            @PathVariable("profileId") Integer profileId,
            
            @Parameter(description = "수정할 닉네임 (중복 불가, 변경하지 않을 경우 제공하지 않음)", required = false, example = "여행왕")
            @RequestParam(required = false) String nickname,
            
            @Parameter(description = "수정할 프로필 이미지 파일 (권장: 320x320px, 최소: 110x110px, 최대 5MB)", required = false)
            @RequestPart(required = false) MultipartFile profileImage,
            
            @Parameter(description = "수정할 프로필 이미지 URL (파일 대신 URL로 이미지 제공, 파일과 URL 모두 제공 시 파일이 우선함)", required = false,
                      example = "https://example.com/images/profile.jpg")
            @RequestParam(required = false) String profileImageUrl,
            
            @Parameter(description = "수정할 자기소개 (프로필 소개글, 최대 255자)", required = false, example = "여행을 좋아하는 직장인입니다.")
            @RequestParam(required = false) String introduction) {
        try {
            log.info("Updating profile for profileId: {}", profileId);
            
            Profile profile;
            if (profileImage != null && !profileImage.isEmpty()) {
                // 파일 업로드가 제공된 경우
                profile = profileService.updateProfileWithImage(profileId, nickname, profileImage, introduction);
            } else {
                // URL 또는 null이 제공된 경우
                profile = profileService.updateProfile(profileId, nickname, profileImageUrl, introduction);
            }
            
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            log.error("Failed to update profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while updating profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ProfileResponse(null, "프로필 수정 중 오류가 발생했습니다: " + e.getMessage()));
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