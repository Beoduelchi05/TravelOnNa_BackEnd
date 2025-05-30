package com.travelonna.demo.domain.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Integer actionId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @CreationTimestamp
    @Column(name = "action_time", nullable = false, updatable = false)
    private LocalDateTime actionTime;

    // 액션 타입 enum
    public enum ActionType {
        POST,    // 게시물 작성
        LIKE,    // 좋아요
        COMMENT, // 댓글 작성
        VIEW     // 조회
    }

    // 대상 타입 enum
    public enum TargetType {
        LOG,   // 여행 기록
        PLACE, // 장소
        PLAN   // 여행 계획
    }
} 