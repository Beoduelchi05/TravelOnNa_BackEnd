package com.travelonna.demo.domain.auth.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.travelonna.demo.global.security.oauth2.OAuth2AuthenticationService;
import com.travelonna.demo.global.security.oauth2.TokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

            // scope 정보 추출
            String scope = tokenResponse.getScope();
            log.debug("Token scope: {}", scope);

            // 사용자 인증 및 JWT 토큰 생성
            log.info("Generating JWT tokens");
            TokenResponse response = oAuth2AuthenticationService.authenticateUser(email, name, scope);
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
    
        // 테스트용 scope 설정
        String testScope = "email profile";
        return oAuth2AuthenticationService.authenticateUser(email, name, testScope);
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

    /**
     * Google OAuth 인증 URL을 생성합니다.
     * 이 URL로 사용자를 리다이렉트하면 Google 로그인 페이지가 표시됩니다.
     */
    public String createGoogleAuthorizationUrl() {
        log.info("Creating Google authorization URL");
        
        // Google OAuth 2.0 인증 엔드포인트
        String baseUrl = "https://accounts.google.com/o/oauth2/v2/auth";
        
        // 필수 파라미터
        String responseType = "code";
        String scope = "email profile";
        
        // CSRF 공격 방지를 위한 상태 파라미터 생성
        String state = generateRandomState();
        
        // URL 생성
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?client_id=").append(clientId);
        urlBuilder.append("&redirect_uri=").append(redirectUri);
        urlBuilder.append("&response_type=").append(responseType);
        urlBuilder.append("&scope=").append(scope);
        urlBuilder.append("&state=").append(state); // 상태 파라미터 추가
        urlBuilder.append("&access_type=offline"); // 리프레시 토큰을 받기 위해 필요
        urlBuilder.append("&prompt=consent"); // 항상 동의 화면 표시
        
        String authUrl = urlBuilder.toString();
        log.debug("Generated Google authorization URL: {}", authUrl);
        
        return authUrl;
    }

    /**
     * CSRF 공격 방지를 위한 랜덤 상태 문자열을 생성합니다.
     */
    private String generateRandomState() {
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
} 