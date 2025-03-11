package com.travelonna.demo.domain.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Integer profileId;
    private Integer userId;
    private String nickname;
    private String profileImage;
    private String introduction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 에러 응답용 생성자
    public ProfileResponse(Integer profileId, String errorMessage) {
        this.profileId = profileId;
        this.introduction = errorMessage; // 에러 메시지를 introduction 필드에 임시 저장
    }
} 
