package com.travelonna.demo.domain.follow.service;

import com.travelonna.demo.domain.follow.dto.FollowResponseDto;
import com.travelonna.demo.domain.follow.entity.Follow;
import com.travelonna.demo.domain.follow.repository.FollowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FollowServiceTest {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Test
    @DisplayName("프로필 팔로우 기능 테스트")
    void followProfileTest() {
        // given
        Integer fromUserId = 1;
        Integer profileId = 2;

        // when
        FollowResponseDto responseDto = followService.followProfile(fromUserId, profileId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getFromUser()).isEqualTo(fromUserId);
        assertThat(responseDto.getProfileId()).isEqualTo(profileId);
        assertThat(responseDto.getToUser()).isEqualTo(0);
        assertThat(responseDto.isFollowing()).isTrue();

        // 데이터베이스에 저장된 값 확인
        Follow savedFollow = followRepository.findById(responseDto.getId()).orElse(null);
        assertThat(savedFollow).isNotNull();
        assertThat(savedFollow.getFromUser()).isEqualTo(fromUserId);
        assertThat(savedFollow.getProfileId()).isEqualTo(profileId);
        assertThat(savedFollow.getToUser()).isEqualTo(0);
    }

    @Test
    @DisplayName("프로필 언팔로우 기능 테스트")
    void unfollowProfileTest() {
        // given
        Integer fromUserId = 1;
        Integer profileId = 2;
        FollowResponseDto responseDto = followService.followProfile(fromUserId, profileId);
        assertThat(followRepository.findById(responseDto.getId())).isPresent();

        // when
        followService.unfollowProfile(fromUserId, profileId);

        // then
        assertThat(followRepository.findByFromUserAndProfileId(fromUserId, profileId)).isEmpty();
    }

    @Test
    @DisplayName("팔로우 상태 확인 테스트")
    void isFollowingTest() {
        // given
        Integer fromUserId = 1;
        Integer profileId = 2;
        followService.followProfile(fromUserId, profileId);

        // when
        boolean isFollowing = followService.isFollowing(fromUserId, profileId);

        // then
        assertThat(isFollowing).isTrue();
    }

    @Test
    @DisplayName("프로필 팔로워 목록 조회 테스트")
    void getProfileFollowersTest() {
        // given
        Integer fromUserId1 = 1;
        Integer fromUserId2 = 2;
        Integer profileId = 3;
        followService.followProfile(fromUserId1, profileId);
        followService.followProfile(fromUserId2, profileId);

        // when
        var followers = followService.getProfileFollowers(profileId, null);

        // then
        assertThat(followers).hasSize(2);
        assertThat(followers).extracting("fromUser").containsExactlyInAnyOrder(fromUserId1, fromUserId2);
    }

    @Test
    @DisplayName("사용자 팔로잉 목록 조회 테스트")
    void getUserFollowingsTest() {
        // given
        Integer userId = 1;
        Integer profileId1 = 2;
        Integer profileId2 = 3;
        followService.followProfile(userId, profileId1);
        followService.followProfile(userId, profileId2);

        // when
        var followings = followService.getUserFollowings(userId, userId);

        // then
        assertThat(followings).hasSize(2);
        assertThat(followings).extracting("profileId").containsExactlyInAnyOrder(profileId1, profileId2);
        assertThat(followings).extracting("isFollowing").containsOnly(true);
    }

    @Test
    @DisplayName("프로필 팔로워 수 조회 테스트")
    void countProfileFollowersTest() {
        // given
        Integer fromUserId1 = 1;
        Integer fromUserId2 = 2;
        Integer profileId = 3;
        followService.followProfile(fromUserId1, profileId);
        followService.followProfile(fromUserId2, profileId);

        // when
        long count = followService.countProfileFollowers(profileId);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자 팔로잉 수 조회 테스트")
    void countUserFollowingsTest() {
        // given
        Integer userId = 1;
        Integer profileId1 = 2;
        Integer profileId2 = 3;
        followService.followProfile(userId, profileId1);
        followService.followProfile(userId, profileId2);

        // when
        long count = followService.countUserFollowings(userId);

        // then
        assertThat(count).isEqualTo(2);
    }
} 