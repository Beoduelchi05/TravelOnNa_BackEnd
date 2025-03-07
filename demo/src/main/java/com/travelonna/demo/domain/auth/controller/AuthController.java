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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
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
            @Valid @RequestBody GoogleTokenRequest request) {
        log.info("Google login request received for code: {}", request.getCode());
        TokenResponse tokenResponse = authService.authenticateWithGoogle(request.getCode());
        return ResponseEntity.ok(tokenResponse);
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
} 