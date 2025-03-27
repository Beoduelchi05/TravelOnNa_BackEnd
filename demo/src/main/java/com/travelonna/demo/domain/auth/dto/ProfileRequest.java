package com.travelonna.demo.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 프로필 요청 DTO
 * 참고: 이 클래스는 현재 컨트롤러에서 직접 사용하지 않으며, 향후 확장을 위해 유지됩니다.
 */
@Getter
@Setter
public class ProfileRequest {
    private Integer userId;
    private String nickname;
    private String profileImageUrl;
    private String introduction;
}