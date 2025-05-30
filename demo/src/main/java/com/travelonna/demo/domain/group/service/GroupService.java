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
import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.plan.repository.PlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    @Transactional
    public GroupResponseDto createGroup(Integer userId, GroupRequestDto requestDto) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GroupEntity group = GroupEntity.builder()
                .isGroup(requestDto.getIsGroup())
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

    @Transactional(readOnly = true)
    public GroupResponseDto findGroupByPlanId(Integer planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with ID: " + planId));
        
        Integer groupId = plan.getGroupId();
        if (groupId == null) {
            throw new IllegalArgumentException("This plan is not associated with any group");
        }
        
        GroupEntity group = groupRepository.findById(groupId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        
        return GroupResponseDto.fromEntity(group);
    }

    @Transactional
    public void joinGroup(Integer userId, String groupUrl) {
        log.debug("Attempting to join group with URL: {} for user ID: {}", groupUrl, userId);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            log.debug("Found user: {}", user.getEmail());
            
            GroupEntity group = groupRepository.findByUrl(groupUrl)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found with URL: " + groupUrl));
            log.debug("Found group with ID: {}", group.getId());
            
            // 이미 가입된 멤버인지 확인
            boolean exists = groupMemberRepository.existsByGroupAndUser(group, user);
            log.debug("User is already a member: {}", exists);
            
            if (exists) {
                log.info("User {} is already a member of group {}, returning success", userId, groupUrl);
                return; // 이미 멤버인 경우 성공으로 처리하고 종료
            }
            
            GroupMember member = GroupMember.builder()
                    .group(group)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            
            groupMemberRepository.save(member);
            log.debug("Successfully added user to group");
        } catch (Exception e) {
            log.error("Error joining group: {}", e.getMessage(), e);
            throw e; // 예외를 다시 던져서 원래 처리 흐름을 유지
        }
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getMyGroups(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<GroupEntity> groups = groupRepository.findAll().stream()
                .filter(group -> groupMemberRepository.existsByGroupAndUser(group, user))
                .collect(Collectors.toList());
        
        List<GroupResponseDto> responseDtos = groups.stream()
                .map(GroupResponseDto::fromEntity)
                .collect(Collectors.toList());
        
        // 각 그룹에 대한 planIds 설정
        for (GroupResponseDto dto : responseDtos) {
            List<Integer> planIds = getPlansForGroup(dto.getId());
            dto.setPlanIds(planIds);
            log.debug("Setting planIds for group {}: {}", dto.getId(), planIds);
        }
        
        return responseDtos;
    }

    @Transactional(readOnly = true)
    public List<Integer> getPlansForGroup(Integer groupId) {
        GroupEntity group = groupRepository.findById(groupId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        
        log.debug("Finding plans for group ID: {}", groupId);
        List<Plan> plans = planRepository.findByGroupId(groupId);
        log.debug("Found {} plans for group ID: {}", plans.size(), groupId);
        
        List<Integer> planIds = plans.stream()
                .map(Plan::getPlanId)
                .collect(Collectors.toList());
        
        log.debug("Plan IDs for group {}: {}", groupId, planIds);
        return planIds;
    }

    @Transactional(readOnly = true)
    public GroupEntity findGroupById(Integer groupId) {
        return groupRepository.findById(groupId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
    }

    /**
     * 사용자가 특정 그룹의 멤버인지 확인합니다.
     * @param userId 확인할 사용자 ID
     * @param groupUrl 확인할 그룹 URL
     * @return 멤버 여부
     */
    @Transactional(readOnly = true)
    public boolean isUserMemberOfGroup(Integer userId, String groupUrl) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            GroupEntity group = groupRepository.findByUrl(groupUrl)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found with URL: " + groupUrl));
            
            boolean isMember = groupMemberRepository.existsByGroupAndUser(group, user);
            log.debug("User {} membership status for group {}: {}", userId, groupUrl, isMember);
            return isMember;
        } catch (Exception e) {
            log.error("Error checking group membership: {}", e.getMessage(), e);
            return false;
        }
    }
} 