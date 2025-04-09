package com.travelonna.demo.domain.log.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    
    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LogImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LogComment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Likes> likes = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (isPublic == null) {
            isPublic = Boolean.FALSE;
        }
    }
    
    // 이미지 추가
    public void addImage(LogImage image) {
        images.add(image);
        image.setLog(this);
    }
    
    // 댓글 추가
    public void addComment(LogComment comment) {
        comments.add(comment);
        comment.setLog(this);
    }
    
    // 좋아요 추가
    public int getLikesCount() {
        return likes.size();
    }
    
    // 공개 여부 변경
    public void updateIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    // 내용 수정
    public void updateComment(String comment) {
        this.comment = comment;
    }
} 