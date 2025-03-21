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
@Tag(name = "그룹", description = "그룹 관리 API")
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
            @Parameter(description = "인증된 사용자 ID") @RequestAttribute("userId") Integer userId,
            @Parameter(description = "생성할 그룹 정보") @RequestBody GroupRequestDto requestDto) {
        GroupResponseDto responseDto = groupService.createGroup(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "URL로 그룹 조회", description = "그룹 URL을 사용하여 그룹 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "그룹 조회 성공"),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{url}")
    public ResponseEntity<GroupResponseDto> getGroupByUrl(
            @Parameter(description = "조회할 그룹의 URL", example = "travel-group") @PathVariable String url) {
        GroupResponseDto responseDto = groupService.findGroupByUrl(url);
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
            @Parameter(description = "인증된 사용자 ID") @RequestAttribute("userId") Integer userId,
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
            @Parameter(description = "인증된 사용자 ID") @RequestAttribute("userId") Integer userId) {
        List<GroupResponseDto> groups = groupService.getMyGroups(userId);
        return ResponseEntity.ok(groups);
    }
} 