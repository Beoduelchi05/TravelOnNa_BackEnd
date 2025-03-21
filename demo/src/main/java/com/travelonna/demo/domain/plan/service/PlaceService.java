package com.travelonna.demo.domain.plan.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.plan.dto.PlaceRequestDto.AddPlaceDto;
import com.travelonna.demo.domain.plan.dto.PlaceRequestDto.CreatePlaceDto;
import com.travelonna.demo.domain.plan.dto.PlaceRequestDto.UpdatePlaceDto;
import com.travelonna.demo.domain.plan.dto.PlaceResponseDto;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.plan.repository.PlaceRepository;
import com.travelonna.demo.domain.plan.repository.PlanRepository;
import com.travelonna.demo.global.exception.BusinessException;
import com.travelonna.demo.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {
    
    private final PlaceRepository placeRepository;
    private final PlanRepository planRepository;
    
    /**
     * 여행 장소 생성
     */
    @Transactional
    public PlaceResponseDto createPlace(Integer userId, Integer planId, CreatePlaceDto requestDto) {
        log.info("여행 장소 생성 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        // 일정 존재 여부 확인 및 권한 체크
        Plan plan = planRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 순서 설정 (현재 최대 순서 + 1)
        Integer maxOrder = placeRepository.findMaxOrderByPlanId(planId);
        int newOrder = (maxOrder != null) ? maxOrder + 1 : 1;
        
        // 장소 저장
        Place place = Place.builder()
                .plan(plan)
                .place(requestDto.getPlace())
                .isPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : false)
                .visitDate(requestDto.getVisitDate() != null ? 
                        LocalDateTime.of(requestDto.getVisitDate(), java.time.LocalTime.MIDNIGHT) : null)
                .placeCost(requestDto.getPlaceCost())
                .memo(requestDto.getMemo())
                .lat(requestDto.getLat())
                .lon(requestDto.getLon())
                .name(requestDto.getPlace()) // 주소를 기본 이름으로 사용
                .order(newOrder)
                .build();
        
        Place savedPlace = placeRepository.save(place);
        log.info("여행 장소 생성 완료: 장소 ID {}", savedPlace.getPlaceId());
        
        return PlaceResponseDto.fromEntityWithDay(savedPlace);
    }
    
    /**
     * 일정 생성 후 장소 추가
     */
    @Transactional
    public PlaceResponseDto addPlace(Integer userId, Integer planId, AddPlaceDto requestDto) {
        log.info("일정 생성 후 장소 추가 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        // 일정 존재 여부 확인 및 권한 체크
        Plan plan = planRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 방문 날짜 계산 (일차(dayNumber)를 이용하여 계산)
        LocalDateTime visitDate = null;
        if (requestDto.getDayNumber() != null && plan.getStartDate() != null) {
            // 시작일 + (일차 - 1)
            visitDate = plan.getStartDate().plusDays(requestDto.getDayNumber() - 1)
                .atStartOfDay(); // LocalDate를 LocalDateTime으로 변환
        }
        
        // 장소 저장
        Place place = Place.builder()
                .plan(plan)
                .place(requestDto.getAddress())
                .isPublic(false) // 기본값
                .name(requestDto.getName())
                .order(requestDto.getOrder())
                .visitDate(visitDate) // 계산된 방문 날짜 설정
                .build();
        
        Place savedPlace = placeRepository.save(place);
        log.info("장소 추가 완료: 장소 ID {}", savedPlace.getPlaceId());
        
        return PlaceResponseDto.fromEntityWithDay(savedPlace);
    }
    
    /**
     * 여행 장소 수정
     */
    @Transactional
    public PlaceResponseDto updatePlace(Integer userId, Integer planId, Integer placeId, UpdatePlaceDto requestDto) {
        log.info("여행 장소 수정 요청: 사용자 ID {}, 일정 ID {}, 장소 ID {}", userId, planId, placeId);
        
        // 일정 존재 여부 확인 및 권한 체크
        Plan plan = planRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 장소 존재 여부 확인
        Place place = placeRepository.findByPlaceIdAndPlan_PlanId(placeId, planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        
        // 장소 정보 업데이트
        if (requestDto.getPlace() != null) place.setPlace(requestDto.getPlace());
        if (requestDto.getIsPublic() != null) place.setIsPublic(requestDto.getIsPublic());
        if (requestDto.getVisitDate() != null) {
            place.setVisitDate(LocalDateTime.of(requestDto.getVisitDate(), java.time.LocalTime.MIDNIGHT));
        }
        if (requestDto.getPlaceCost() != null) place.setPlaceCost(requestDto.getPlaceCost());
        if (requestDto.getMemo() != null) place.setMemo(requestDto.getMemo());
        if (requestDto.getLat() != null) place.setLat(requestDto.getLat());
        if (requestDto.getLon() != null) place.setLon(requestDto.getLon());
        if (requestDto.getName() != null) place.setName(requestDto.getName());
        if (requestDto.getOrder() != null) place.setOrder(requestDto.getOrder());
        
        Place updatedPlace = placeRepository.save(place);
        log.info("여행 장소 수정 완료: 장소 ID {}", updatedPlace.getPlaceId());
        
        return PlaceResponseDto.fromEntityWithDay(updatedPlace);
    }
    
    /**
     * 여행 장소 삭제
     */
    @Transactional
    public void deletePlace(Integer userId, Integer planId, Integer placeId) {
        log.info("여행 장소 삭제 요청: 사용자 ID {}, 일정 ID {}, 장소 ID {}", userId, planId, placeId);
        
        // 일정 존재 여부 확인 및 권한 체크
        planRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 장소 존재 여부 확인
        Place place = placeRepository.findByPlaceIdAndPlan_PlanId(placeId, planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        
        // 장소 삭제
        placeRepository.delete(place);
        log.info("여행 장소 삭제 완료: 장소 ID {}", placeId);
    }
    
    /**
     * 여행 장소 목록 조회
     */
    public List<PlaceResponseDto> getPlacesByPlanId(Integer userId, Integer planId) {
        log.info("여행 장소 목록 조회 요청: 사용자 ID {}, 일정 ID {}", userId, planId);
        
        // 일정 존재 여부 확인 및 권한 체크
        planRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 장소 목록 조회
        List<Place> places = placeRepository.findByPlanIdOrderByOrder(planId);
        
        // 장소 응답 DTO 생성 (visitDate로부터 여행 일차 계산)
        List<PlaceResponseDto> placeDtos = places.stream()
                .map(PlaceResponseDto::fromEntityWithDay)
                .collect(Collectors.toList());
        
        log.info("여행 장소 목록 조회 완료: 장소 수 {}", placeDtos.size());
        
        return placeDtos;
    }
} 