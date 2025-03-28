package com.travelonna.demo.domain.group.controller;

import com.travelonna.demo.domain.group.dto.PlanUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 세션에서 유저 정보 및 그룹 URL 가져오기
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");
        String groupUrl = (String) headerAccessor.getSessionAttributes().get("groupUrl");
        
        if (username != null && groupUrl != null) {
            log.info("User Disconnected: {}", username);
            
            // 연결 끊김 메시지 생성
            PlanUpdateMessage message = PlanUpdateMessage.builder()
                    .type("LEAVE")
                    .userName(username)
                    .userId(userId)
                    .groupUrl(groupUrl)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            // 해당 그룹에 메시지 전송
            messagingTemplate.convertAndSend("/topic/plan/" + groupUrl, message);
        }
    }
} 