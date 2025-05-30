package com.travelonna.demo.domain.recommendation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.travelonna.demo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user"})
public class Recommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Integer recommendationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "item_id", nullable = false)
    private Integer itemId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    
    @Column(name = "score", nullable = false)
    private Float score;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public Recommendation(User user, Integer itemId, ItemType itemType, Float score) {
        this.user = user;
        this.itemId = itemId;
        this.itemType = itemType;
        this.score = score;
    }
    
    public enum ItemType {
        LOG("log"),
        PLACE("place"), 
        PLAN("plan");
        
        private final String value;
        
        ItemType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
} 