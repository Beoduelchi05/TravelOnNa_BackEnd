package com.travelonna.demo.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profile", uniqueConstraints = {
    @UniqueConstraint(name = "uk_profile_nickname", columnNames = {"nickname"})
})
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id") 
    private Integer profileId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(nullable = false, unique = true) // unique 속성 추가
    private String nickname;
    
    @Column(name = "profile_image")
    private String profileImage;
    
    private String introduction;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Builder
    public Profile(Integer userId, String nickname, String profileImage, String introduction) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.introduction = introduction;
    }
    
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}