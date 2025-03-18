package com.travelonna.demo.domain.group.repository;

import com.travelonna.demo.domain.group.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    Optional<GroupEntity> findByUrl(String url);
    boolean existsByUrl(String url);
} 