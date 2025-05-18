package com.travelonna.demo.domain.group.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.group.dto.GroupRequestDto;
import com.travelonna.demo.domain.group.dto.GroupResponseDto;
import com.travelonna.demo.domain.group.entity.GroupEntity;
import com.travelonna.demo.domain.group.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "그룹", description = "그룹 관리 API (인증 필요)")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "그룹 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(
            @Parameter(description = "인증된 사용자 ID", example = "1") @RequestAttribute("userId") Integer userId,
            @Parameter(description = "생성할 그룹 정보", example = "{\n  \"name\": \"서울 여행 모임\",\n  \"description\": \"서울 여행을 함께 계획할 그룹입니다.\",\n  \"isPublic\": true\n}") @RequestBody GroupRequestDto requestDto) {
        GroupResponseDto responseDto = groupService.createGroup(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Plan ID로 그룹 조회", description = "Plan ID를 사용하여 그룹 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "그룹 조회 성공"),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/plan/{planId}")
    public ResponseEntity<GroupResponseDto> getGroupByPlanId(
            @Parameter(description = "조회할 Plan의 ID", example = "1") @PathVariable Integer planId) {
        GroupResponseDto responseDto = groupService.findGroupByPlanId(planId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "그룹 참여", description = "URL을 통해 그룹에 참여합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "그룹 참여 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @PostMapping("/join/{url}")
    public ResponseEntity<Void> joinGroup(
            @Parameter(description = "인증된 사용자 ID", example = "1") @RequestAttribute("userId") Integer userId,
            @Parameter(description = "참여할 그룹의 URL", example = "travel-group") @PathVariable String url) {
        log.info("Joining group with URL: {}, user ID: {}", url, userId);
        try {
            groupService.joinGroup(userId, url);
            log.info("Successfully joined group with URL: {}", url);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error joining group: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "내 그룹 목록 조회", description = "현재 사용자가 속한, 혹은 생성한 그룹 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public ResponseEntity<List<GroupResponseDto>> getMyGroups(
            @Parameter(description = "인증된 사용자 ID", example = "1") @RequestAttribute("userId") Integer userId) {
        List<GroupResponseDto> groups = groupService.getMyGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "그룹의 플랜 ID 목록 조회", description = "그룹 ID를 사용하여 해당 그룹에 속한 플랜 ID 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "플랜 ID 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroupDetails(
            @Parameter(description = "조회할 그룹의 ID", example = "1") @PathVariable Integer groupId) {
        // 그룹 정보 조회
        GroupEntity group = groupService.findGroupById(groupId);
        GroupResponseDto responseDto = GroupResponseDto.fromEntity(group);
        
        // 그룹에 속한 플랜 ID 목록 조회 및 설정
        List<Integer> planIds = groupService.getPlansForGroup(groupId);
        responseDto.setPlanIds(planIds);
        
        return ResponseEntity.ok(responseDto);
    }
} 