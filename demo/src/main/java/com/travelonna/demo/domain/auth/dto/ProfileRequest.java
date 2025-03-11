package com.travelonna.demo.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequest {
    private Integer userId;
    private String nickname;
    private String profileImage;
    private String introduction;
}