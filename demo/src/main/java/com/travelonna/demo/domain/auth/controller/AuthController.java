package com.travelonna.demo.domain.auth.controller;

import com.travelonna.demo.domain.auth.dto.GoogleTokenRequest;
import com.travelonna.demo.domain.auth.dto.RefreshTokenRequest;
import com.travelonna.demo.domain.auth.dto.TestLoginRequest;
import com.travelonna.demo.domain.auth.service.AuthService;
import com.travelonna.demo.global.security.oauth2.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Google 로그인", description = "Google OAuth2.0 인증 코드를 사용하여 로그인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(
            @Parameter(description = "Google 인증 코드", required = true)
            @Valid @RequestBody GoogleTokenRequest request,
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

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", 
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "리프레시 토큰", required = true)
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "테스트용 로그인 (개발 환경 전용)", description = "개발 환경에서만 사용 가능한 테스트용 로그인 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/test-login")
    @Profile({"dev", "local"}) // 개발 환경에서만 사용 가능
    public ResponseEntity<TokenResponse> testLogin(
            @Parameter(description = "테스트 로그인 정보", required = true)
            @Valid @RequestBody TestLoginRequest request) {
        log.info("Test login request received for email: {}", request.getEmail());
        TokenResponse tokenResponse = authService.authenticateForTest(request.getEmail(), request.getName());
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("Ping request received");
        return ResponseEntity.ok("pong");
    }
    
    @PostMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug(@RequestBody(required = false) Map<String, Object> payload, 
                                                   HttpServletRequest request) {
        log.info("Debug request received");
        
        // 요청 정보 로깅
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Request method: {}", request.getMethod());
        
        // 헤더 정보 로깅
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("Header - {}: {}", headerName, request.getHeader(headerName));
        }
        
        // 요청 본문 로깅
        log.info("Request payload: {}", payload);
        
        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Debug endpoint called successfully");
        response.put("timestamp", System.currentTimeMillis());
        response.put("receivedPayload", payload);
        
        return ResponseEntity.ok(response);
    }

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

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        log.info("OPTIONS request received");
        return ResponseEntity.ok().build();
    }
} 