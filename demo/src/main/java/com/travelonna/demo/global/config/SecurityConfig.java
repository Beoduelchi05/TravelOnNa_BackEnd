package com.travelonna.demo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.travelonna.demo.global.security.jwt.JwtAuthenticationFilter;
import com.travelonna.demo.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        
        // JwtAuthenticationFilter 인스턴스 생성 및 순서 설정
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 인증이 필요 없는 API
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Actuator 헬스 체크 엔드포인트 인증 없이 허용
                .requestMatchers("/actuator/**").permitAll()
                // 프로필 조회 API는 인증 없이 허용
                .requestMatchers("/api/v1/profiles/user/**").permitAll()
                // 검색 API 인증 없이 허용
                .requestMatchers("/api/v1/search/**").permitAll()
                // 프로필 생성 및 수정 API는 인증 필요
                .requestMatchers(HttpMethod.POST, "/api/v1/profiles").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/profiles/**").authenticated()
                // 프로필 관리자 기능은 인증 필요
                .requestMatchers("/api/v1/profiles/admin/**").authenticated()
                // 테스트용으로 일단 로그 API는 인증 없이 허용
                .requestMatchers("/api/logs/**").permitAll()
                // 테스트 API 인증 없이 허용
                .requestMatchers("/api/v1/plans/test/**").permitAll()
                // 그 외 모든 API는 인증 필요
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 