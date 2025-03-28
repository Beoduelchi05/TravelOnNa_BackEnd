package com.travelonna.demo.domain.group.controller;

import com.travelonna.demo.domain.group.dto.PlanUpdateMessage;
import com.travelonna.demo.domain.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "그룹 일정", description = "그룹 일정 관리 API (인증 필요)")
public class GroupPlanWebSocketController {

    private final GroupService groupService;

    /**
     * 사용자가 그룹 계획에 변경 사항을 보낼 때 사용하는 엔드포인트
     * @param groupUrl 그룹의 고유 URL
     * @param message 계획 업데이트 메시지
     * @param headerAccessor WebSocket 세션 정보
     * @return 다른 참여자들에게 전달될 메시지
     */
    @MessageMapping("/plan/{groupUrl}")
    @SendTo("/topic/plan/{groupUrl}")
    public PlanUpdateMessage sendUpdate(
            @Parameter(description = "그룹의 고유 URL", example = "travel-group") @DestinationVariable String groupUrl,
            @Parameter(description = "계획 업데이트 메시지") @Payload PlanUpdateMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // 로깅
        log.info("Received plan update message: {} for group: {}", message, groupUrl);
        
        // 타임스탬프 추가
        if (message.getTimestamp() == null) {
            message = PlanUpdateMessage.builder()
                    .groupUrl(message.getGroupUrl())
                    .type(message.getType())
                    .planId(message.getPlanId())
                    .placeId(message.getPlaceId())
                    .content(message.getContent())
                    .userId(message.getUserId())
                    .userName(message.getUserName())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        
        return message;
    }

    /**
     * 사용자가 그룹 계획 페이지에 접속했을 때 호출되는 엔드포인트
     * @param groupUrl 그룹의 고유 URL
     * @param message 접속 메시지
     * @param headerAccessor WebSocket 세션 정보
     * @return 새 사용자가 접속했다는 메시지
     */
    @MessageMapping("/plan/{groupUrl}/join")
    @SendTo("/topic/plan/{groupUrl}")
    public PlanUpdateMessage addUser(
            @Parameter(description = "그룹의 고유 URL", example = "travel-group") @DestinationVariable String groupUrl,
            @Parameter(description = "접속 메시지") @Payload PlanUpdateMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // 세션에 사용자 정보 저장
        headerAccessor.getSessionAttributes().put("username", message.getUserName());
        headerAccessor.getSessionAttributes().put("userId", message.getUserId());
        headerAccessor.getSessionAttributes().put("groupUrl", groupUrl);
        
        // 로깅
        log.info("User {} joined group: {}", message.getUserName(), groupUrl);
        
        return PlanUpdateMessage.builder()
                .groupUrl(groupUrl)
                .type("JOIN")
                .userId(message.getUserId())
                .userName(message.getUserName())
                .timestamp(System.currentTimeMillis())
                .build();
    }
} 