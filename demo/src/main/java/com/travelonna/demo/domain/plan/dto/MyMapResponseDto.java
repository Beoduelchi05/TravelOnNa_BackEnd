package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyMapResponseDto {
    
    private Integer mymapId;
    private Integer logId;
    private String logComment;
    private Boolean logIsPublic;
    private LocalDateTime logCreatedAt;
    private Integer userId;
    private String userName;
    private Long mapCodeId;
    private String mapCodeCity;
    private String mapCodeDistrict;
    private Integer planId;
    private String planTitle;
    private String planLocation;
    private Integer placeId;
    private String placeName;
    private String placeAddress;
    
    public MyMapResponseDto() {}
} 