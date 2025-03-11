package com.travelonna.demo.domain.follow.service;

import com.travelonna.demo.domain.follow.dto.FollowResponseDto;
import com.travelonna.demo.domain.follow.entity.Follow;
import com.travelonna.demo.domain.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    /**
     * 프로필 팔로우하기
     */
    @Transactional
    public FollowResponseDto followProfile(Integer fromUserId, Integer profileId) {
        log.info("사용자 ID: {}가 프로필 ID: {}를 팔로우합니다", fromUserId, profileId);
        
        // 이미 팔로우 중인지 확인
        Optional<Follow> existingFollow = followRepository.findByFromUserAndProfileId(fromUserId, profileId);
        if (existingFollow.isPresent()) {
            log.info("이미 팔로우 중입니다");
            return FollowResponseDto.fromEntity(existingFollow.get(), true);
        }
        
        // 새로운 팔로우 관계 생성
        Follow follow = Follow.builder()
                .fromUser(fromUserId)
                .profileId(profileId)
                .build();
        
        Follow savedFollow = followRepository.save(follow);
        log.info("팔로우 관계가 생성되었습니다. ID: {}", savedFollow.getId());
        
        return FollowResponseDto.fromEntity(savedFollow, true);
    }
    
    /**
     * 프로필 언팔로우하기
     */
    @Transactional
    public void unfollowProfile(Integer fromUserId, Integer profileId) {
        log.info("사용자 ID: {}가 프로필 ID: {}를 언팔로우합니다", fromUserId, profileId);
        
        // 팔로우 관계 확인
        Optional<Follow> existingFollow = followRepository.findByFromUserAndProfileId(fromUserId, profileId);
        if (existingFollow.isEmpty()) {
            log.info("팔로우 관계가 존재하지 않습니다");
            return;
        }
        
        // 팔로우 관계 삭제
        followRepository.deleteByFromUserAndProfileId(fromUserId, profileId);
        log.info("팔로우 관계가 삭제되었습니다");
    }
    
    /**
     * 팔로우 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Integer fromUserId, Integer profileId) {
        return followRepository.findByFromUserAndProfileId(fromUserId, profileId).isPresent();
    }
    
    /**
     * 프로필의 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowResponseDto> getProfileFollowers(Integer profileId, Integer currentUserId) {
        log.info("프로필 ID: {}의 팔로워 목록을 조회합니다", profileId);
        
        List<Follow> followers = followRepository.findAllByProfileId(profileId);
        
        return followers.stream()
                .map(follow -> {
                    boolean isFollowing = false;
                    if (currentUserId != null) {
                        isFollowing = followRepository.findByFromUserAndToUser(currentUserId, follow.getFromUser()).isPresent();
                    }
                    return FollowResponseDto.fromEntity(follow, isFollowing);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 팔로잉 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowResponseDto> getUserFollowings(Integer userId, Integer currentUserId) {
        log.info("사용자 ID: {}의 팔로잉 목록을 조회합니다", userId);
        
        List<Follow> followings = followRepository.findAllByFromUser(userId);
        
        return followings.stream()
                .map(follow -> {
                    boolean isFollowing = false;
                    if (currentUserId != null && currentUserId.equals(userId)) {
                        isFollowing = true;
                    } else if (currentUserId != null) {
                        isFollowing = followRepository.findByFromUserAndProfileId(currentUserId, follow.getProfileId()).isPresent();
                    }
                    return FollowResponseDto.fromEntity(follow, isFollowing);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 프로필의 팔로워 수 조회
     */
    @Transactional(readOnly = true)
    public long countProfileFollowers(Integer profileId) {
        return followRepository.countByProfileId(profileId);
    }
    
    /**
     * 사용자의 팔로잉 수 조회
     */
    @Transactional(readOnly = true)
    public long countUserFollowings(Integer userId) {
        return followRepository.countByFromUser(userId);
    }
} 