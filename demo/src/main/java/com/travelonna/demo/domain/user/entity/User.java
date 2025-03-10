package com.travelonna.demo.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.security.AuthProvider;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    //고유 식별자를 저장하기 위해서
    private String providerId;

    //User와 Profile은 1:1 관계설정 하나의 User당 하나의 Profile이 존재
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    public void setName(String name) {
        this.name = name;
    }
} 
