package com.travelonna.demo.domain.plan.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelonna.demo.domain.plan.dto.PlanRequestDto.CreatePlanDto;
import com.travelonna.demo.domain.plan.dto.PlanRequestDto.SearchTransportationDto;
import com.travelonna.demo.domain.plan.dto.PlanRequestDto.UpdateCostDto;
import com.travelonna.demo.domain.plan.dto.PlanRequestDto.UpdateLocationDto;
import com.travelonna.demo.domain.plan.dto.PlanRequestDto.UpdatePeriodDto;
import com.travelonna.demo.domain.plan.dto.PlanRequestDto.UpdatePlanDto;
import com.travelonna.demo.domain.plan.dto.PlanRequestDto.UpdateTransportDto;
import com.travelonna.demo.domain.plan.dto.PlanResponseDto;
import com.travelonna.demo.domain.plan.service.PlanService;
import com.travelonna.demo.global.api.odsay.ODSayTransportService;
import com.travelonna.demo.global.api.odsay.dto.TransportationResponseDto;
import com.travelonna.demo.global.common.ApiResponse;
import com.travelonna.demo.global.security.jwt.JwtUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "개인 일정", description = "개인 일정 관리 API")
public class PlanController {
    
    private final PlanService planService;
    private final ODSayTransportService oDSayTransportService;
    
    @Operation(summary = "개인 일정 생성", description = "새로운 개인 일정을 생성합니다. 기간, 여행지, 이동수단을 함께 설정할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "일정 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PlanResponseDto>> createPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "생성할 일정 정보") @RequestBody CreatePlanDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("개인 일정 생성 요청: 사용자 ID {}", userId);
        
        PlanResponseDto responseDto = planService.createPlan(userId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("여행 일정이 생성되었습니다.", responseDto));
    }
    
    @Operation(summary = "기간 설정", description = "개인 일정의 기간을 설정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기간 설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/{planId}/period")
    public ResponseEntity<ApiResponse<PlanResponseDto>> updatePeriod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "수정할 일정 ID") @PathVariable Integer planId,
            @Parameter(description = "수정할 기간 정보") @RequestBody UpdatePeriodDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 기간 설정 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        PlanResponseDto responseDto = planService.updatePeriod(userId, planId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("여행 기간이 수정되었습니다.", responseDto));
    }
    
    @Operation(summary = "여행지 설정", description = "개인 일정의 여행지를 설정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행지 설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/{planId}/location")
    public ResponseEntity<ApiResponse<PlanResponseDto>> updateLocation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "수정할 일정 ID") @PathVariable Integer planId,
            @Parameter(description = "수정할 여행지 정보") @RequestBody UpdateLocationDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 여행지 설정 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        PlanResponseDto responseDto = planService.updateLocation(userId, planId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("여행지가 수정되었습니다.", responseDto));
    }
    
    @Operation(summary = "이동수단 설정", description = "개인 일정의 이동수단을 설정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이동수단 설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/{planId}/transport")
    public ResponseEntity<ApiResponse<PlanResponseDto>> updateTransport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "수정할 일정 ID") @PathVariable Integer planId,
            @Parameter(description = "수정할 이동수단 정보") @RequestBody UpdateTransportDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 이동수단 설정 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        PlanResponseDto responseDto = planService.updateTransport(userId, planId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("이동수단이 수정되었습니다.", responseDto));
    }
    
    @Operation(summary = "일정 수정", description = "개인 일정 정보를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/{planId}")
    public ResponseEntity<ApiResponse<PlanResponseDto>> updatePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "수정할 일정 ID") @PathVariable Integer planId,
            @Parameter(description = "수정할 일정 정보") @RequestBody UpdatePlanDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 수정 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        PlanResponseDto responseDto = planService.updatePlan(userId, planId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("여행 일정이 수정되었습니다.", responseDto));
    }
    
    @Operation(summary = "일정 삭제", description = "개인 일정을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "삭제할 일정 ID") @PathVariable Integer planId) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 삭제 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        planService.deletePlan(userId, planId);
        
        return ResponseEntity.ok(ApiResponse.success("여행 일정이 삭제되었습니다.", null));
    }
    
    @Operation(summary = "일정 비용 업데이트", description = "개인 일정의 총 비용을 설정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비용 업데이트 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/{planId}/cost")
    public ResponseEntity<ApiResponse<PlanResponseDto>> updatePlanCost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "비용을 업데이트할 일정 ID") @PathVariable Integer planId,
            @Parameter(description = "업데이트할 비용 정보") @RequestBody UpdateCostDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 비용 업데이트 요청: 사용자 ID {}, 일정 ID {}, 비용 {}", userId, planId, requestDto.getTotalCost());
        
        PlanResponseDto responseDto = planService.updatePlanCost(userId, planId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("여행 비용이 업데이트되었습니다.", responseDto));
    }
    
    @Operation(summary = "일정 비용 조회", description = "개인 일정의 총 비용을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비용 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @GetMapping("/{planId}/cost")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getPlanCost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "비용을 조회할 일정 ID") @PathVariable Integer planId) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 비용 조회 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        Integer totalCost = planService.getPlanTotalCost(userId, planId);
        
        return ResponseEntity.ok(ApiResponse.success("여행 비용 조회 성공", Map.of("totalCost", totalCost)));
    }
    
    @Operation(summary = "내 일정 목록 조회", description = "사용자의 모든 개인 일정을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanResponseDto>>> getMyPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("사용자 개인 일정 목록 조회 요청: 사용자 ID {}", userId);
        
        List<PlanResponseDto> plans = planService.getUserPlans(userId);
        
        return ResponseEntity.ok(ApiResponse.success("내 여행 일정 조회 성공", plans));
    }
    
    @Operation(summary = "교통편 검색", description = "일정 기반의 교통편 정보를 검색합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "교통편 검색 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/transportation/search")
    public ResponseEntity<ApiResponse<TransportationResponseDto>> searchTransportation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "교통편 검색 정보") @RequestBody SearchTransportationDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("교통편 검색 요청: 사용자 ID {}, 출발지 {}, 도착지 {}, 날짜 {}", 
                userId, requestDto.getSource(), requestDto.getDestination(), requestDto.getDepartureDate());
        
        TransportationResponseDto responseDto = oDSayTransportService.searchTransportation(
                requestDto.getSource(), 
                requestDto.getDestination(), 
                requestDto.getDepartureDate(), 
                requestDto.getTransportType());
        
        return ResponseEntity.ok(ApiResponse.success("교통편 검색 성공", responseDto));
    }
}