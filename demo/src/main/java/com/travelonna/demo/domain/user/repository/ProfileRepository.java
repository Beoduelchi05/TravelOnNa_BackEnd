package com.travelonna.demo.domain.user.repository;

import com.travelonna.demo.domain.user.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    boolean existsByNickname(String nickname);
    Optional<Profile> findByNickname(String nickname);
    Optional<Profile> findByUserId(Integer userId);
}