package com.travelonna.demo.domain.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanUpdateMessage {
    private String groupUrl;
    private String type; // 업데이트 타입 (추가, 수정, 삭제 등)
    private Long planId;
    private Long placeId;
    private String content; // 변경된 내용 (JSON 문자열)
    private Integer userId; // 업데이트한 사용자 ID
    private String userName; // 업데이트한 사용자 이름
    private Long timestamp;
} 