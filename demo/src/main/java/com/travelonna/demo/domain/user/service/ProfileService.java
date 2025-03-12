package com.travelonna.demo.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

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
}