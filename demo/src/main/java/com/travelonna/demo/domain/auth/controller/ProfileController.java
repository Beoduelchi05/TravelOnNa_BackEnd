package com.travelonna.demo.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travelonna.demo.domain.auth.dto.ProfileRequest;
import com.travelonna.demo.domain.auth.dto.ProfileResponse;
import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(@RequestBody ProfileRequest request) {
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
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(@PathVariable Integer userId) {
        try {
            Profile profile = profileService.getProfileByUserId(userId);
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        }
    }
    
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Integer profileId) {
        try {
            Profile profile = profileService.getProfileById(profileId);
            return ResponseEntity.ok(convertToResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProfileResponse(null, e.getMessage()));
        }
    }
    
    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Integer profileId,
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