package com.travelonna.demo.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.entity.UserToken;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Integer> {
    Optional<UserToken> findByRefreshTokenAndRevokedFalse(String refreshToken);
    Optional<UserToken> findByUserAndRevokedFalse(User user);
} 