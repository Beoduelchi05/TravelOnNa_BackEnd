package com.travelonna.demo.domain.plan.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.plan.repository.MyMapRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/mymap")
@RequiredArgsConstructor
@Tag(name = "MyMap", description = "지역별 로그 매핑 관리 API")
public class MyMapController {
    
    private final MyMapRepository myMapRepository;
    
    // 기존 MyMap 관련 API들을 모두 제거했습니다.
    // MyMap 데이터는 LogService에서 내부적으로만 사용됩니다.
} 