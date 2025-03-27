package com.travelonna.demo.domain.auth.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프로필 응답 DTO
 * 프로필 정보 조회 및 수정 결과를 클라이언트에 반환하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    @Schema(description = "프로필 ID (프로필 테이블의 PK)", example = "1")
    private Integer profileId;
    
    @Schema(description = "사용자 ID (회원 테이블의 FK)", example = "1")
    private Integer userId;
    
    @Schema(description = "사용자 닉네임", example = "여행왕")
    private String nickname;
    
    @Schema(description = "프로필 이미지 URL", example = "https://travelonna-image.s3.ap-northeast-2.amazonaws.com/profile/f8d7e9f3-42a1-4b3c-8f6d-1e9b2a3c4d5e.jpg")
    private String profileImage;
    
    @Schema(description = "자기소개", example = "여행을 좋아하는 직장인입니다.")
    private String introduction;
    
    @Schema(description = "프로필 생성 일시", example = "2023-05-15T14:30:15")
    private LocalDateTime createdAt;
    
    @Schema(description = "프로필 수정 일시", example = "2023-05-16T09:45:22")
    private LocalDateTime updatedAt;
    
    @Schema(description = "오류 메시지 (오류 발생 시에만 포함)", example = "이미 사용 중인 닉네임입니다.")
    private String errorMessage;
    
    // 생성자 오버로딩
    public ProfileResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ProfileResponse(Integer profileId, String errorMessage) {
        this.profileId = profileId;
        this.errorMessage = errorMessage;
    }
} 
