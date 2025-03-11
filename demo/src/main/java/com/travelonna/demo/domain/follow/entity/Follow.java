package com.travelonna.demo.domain.follow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follow")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_id")
    private Integer id;

    @Column(name = "to_user")
    private Integer toUser;

    @Column(name = "from_user")
    private Integer fromUser;

    @Column(name = "profile_id")
    private Integer profileId;
} 