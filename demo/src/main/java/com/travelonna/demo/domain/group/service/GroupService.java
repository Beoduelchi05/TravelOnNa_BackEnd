package com.travelonna.demo.domain.group.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.group.dto.GroupRequestDto;
import com.travelonna.demo.domain.group.dto.GroupResponseDto;
import com.travelonna.demo.domain.group.entity.GroupEntity;
import com.travelonna.demo.domain.group.entity.GroupMember;
import com.travelonna.demo.domain.group.repository.GroupMemberRepository;
import com.travelonna.demo.domain.group.repository.GroupRepository;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponseDto createGroup(Integer userId, GroupRequestDto requestDto) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GroupEntity group = GroupEntity.builder()
                .isPublic(requestDto.getIsPublic())
                .createdDate(LocalDateTime.now())
                .host(host)
                .build();

        // URL 생성
        generateUniqueUrl(group);

        GroupEntity savedGroup = groupRepository.save(group);

        // 그룹 생성자를 멤버로 추가
        GroupMember hostMember = GroupMember.builder()
                .group(savedGroup)
                .user(host)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        groupMemberRepository.save(hostMember);

        return GroupResponseDto.fromEntity(savedGroup);
    }

    private void generateUniqueUrl(GroupEntity group) {
        group.generateUniqueUrl();
        
        // URL 중복 체크 및 재생성
        while (groupRepository.existsByUrl(group.getUrl())) {
            group.generateUniqueUrl();
        }
    }

    @Transactional(readOnly = true)
    public GroupResponseDto findGroupByUrl(String url) {
        GroupEntity group = groupRepository.findByUrl(url)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with URL: " + url));
        
        return GroupResponseDto.fromEntity(group);
    }

    @Transactional
    public void joinGroup(Integer userId, String groupUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        GroupEntity group = groupRepository.findByUrl(groupUrl)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with URL: " + groupUrl));
        
        // 이미 가입된 멤버인지 확인
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("User is already a member of this group");
        }
        
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        
        groupMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getMyGroups(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<GroupEntity> groups = groupRepository.findAll().stream()
                .filter(group -> groupMemberRepository.existsByGroupAndUser(group, user))
                .collect(Collectors.toList());
        
        return groups.stream()
                .map(GroupResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
} 