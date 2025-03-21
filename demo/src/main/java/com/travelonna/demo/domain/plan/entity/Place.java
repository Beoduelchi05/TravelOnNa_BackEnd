package com.travelonna.demo.domain.plan.entity;

import java.time.LocalDateTime;

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
@Table(name = "place")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Integer placeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    
    @Column(name = "place", nullable = false)
    private String place;
    
    @Column(name = "is_public")
    private Boolean isPublic;
    
    @Column(name = "visit_date")
    private LocalDateTime visitDate;
    
    @Column(name = "place_cost")
    private Integer placeCost;
    
    @Column(name = "memo")
    private String memo;
    
    @Column(name = "lat")
    private String lat;
    
    @Column(name = "lon")
    private String lon;
    
    @Column(name = "p_name", nullable = false)
    private String name;
    
    @Column(name = "p_order", nullable = false)
    private Integer order;
} 