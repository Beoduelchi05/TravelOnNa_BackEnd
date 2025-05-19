package com.travelonna.demo.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.repository.ProfileRepository;
import com.travelonna.demo.global.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

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
        
        // 사용자 ID 중복 검사
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("해당 사용자는 이미 프로필을 가지고 있습니다. " + "userID: " + userId);
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
        
        // 사용자 ID 중복 검사
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("해당 사용자는 이미 프로필을 가지고 있습니다: " + userId);
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
    
    /**
     * 사용자 ID에 해당하는 프로필을 Optional로 반환합니다. 
     * 존재하지 않는 경우 빈 Optional을 반환합니다.
     * 여러 결과가 있는 경우 첫 번째 결과만 반환합니다.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Profile> findProfileByUserId(Integer userId) {
        try {
            // 직접 native query 또는 JPQL로 첫 번째 결과만 가져오는 방식으로 변경
            List<Profile> profiles = profileRepository.findAll().stream()
                    .filter(p -> {
                        Integer pUserId = p.getUserId();
                        return pUserId != null && pUserId.equals(userId);
                    })
                    .limit(1)
                    .collect(Collectors.toList());
                    
            if (profiles.isEmpty()) {
                return java.util.Optional.empty();
            } else {
                return java.util.Optional.of(profiles.get(0));
            }
        } catch (Exception e) {
            // 예외 처리
            log.error("프로필 조회 중 오류 발생: {}", e.getMessage());
            return java.util.Optional.empty();
        }
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
    
    /**
     * 사용자 ID에 대한 중복 프로필 정리
     * 여러 프로필이 존재할 경우 가장 최근에 생성된 프로필만 남기고 나머지는 삭제합니다.
     * 
     * @param userId 정리할 사용자 ID
     * @return 삭제된 프로필 수
     */
    @Transactional
    public int cleanupDuplicateProfiles(Integer userId) {
        try {
            // 해당 사용자의 모든 프로필을 가져오기
            List<Profile> profiles = profileRepository.findAll().stream()
                    .filter(p -> userId.equals(p.getUserId()))
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt())) // 최신순 정렬
                    .collect(Collectors.toList());
            
            if (profiles.size() <= 1) {
                // 프로필이 없거나 하나만 있으면 정리할 필요 없음
                return 0;
            }
            
            // 가장 최근 프로필 외의 모든 프로필 삭제
            int deletedCount = 0;
            for (int i = 1; i < profiles.size(); i++) {
                profileRepository.delete(profiles.get(i));
                deletedCount++;
            }
            
            log.info("사용자 ID {}의 중복 프로필 {} 개를 정리했습니다", userId, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("프로필 정리 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
}