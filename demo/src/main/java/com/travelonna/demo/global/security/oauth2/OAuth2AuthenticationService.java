package com.travelonna.demo.global.security.oauth2;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.entity.UserToken;
import com.travelonna.demo.domain.user.repository.UserTokenRepository;
import com.travelonna.demo.domain.user.service.UserService;
import com.travelonna.demo.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2AuthenticationService {

    private final UserService userService;
    private final UserTokenRepository userTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse authenticateUser(String email, String name) {
        User user = userService.createOrUpdateUser(email, name);
        
        // 기존 토큰이 있으면 폐기
        Optional<UserToken> existingToken = userTokenRepository.findByUserAndRevokedFalse(user);
        existingToken.ifPresent(UserToken::revokeToken);
        
        // 사용자 ID 가져오기
        Integer userId = user.getUserId();
        
        // 새 토큰 생성 (user_id 포함)
        String accessToken = jwtTokenProvider.createAccessToken(email, userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, userId);
        
        // 리프레시 토큰 저장
        UserToken userToken = UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .issuedAt(LocalDateTime.now())
                .expiresIn(14 * 24 * 60 * 60) // 14일
                .revoked(false)
                .build();
        
        userTokenRepository.save(userToken);
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600) // 1시간
                .user_id(userId) // user_id 설정
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        Optional<UserToken> userTokenOpt = userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken);
        if (userTokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Refresh token not found or revoked");
        }
        
        UserToken userToken = userTokenOpt.get();
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        Integer userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        
        // 새 액세스 토큰 생성 (user_id 포함)
        String newAccessToken = jwtTokenProvider.createAccessToken(email, userId);
        
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 리프레시 토큰 유지
                .tokenType("Bearer")
                .expiresIn(3600) // 1시간
                .user_id(userId) // user_id 설정
                .build();
    }
} 