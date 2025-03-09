package com.travelonna.demo.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.travelonna.demo.global.security.oauth2.OAuth2AuthenticationService;
import com.travelonna.demo.global.security.oauth2.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuth2AuthenticationService oAuth2AuthenticationService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public TokenResponse authenticateWithGoogle(String authorizationCode) {
        log.info("Starting Google authentication process");
        log.debug("Using client ID: {}", clientId);
        log.debug("Using redirect URI: {}", redirectUri);
        
        try {
            log.info("Exchanging authorization code for token");
            // Google로부터 토큰 받기 (안드로이드 클라이언트는 client_secret이 없음)
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    null, // 안드로이드 클라이언트는 client_secret이 없음
                    authorizationCode,
                    redirectUri)
                    .execute();
            
            log.info("Token exchange successful");
            
            // ID 토큰 검증
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            // 사용자 정보 추출
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            
            log.info("User authenticated with Google: email={}", email);
            log.debug("User name: {}", name);

            // 사용자 인증 및 JWT 토큰 생성
            log.info("Generating JWT tokens");
            TokenResponse response = oAuth2AuthenticationService.authenticateUser(email, name);
            log.info("JWT tokens generated successfully");
            
            return response;
        } catch (IOException e) {
            log.error("Error authenticating with Google: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with Google", e);
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        return oAuth2AuthenticationService.refreshToken(refreshToken);
    }

    /**
     * 테스트용 인증 메서드
     * 실제 구글 인증 과정 없이 직접 사용자 정보로 인증합니다.
     */
    @Profile({"dev", "local"})
    public TokenResponse authenticateForTest(String email, String name) {
        log.info("Test authentication for user: {}", email);
        return oAuth2AuthenticationService.authenticateUser(email, name);
    }
} 