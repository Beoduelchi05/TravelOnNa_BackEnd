package com.travelonna.demo.domain.follow.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.follow.dto.FollowResponseDto;
import com.travelonna.demo.domain.follow.entity.Follow;
import com.travelonna.demo.domain.follow.repository.FollowRepository;
import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.service.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final ProfileService profileService;

    /**
     * 프로필 팔로우하기
     * @param fromUser 팔로우하는 사용자 ID (나)
     * @param toUser 팔로우할 사용자 ID (상대방)
     * @return 팔로우 응답 DTO
     */
    @Transactional
    public FollowResponseDto followProfile(Integer fromUser, Integer toUser) {
        log.info("사용자 ID: {}가 사용자 ID: {}를 팔로우합니다", fromUser, toUser);
        
        // 입력값 검증
        if (fromUser == null || toUser == null) {
            log.error("사용자 ID가 null입니다. fromUser: {}, toUser: {}", fromUser, toUser);
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            // 내 프로필 조회
            Profile myProfile = null;
            try {
                myProfile = profileService.getProfileByUserId(fromUser);
                log.info("내 프로필 조회 성공: 사용자 ID {}, 프로필 ID {}", fromUser, myProfile.getProfileId());
            } catch (Exception e) {
                log.error("내 프로필 조회 실패: 사용자 ID {}, 오류: {}", fromUser, e.getMessage());
                throw new IllegalArgumentException("해당 사용자 ID에 대한 프로필을 찾을 수 없습니다: " + fromUser);
            }
            Integer myProfileId = myProfile.getProfileId();
            
            // 상대방 프로필 확인 (존재하는지만 확인)
            Profile toProfile = null;
            try {
                toProfile = profileService.getProfileByUserId(toUser);
                log.info("상대방 프로필 조회 성공: 사용자 ID {}, 프로필 ID {}", toUser, toProfile.getProfileId());
            } catch (Exception e) {
                log.error("상대방 프로필 조회 실패: 사용자 ID {}, 오류: {}", toUser, e.getMessage());
                throw new IllegalArgumentException("해당 사용자 ID에 대한 프로필을 찾을 수 없습니다: " + toUser);
            }
            
            // 자기 자신을 팔로우하는 경우 예외 처리
            if (fromUser.equals(toUser)) {
                log.error("자기 자신을 팔로우할 수 없습니다. fromUser: {}, toUser: {}", fromUser, toUser);
                throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
            }
            
            // 이미 팔로우 중인지 확인
            Optional<Follow> existingFollow = followRepository.findByFromUserAndToUser(fromUser, toUser);
            if (existingFollow.isPresent()) {
                log.info("이미 팔로우 중입니다");
                return FollowResponseDto.fromEntity(existingFollow.get(), true);
            }
            
            // 새로운 팔로우 관계 생성
            Follow follow = Follow.builder()
                    .fromUser(fromUser)
                    .toUser(toUser)
                    .profileId(myProfileId) // 내 프로필 ID 설정
                    .build();
            
            log.info("팔로우 관계 생성 시도: fromUser={}, toUser={}, profileId={}", 
                    follow.getFromUser(), follow.getToUser(), follow.getProfileId());
            
            Follow savedFollow = followRepository.save(follow);
            log.info("팔로우 관계가 생성되었습니다. ID: {}", savedFollow.getId());
            
            return FollowResponseDto.fromEntity(savedFollow, true);
        } catch (Exception e) {
            log.error("팔로우 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로우 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 프로필 언팔로우하기
     * @param fromUser 팔로우하는 사용자 ID (나)
     * @param toUser 언팔로우할 사용자 ID (상대방)
     */
    @Transactional
    public void unfollowProfile(Integer fromUser, Integer toUser) {
        log.info("사용자 ID: {}가 사용자 ID: {}를 언팔로우합니다", fromUser, toUser);
        
        // 입력값 검증
        if (fromUser == null || toUser == null) {
            log.error("사용자 ID가 null입니다. fromUser: {}, toUser: {}", fromUser, toUser);
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            // 팔로우 관계 확인
            Optional<Follow> existingFollow = followRepository.findByFromUserAndToUser(fromUser, toUser);
            if (existingFollow.isEmpty()) {
                log.info("팔로우 관계가 존재하지 않습니다");
                return;
            }
            
            // 팔로우 관계 삭제
            followRepository.deleteByFromUserAndToUser(fromUser, toUser);
            log.info("팔로우 관계가 삭제되었습니다");
        } catch (Exception e) {
            log.error("언팔로우 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("언팔로우 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 팔로우 상태 확인
     * @param fromUser 팔로우하는 사용자 ID (나)
     * @param toUser 팔로우 대상 사용자 ID (상대방)
     * @return 팔로우 여부
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Integer fromUser, Integer toUser) {
        // 입력값 검증
        if (fromUser == null || toUser == null) {
            log.error("사용자 ID가 null입니다. fromUser: {}, toUser: {}", fromUser, toUser);
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            return followRepository.findByFromUserAndToUser(fromUser, toUser).isPresent();
        } catch (Exception e) {
            log.error("팔로우 상태 확인 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로우 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 프로필의 팔로워 목록 조회
     * @param profileId 조회할 프로필 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     */
    @Transactional(readOnly = true)
    public List<FollowResponseDto> getProfileFollowers(Integer profileId, Integer currentUserId) {
        log.info("프로필 ID: {}의 팔로워 목록을 조회합니다", profileId);
        
        // 입력값 검증
        if (profileId == null) {
            log.error("프로필 ID가 null입니다.");
            throw new IllegalArgumentException("프로필 ID가 null입니다.");
        }
        
        try {
            // 프로필 ID로 사용자 ID 조회
            Profile profile = profileService.getProfileById(profileId);
            Integer userId = profile.getUserId();
            
            log.info("프로필 ID: {}의 사용자 ID: {}에 대한 팔로워 목록을 조회합니다", profileId, userId);
            
            // 해당 사용자를 팔로우하는 모든 사용자 목록 조회
            List<Follow> followers = followRepository.findAllByToUser(userId);
            
            return followers.stream()
                    .map(follow -> {
                        boolean isFollowing = false;
                        if (currentUserId != null) {
                            // 현재 로그인한 사용자가 이 팔로워를 팔로우하고 있는지 확인
                            isFollowing = followRepository.findByFromUserAndToUser(currentUserId, follow.getFromUser()).isPresent();
                        }
                        return FollowResponseDto.fromEntity(follow, isFollowing);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("팔로워 목록 조회 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로워 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 사용자의 팔로잉 목록 조회
     * @param userId 조회할 사용자 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     */
    @Transactional(readOnly = true)
    public List<FollowResponseDto> getUserFollowings(Integer userId, Integer currentUserId) {
        log.info("사용자 ID: {}의 팔로잉 목록을 조회합니다", userId);
        
        // 입력값 검증
        if (userId == null) {
            log.error("사용자 ID가 null입니다.");
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        
        try {
            // 해당 사용자가 팔로우하는 모든 프로필 목록 조회
            List<Follow> followings = followRepository.findAllByFromUser(userId);
            
            return followings.stream()
                    .map(follow -> {
                        boolean isFollowing = false;
                        if (currentUserId != null && currentUserId.equals(userId)) {
                            // 자신의 팔로잉 목록을 조회하는 경우 항상 true
                            isFollowing = true;
                        } else if (currentUserId != null) {
                            // 현재 로그인한 사용자가 이 팔로잉 대상을 팔로우하고 있는지 확인
                            isFollowing = followRepository.findByFromUserAndToUser(currentUserId, follow.getToUser()).isPresent();
                        }
                        return FollowResponseDto.fromEntity(follow, isFollowing);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("팔로잉 목록 조회 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로잉 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 프로필의 팔로워 수 조회
     * @param profileId 조회할 프로필 ID
     * @return 팔로워 수
     */
    @Transactional(readOnly = true)
    public long countProfileFollowers(Integer profileId) {
        // 입력값 검증
        if (profileId == null) {
            log.error("프로필 ID가 null입니다.");
            throw new IllegalArgumentException("프로필 ID가 null입니다.");
        }
        
        try {
            // 프로필 ID로 사용자 ID 조회
            Profile profile = profileService.getProfileById(profileId);
            Integer userId = profile.getUserId();
            
            log.info("프로필 ID: {}의 사용자 ID: {}에 대한 팔로워 수를 조회합니다", profileId, userId);
            
            // 해당 사용자를 팔로우하는 수 조회
            return followRepository.countByToUser(userId);
        } catch (Exception e) {
            log.error("팔로워 수 조회 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로워 수 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 프로필의 팔로잉 수 조회
     * @param profileId 조회할 프로필 ID
     * @return 팔로잉 수
     */
    @Transactional(readOnly = true)
    public long countProfileFollowings(Integer profileId) {
        // 입력값 검증
        if (profileId == null) {
            log.error("프로필 ID가 null입니다.");
            throw new IllegalArgumentException("프로필 ID가 null입니다.");
        }
        
        try {
            // 프로필 ID로 사용자 ID 조회
            Profile profile = profileService.getProfileById(profileId);
            Integer userId = profile.getUserId();
            
            log.info("프로필 ID: {}의 사용자 ID: {}에 대한 팔로잉 수를 조회합니다", profileId, userId);
            
            // 해당 사용자가 팔로우하는 수 조회
            return followRepository.countByFromUser(userId);
        } catch (Exception e) {
            log.error("팔로잉 수 조회 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로잉 수 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 프로필의 팔로잉 목록 조회
     * @param profileId 조회할 프로필 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 팔로잉 목록
     */
    @Transactional(readOnly = true)
    public List<FollowResponseDto> getProfileFollowings(Integer profileId, Integer currentUserId) {
        log.info("프로필 ID: {}의 팔로잉 목록을 조회합니다", profileId);
        
        // 입력값 검증
        if (profileId == null) {
            log.error("프로필 ID가 null입니다.");
            throw new IllegalArgumentException("프로필 ID가 null입니다.");
        }
        
        try {
            // 프로필 ID로 사용자 ID 조회
            Profile profile = profileService.getProfileById(profileId);
            Integer userId = profile.getUserId();
            
            log.info("프로필 ID: {}의 사용자 ID: {}에 대한 팔로잉 목록을 조회합니다", profileId, userId);
            
            // 해당 사용자가 팔로우하는 모든 프로필 목록 조회
            List<Follow> followings = followRepository.findAllByFromUser(userId);
            
            return followings.stream()
                    .map(follow -> {
                        boolean isFollowing = false;
                        if (currentUserId != null && currentUserId.equals(userId)) {
                            // 자신의 팔로잉 목록을 조회하는 경우 항상 true
                            isFollowing = true;
                        } else if (currentUserId != null) {
                            // 현재 로그인한 사용자가 이 팔로잉 대상을 팔로우하고 있는지 확인
                            isFollowing = followRepository.findByFromUserAndToUser(currentUserId, follow.getToUser()).isPresent();
                        }
                        return FollowResponseDto.fromEntity(follow, isFollowing);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("팔로잉 목록 조회 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("팔로잉 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 