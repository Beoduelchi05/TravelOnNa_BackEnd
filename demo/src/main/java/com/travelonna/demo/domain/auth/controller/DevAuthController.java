package com.travelonna.demo.domain.auth.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.auth.service.AuthService;
import com.travelonna.demo.global.security.oauth2.TokenResponse;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 개발 환경에서만 작동하는 인증 컨트롤러
 * 테스트를 위한 자동 로그인 기능을 제공합니다.
 * 프로덕션 환경에서는 활성화되지 않습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/dev")
@Profile({"dev", "local"}) // 개발 환경에서만 활성화
@RequiredArgsConstructor
public class DevAuthController {

    private final AuthService authService;

    /**
     * 개발 테스트용 자동 로그인 API
     * 사용자가 제공한 이메일과 이름으로 자동 로그인을 수행합니다.
     * 이메일과 이름을 제공하지 않으면 기본값으로 테스트 계정을 사용합니다.
     */
    @PostMapping("/auto-login")
    public ResponseEntity<TokenResponse> autoLogin(@RequestBody DevLoginRequest request) {
        log.info("개발용 자동 로그인 요청: {}", request);
        
        // 개발용 테스트 계정으로 자동 로그인
        String email = request.getEmail() != null ? request.getEmail() : "bdc@gmail.com";
        String name = request.getName() != null ? request.getName() : "Dev_bdc";
        
        TokenResponse tokenResponse = authService.authenticateForTest(email, name);
        log.info("개발용 자동 로그인 성공: userId={}", tokenResponse.getUser_id());
        
        return ResponseEntity.ok(tokenResponse);
    }
}

/**
 * 개발용 자동 로그인 요청 DTO
 */
@Data
class DevLoginRequest {
    private String email;
    private String name;
} 