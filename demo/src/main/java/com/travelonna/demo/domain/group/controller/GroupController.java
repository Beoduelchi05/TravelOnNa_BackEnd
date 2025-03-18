package com.travelonna.demo.domain.group.controller;

import com.travelonna.demo.domain.group.dto.GroupRequestDto;
import com.travelonna.demo.domain.group.dto.GroupResponseDto;
import com.travelonna.demo.domain.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(
            @RequestAttribute("userId") Integer userId,
            @RequestBody GroupRequestDto requestDto) {
        GroupResponseDto responseDto = groupService.createGroup(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{url}")
    public ResponseEntity<GroupResponseDto> getGroupByUrl(@PathVariable String url) {
        GroupResponseDto responseDto = groupService.findGroupByUrl(url);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/join/{url}")
    public ResponseEntity<Void> joinGroup(
            @RequestAttribute("userId") Integer userId,
            @PathVariable String url) {
        groupService.joinGroup(userId, url);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupResponseDto>> getMyGroups(
            @RequestAttribute("userId") Integer userId) {
        List<GroupResponseDto> groups = groupService.getMyGroups(userId);
        return ResponseEntity.ok(groups);
    }
} 