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
    
    @Value("${google.client-secret}")
    private String clientSecret;
    
    @Value("${google.redirect-uri}")
    private String redirectUri;

    public TokenResponse authenticateWithGoogle(String authorizationCode) {
        log.info("Starting Google authentication process");
        log.debug("Using client ID: {}", clientId);
        log.debug("Authorization code length: {}", authorizationCode != null ? authorizationCode.length() : 0);
        
        // 인증 코드의 일부를 로그에 남김 (보안을 위해 전체 코드는 로그에 남기지 않음)
        if (authorizationCode != null && authorizationCode.length() > 20) {
            String firstPart = authorizationCode.substring(0, 10);
            String lastPart = authorizationCode.substring(authorizationCode.length() - 10);
            log.debug("Authorization code preview: {}...{}", firstPart, lastPart);
        } else if (authorizationCode != null) {
            log.debug("Authorization code is too short to preview safely");
        }
        
        try {
            log.info("Exchanging authorization code for token");
            log.debug("Using client ID: {}", clientId);
            log.debug("Using client secret: {}", clientSecret != null ? "설정됨" : "설정되지 않음");
            log.debug("Using redirect URI: {}", redirectUri);
            
            // Google로부터 토큰 받기 (웹 클라이언트용)
            // 웹 클라이언트 ID를 사용할 때는 client_secret이 필요함
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),                // HTTP 전송 방식
                    GsonFactory.getDefaultInstance(),      // JSON 파서
                    "https://oauth2.googleapis.com/token", // Google OAuth 토큰 엔드포인트
                    clientId,                              // 클라이언트 ID
                    clientSecret,                          // 클라이언트 시크릿
                    authorizationCode,                     // 인증 코드
                    redirectUri                            // 리디렉션 URI (설정 파일에서 가져옴)
                    )
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
            log.error("Error details: ", e);
            
            // 401 Unauthorized 오류에 대한 자세한 로깅
            if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
                log.error("Google authentication failed with 401 Unauthorized. This usually means:");
                log.error("1. The authorization code is invalid or expired");
                log.error("2. The authorization code has already been used");
                log.error("3. The client ID or client secret is incorrect");
                log.error("4. The redirect URI doesn't match the one used to get the authorization code");
                
                // 웹 클라이언트 ID 사용 시 주의사항
                log.error("For Web client IDs, make sure:");
                log.error("1. You're using the correct client ID and client secret from Google Developer Console");
                log.error("2. The authorization code is fresh and hasn't been used before");
                log.error("3. The redirect URI exactly matches the one configured in Google Developer Console");
            }
            
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
        log.info("Starting Google authentication process");
    
        return oAuth2AuthenticationService.authenticateUser(email, name);
    }

    /**
     * 클라이언트 ID의 길이를 반환합니다.
     * 보안상의 이유로 실제 ID는 노출하지 않습니다.
     */
    public int getClientIdLength() {
        return clientId != null ? clientId.length() : 0;
    }

    /**
     * 클라이언트 시크릿이 설정되었는지 확인합니다.
     */
    public boolean isClientSecretConfigured() {
        return clientSecret != null && !clientSecret.isEmpty();
    }
    
    /**
     * 리디렉션 URI를 반환합니다.
     */
    public String getRedirectUri() {
        return redirectUri;
    }
} 