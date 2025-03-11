package com.travelonna.demo.domain.follow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.follow.entity.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    
    // 특정 사용자가 팔로우하는 모든 관계 조회
    List<Follow> findAllByFromUser(Integer fromUser);
    
    // 특정 사용자를 팔로우하는 모든 관계 조회
    List<Follow> findAllByToUser(Integer toUser);
    
    // 특정 프로필을 팔로우하는 모든 관계 조회
    List<Follow> findAllByProfileId(Integer profileId);
    
    // 특정 사용자가 특정 사용자를 팔로우하는지 확인
    Optional<Follow> findByFromUserAndToUser(Integer fromUser, Integer toUser);
    
    // 특정 사용자가 특정 프로필을 팔로우하는지 확인
    Optional<Follow> findByFromUserAndProfileId(Integer fromUser, Integer profileId);
    
    // 특정 사용자가 특정 사용자를 팔로우하는 관계 삭제
    void deleteByFromUserAndToUser(Integer fromUser, Integer toUser);
    
    // 특정 사용자가 특정 프로필을 팔로우하는 관계 삭제
    void deleteByFromUserAndProfileId(Integer fromUser, Integer profileId);
    
    // 특정 사용자를 팔로우하는 수 카운트
    long countByToUser(Integer toUser);
    
    // 특정 사용자가 팔로우하는 수 카운트
    long countByFromUser(Integer fromUser);
    
    // 특정 프로필을 팔로우하는 수 카운트
    long countByProfileId(Integer profileId);
} 