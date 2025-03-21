package com.travelonna.demo.domain.auth.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.auth.dto.GoogleTokenRequest;
import com.travelonna.demo.domain.auth.dto.RefreshTokenRequest;
import com.travelonna.demo.domain.auth.service.AuthService;
import com.travelonna.demo.global.security.oauth2.TokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API (인증 불필요)")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Google 로그인", description = "Google OAuth2.0 인증 코드를 사용하여 로그인합니다. (인증 불필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(
            @Parameter(description = "Google 인증 코드") @Valid @RequestBody GoogleTokenRequest request,
            HttpServletRequest httpRequest) {
        log.info("Google login request received from web client");
        log.debug("Code length: {}", request.getCode().length());
        
        // 인증 코드의 일부를 로그에 남김 (보안을 위해 전체 코드는 로그에 남기지 않음)
        if (request.getCode() != null && request.getCode().length() > 20) {
            String firstPart = request.getCode().substring(0, 10);
            String lastPart = request.getCode().substring(request.getCode().length() - 10);
            log.debug("Authorization code preview: {}...{}", firstPart, lastPart);
        }
        
        // 요청 정보 로깅
        log.debug("Request URI: {}", httpRequest.getRequestURI());
        log.debug("Request method: {}", httpRequest.getMethod());
        log.debug("Remote address: {}", httpRequest.getRemoteAddr());
        
        // 헤더 정보 로깅
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("Header - {}: {}", headerName, httpRequest.getHeader(headerName));
        }
        
        try {
            log.info("Calling authenticateWithGoogle method for web client");
            TokenResponse tokenResponse = authService.authenticateWithGoogle(request.getCode());
            log.info("Google login successful for web client");
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Error during Google authentication for web client: {}", e.getMessage());
            log.error("Error details: ", e);
            
            // 오류 유형에 따라 다른 응답 반환
            if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
                log.error("Google authentication failed with 401 Unauthorized. This usually means the authorization code is invalid or expired.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            
            throw e;
        }
    }

    // 웹 클라이언트용 Google OAuth 콜백 엔드포인트 추가
    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request) {
        log.info("Google OAuth callback received");
        log.debug("Code length: {}", code.length());
        log.debug("State: {}", state);
        
        // 여기서는 실제 인증 처리를 하지 않고, 프론트엔드로 리다이렉트하거나 코드를 반환
        // 프론트엔드에서 이 코드를 사용하여 /api/auth/google 엔드포인트를 호출해야 함
        
        // 실제 구현에서는 프론트엔드 URL로 리다이렉트하는 것이 좋음
        // 예: return ResponseEntity.status(HttpStatus.FOUND).header("Location", "https://your-frontend-url?code=" + code).build();
        
        return ResponseEntity.ok("Authorization code received. Please use this code to complete authentication: " + code);
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다. (인증 불필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", 
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "리프레시 토큰") @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "OAuth 설정 조회", description = "OAuth 관련 설정 정보를 조회합니다. (인증 불필요)")
    @GetMapping("/oauth-config")
    public ResponseEntity<Map<String, Object>> getOAuthConfig() {
        log.info("OAuth configuration request received");
        
        Map<String, Object> config = new HashMap<>();
        config.put("clientIdLength", authService.getClientIdLength());
        config.put("clientSecretConfigured", authService.isClientSecretConfigured());
        config.put("redirectUri", authService.getRedirectUri());
        config.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Google 로그인 페이지", description = "Google OAuth2.0 로그인 페이지로 리다이렉트하거나 로그인 URL을 반환합니다. (인증 불필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Google 로그인 페이지로 리다이렉트"),
        @ApiResponse(responseCode = "200", description = "Google 로그인 URL 반환"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/google")
    public ResponseEntity<?> googleLoginRedirect(
            @Parameter(description = "리다이렉트 여부") @RequestParam(value = "redirect", defaultValue = "true") boolean redirect,
            HttpServletResponse response) {
        log.info("Google login request received, redirect={}", redirect);
        
        try {
            // Google OAuth 로그인 URL 생성
            String googleAuthUrl = authService.createGoogleAuthorizationUrl();
            
            if (redirect) {
                // 리다이렉트 응답 생성
                log.info("Redirecting to Google login page");
                response.sendRedirect(googleAuthUrl);
                return ResponseEntity.status(HttpStatus.FOUND).build();
            } else {
                // JSON 응답 생성
                log.info("Returning Google login URL");
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("loginUrl", googleAuthUrl);
                responseBody.put("message", "Use this URL to login with Google");
                responseBody.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(responseBody);
            }
        } catch (Exception e) {
            log.error("Error creating Google authorization URL: {}", e.getMessage());
            log.error("Error details: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        log.info("OPTIONS request received");
        return ResponseEntity.ok().build();
    }
} 