package com.travelonna.demo.domain.plan.controller;

import java.util.List;

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

import com.travelonna.demo.domain.plan.dto.PlaceRequestDto.AddPlaceDto;
import com.travelonna.demo.domain.plan.dto.PlaceRequestDto.CreatePlaceDto;
import com.travelonna.demo.domain.plan.dto.PlaceRequestDto.UpdatePlaceDto;
import com.travelonna.demo.domain.plan.dto.PlaceResponseDto;
import com.travelonna.demo.domain.plan.service.PlaceService;
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
@RequestMapping("/api/v1/plans/{planId}/places")
@RequiredArgsConstructor
@Tag(name = "여행 장소", description = "여행 장소 관리 API")
public class PlaceController {
    
    private final PlaceService placeService;
    
    @Operation(summary = "여행 장소 생성", description = "새로운 여행 장소를 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "장소 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PlaceResponseDto>> createPlace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "일정 ID") @PathVariable Integer planId,
            @Parameter(description = "생성할 장소 정보") @RequestBody CreatePlaceDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("여행 장소 생성 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        PlaceResponseDto responseDto = placeService.createPlace(userId, planId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("여행 장소가 생성되었습니다.", responseDto));
    }
    
    @Operation(summary = "일정 생성 후 장소 추가", description = "일정 생성 후 여행 장소를 추가합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "장소 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<PlaceResponseDto>> addPlace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "일정 ID") @PathVariable Integer planId,
            @Parameter(description = "추가할 장소 정보") @RequestBody AddPlaceDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("일정 생성 후 장소 추가 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        PlaceResponseDto responseDto = placeService.addPlace(userId, planId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("여행 장소가 추가되었습니다.", responseDto));
    }
    
    @Operation(summary = "여행 장소 수정", description = "기존 여행 장소 정보를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "장소 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소 또는 일정을 찾을 수 없음")
    })
    @PutMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponseDto>> updatePlace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "일정 ID") @PathVariable Integer planId,
            @Parameter(description = "장소 ID") @PathVariable Integer placeId,
            @Parameter(description = "수정할 장소 정보") @RequestBody UpdatePlaceDto requestDto) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("여행 장소 수정 요청: 사용자 ID {}, 일정 ID {}, 장소 ID {}", userId, planId, placeId);
        
        PlaceResponseDto responseDto = placeService.updatePlace(userId, planId, placeId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("여행 장소가 수정되었습니다.", responseDto));
    }
    
    @Operation(summary = "여행 장소 삭제", description = "여행 장소를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "장소 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소 또는 일정을 찾을 수 없음")
    })
    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "일정 ID") @PathVariable Integer planId,
            @Parameter(description = "장소 ID") @PathVariable Integer placeId) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("여행 장소 삭제 요청: 사용자 ID {}, 일정 ID {}, 장소 ID {}", userId, planId, placeId);
        
        placeService.deletePlace(userId, planId, placeId);
        
        return ResponseEntity.ok(ApiResponse.success("여행 장소가 삭제되었습니다.", null));
    }
    
    @Operation(summary = "여행 장소 목록 조회", description = "일정에 등록된 모든 여행 장소를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "장소 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @GetMapping("/view")
    public ResponseEntity<ApiResponse<List<PlaceResponseDto>>> getPlaces(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "일정 ID") @PathVariable Integer planId) {
        
        JwtUserDetails jwtUserDetails = (JwtUserDetails) userDetails;
        int userId = jwtUserDetails.getUserId();
        log.info("여행 장소 목록 조회 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        List<PlaceResponseDto> places = placeService.getPlacesByPlanId(userId, planId);
        
        return ResponseEntity.ok(ApiResponse.success("여행 장소 목록 조회 성공", places));
    }
} 