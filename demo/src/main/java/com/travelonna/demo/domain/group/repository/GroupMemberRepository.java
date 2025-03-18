package com.travelonna.demo.domain.group.repository;

import com.travelonna.demo.domain.group.entity.GroupEntity;
import com.travelonna.demo.domain.group.entity.GroupMember;
import com.travelonna.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupAndIsActiveTrue(GroupEntity group);
    Optional<GroupMember> findByGroupAndUser(GroupEntity group, User user);
    boolean existsByGroupAndUser(GroupEntity group, User user);
} 