package com.travelonna.demo.domain.user.service;

import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
    private final ProfileRepository profileRepository;
    
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
        // 닉네임 중복 검사 (자기 자신은 제외)
        if (nickname != null) {
            Optional<Profile> existingProfile = profileRepository.findByNickname(nickname);
            if (existingProfile.isPresent() && !existingProfile.get().getProfileId().equals(profileId)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
            }
        }
        
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId));
        
        if (nickname != null) profile.updateNickname(nickname);
        if (profileImage != null) profile.updateProfileImage(profileImage);
        if (introduction != null) profile.updateIntroduction(introduction);
        
        return profileRepository.save(profile);
    }
}