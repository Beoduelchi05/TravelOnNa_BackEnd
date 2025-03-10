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
        log.info("Google login request received");
        log.debug("Code length: {}", request.getCode().length());
        log.debug("Code first 10 chars: {}", request.getCode().substring(0, Math.min(10, request.getCode().length())));
        
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
            log.info("Calling authenticateWithGoogle method");
            TokenResponse tokenResponse = authService.authenticateWithGoogle(request.getCode());
            log.info("Google login successful");
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Error during Google authentication: {}", e.getMessage());
            log.error("Error details: ", e);
            throw e;
        }
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

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        log.info("OPTIONS request received");
        return ResponseEntity.ok().build();
    }
} 