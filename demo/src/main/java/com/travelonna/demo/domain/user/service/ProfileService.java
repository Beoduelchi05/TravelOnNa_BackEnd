package com.travelonna.demo.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.repository.ProfileRepository;
import com.travelonna.demo.global.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final S3Service s3Service;
    
    public Profile createProfile(Integer userId, String nickname, String profileImage, String introduction) {
        // 닉네임 중복 검사
        if (profileRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        
        Profile profile = Profile.builder()
                .userId(userId)
                .nickname(nickname)
                .profileImage(profileImage)
                .introduction(introduction)
                .build();
                
        return profileRepository.save(profile);
    }
    
    /**
     * 프로필을 생성하고 이미지 파일을 S3에 업로드합니다.
     */
    public Profile createProfileWithImage(Integer userId, String nickname, MultipartFile profileImageFile, String introduction) {
        // 닉네임 중복 검사
        if (profileRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        
        // 프로필 이미지 업로드
        String profileImageUrl = null;
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            profileImageUrl = s3Service.uploadProfileImage(profileImageFile);
            log.info("Uploaded profile image: {}", profileImageUrl);
        }
        
        Profile profile = Profile.builder()
                .userId(userId)
                .nickname(nickname)
                .profileImage(profileImageUrl)
                .introduction(introduction)
                .build();
                
        return profileRepository.save(profile);
    }
    
    @Transactional(readOnly = true)
    public Profile getProfileById(Integer profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId));
    }
    
    @Transactional(readOnly = true)
    public Profile getProfileByUserId(Integer userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 프로필을 찾을 수 없습니다: " + userId));
    }
    
    public Profile updateProfile(Integer profileId, String nickname, String profileImage, String introduction) {
        Profile profile = getProfileById(profileId);
        
        // 닉네임이 변경되었고, 새 닉네임이 이미 사용 중인 경우 예외 발생
        if (nickname != null && !nickname.equals(profile.getNickname()) && profileRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        
        // 각 필드가 null이 아닌 경우에만 업데이트
        if (nickname != null) {
            profile.updateNickname(nickname);
        }
        
        if (profileImage != null) {
            profile.updateProfileImage(profileImage);
        }
        
        if (introduction != null) {
            profile.updateIntroduction(introduction);
        }
        
        return profileRepository.save(profile);
    }
    
    /**
     * 프로필을 업데이트하고 이미지 파일을 S3에 업로드합니다.
     */
    public Profile updateProfileWithImage(Integer profileId, String nickname, MultipartFile profileImageFile, String introduction) {
        Profile profile = getProfileById(profileId);
        
        // 닉네임이 변경되었고, 새 닉네임이 이미 사용 중인 경우 예외 발생
        if (nickname != null && !nickname.equals(profile.getNickname()) && profileRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        
        // 각 필드가 null이 아닌 경우에만 업데이트
        if (nickname != null) {
            profile.updateNickname(nickname);
        }
        
        // 이미지 파일이 제공된 경우 업로드하고 URL 업데이트
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            // 기존 이미지가 있으면 삭제 (선택 사항)
            if (profile.getProfileImage() != null && !profile.getProfileImage().isEmpty()) {
                try {
                    s3Service.deleteFile(profile.getProfileImage());
                } catch (Exception e) {
                    log.warn("Failed to delete old profile image: {}", e.getMessage());
                }
            }
            
            String profileImageUrl = s3Service.uploadProfileImage(profileImageFile);
            profile.updateProfileImage(profileImageUrl);
            log.info("Updated profile image: {}", profileImageUrl);
        }
        
        if (introduction != null) {
            profile.updateIntroduction(introduction);
        }
        
        return profileRepository.save(profile);
    }
}