package com.travelonna.demo.domain.group.entity;

import com.travelonna.demo.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_group")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer id;

    @Column(name = "url", unique = true)
    private String url;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "date")
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host")
    private User host;

    public void generateUniqueUrl() {
        this.url = UUID.randomUUID().toString().substring(0, 8);
    }
} 