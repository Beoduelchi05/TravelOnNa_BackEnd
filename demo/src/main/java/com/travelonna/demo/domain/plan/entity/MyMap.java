package com.travelonna.demo.domain.plan.entity;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "my_map")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyMap {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mymap_id")
    private Integer mymapId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id")
    private Log log;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_code")
    private MapCode mapCode;
    
    // 편의 메서드
    public void setAssociations(Log log, User user, MapCode mapCode) {
        this.log = log;
        this.user = user;
        this.mapCode = mapCode;
    }
} 