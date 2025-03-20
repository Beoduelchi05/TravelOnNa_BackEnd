package com.travelonna.demo.domain.plan.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Transient;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer planId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_info")
    private TransportInfo transportInfo;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "group_id")
    private Integer groupId;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    
    @Column(name = "total_cost")
    private Integer totalCost;
    
    @Column(name = "memo")
    private String memo;
    
    
    @Transient
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Transient
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (isPublic == null) {
            isPublic = Boolean.FALSE;
        }
    }
    
    // 일정 기간 수정
    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // 여행지 설정
    public void updateLocation(String location) {
        this.location = location;
    }
    
    // 이동수단 설정
    public void updateTransport(TransportInfo transportInfo) {
        this.transportInfo = transportInfo;
    }
    
    // 총 비용 설정
    public void updateTotalCost(Integer totalCost) {
        this.totalCost = totalCost;
    }
    
    // 제목 설정
    public void updateTitle(String title) {
        this.title = title;
    }
    
    // 공개 여부 설정
    public void updateIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    // 메모 설정
    public void updateMemo(String memo) {
        this.memo = memo;
    }
    
    // 그룹 ID 설정
    public void updateGroupId(Integer groupId) {
        this.groupId = groupId;
    }
} 