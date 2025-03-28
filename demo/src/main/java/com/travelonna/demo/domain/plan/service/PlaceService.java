package com.travelonna.demo.domain.plan.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final PlanService planService;
    
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
                .googleId(requestDto.getGoogleId())
                .build();
        
        Place savedPlace = placeRepository.save(place);
        log.info("여행 장소 생성 완료: 장소 ID {}", savedPlace.getPlaceId());
        
        // 일정 총 비용 업데이트
        updatePlanTotalCost(planId);
        
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
        
        // 해당 일차의 최대 순서 조회하여 그 다음 순서로 추가
        Integer maxOrderForDay = null;
        
        if (requestDto.getDayNumber() != null) {
            // 해당 일차의 최대 순서 조회
            maxOrderForDay = placeRepository.findMaxOrderByPlanIdAndDay(planId, requestDto.getDayNumber());
        } else {
            // 일차 정보가 없는 경우 전체 장소 중 최대 순서 사용
            maxOrderForDay = placeRepository.findMaxOrderByPlanId(planId);
        }
        
        // 순서 결정: 명시적으로 순서가 지정된 경우 해당 값 사용, 아니면 자동 계산
        Integer newOrder;
        if (requestDto.getOrder() != null) {
            newOrder = requestDto.getOrder();
        } else {
            newOrder = (maxOrderForDay != null) ? maxOrderForDay + 1 : 1;
        }
        
        // 장소 저장
        Place place = Place.builder()
                .plan(plan)
                .place(requestDto.getAddress())
                .isPublic(false) // 기본값
                .name(requestDto.getName())
                .order(newOrder)
                .visitDate(visitDate) // 계산된 방문 날짜 설정
                .googleId(requestDto.getGoogleId())
                .build();
        
        Place savedPlace = placeRepository.save(place);
        log.info("장소 추가 완료: 장소 ID {}, 일차: {}, 순서: {}", 
                savedPlace.getPlaceId(), requestDto.getDayNumber(), newOrder);
        
        // 일정 총 비용 업데이트
        updatePlanTotalCost(planId);
        
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
        if (requestDto.getGoogleId() != null) place.setGoogleId(requestDto.getGoogleId());
        Place updatedPlace = placeRepository.save(place);
        log.info("여행 장소 수정 완료: 장소 ID {}", updatedPlace.getPlaceId());
        
        // 일정 총 비용 업데이트
        updatePlanTotalCost(planId);
        
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
        
        // 일정 총 비용 업데이트
        updatePlanTotalCost(planId);
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
    
    /**
     * 장소 순서 일괄 업데이트
     * 
     * @param userId 사용자 ID
     * @param planId 일정 ID
     * @param dayNumber 일차
     * @param placeIds 순서대로 정렬된 장소 ID 목록
     * @return 업데이트된 장소 목록
     */
    @Transactional
    public List<PlaceResponseDto> updatePlacesOrder(Integer userId, Integer planId, 
                                                  Integer dayNumber, List<Integer> placeIds) {
        log.info("장소 순서 일괄 업데이트 요청: 사용자 ID {}, 일정 ID {}, 일차 {}, 장소 수 {}", 
                userId, planId, dayNumber, placeIds.size());
        
        // 일정 존재 여부 확인 및 권한 체크
        Plan plan = planRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 해당 일차의 모든 장소 조회
        List<Place> places;
        if (dayNumber != null) {
            places = placeRepository.findByPlanIdAndDayOrderByOrder(planId, dayNumber);
        } else {
            places = placeRepository.findByPlanIdOrderByOrder(planId);
        }
        
        // 장소 ID -> 장소 엔티티 매핑
        Map<Integer, Place> placeMap = places.stream()
                .collect(Collectors.toMap(Place::getPlaceId, place -> place));
        
        // 업데이트할 장소 목록
        List<Place> updatedPlaces = new ArrayList<>();
        
        // 순서 업데이트
        for (int i = 0; i < placeIds.size(); i++) {
            Integer placeId = placeIds.get(i);
            Place place = placeMap.get(placeId);
            
            if (place != null) {
                place.setOrder(i + 1); // 1부터 순차적으로 순서 부여
                updatedPlaces.add(place);
            } else {
                log.warn("장소 ID {}가 해당 일정에 존재하지 않습니다.", placeId);
            }
        }
        
        // 변경된 장소 저장
        List<Place> savedPlaces = placeRepository.saveAll(updatedPlaces);
        log.info("장소 순서 일괄 업데이트 완료: 업데이트된 장소 수 {}", savedPlaces.size());
        
        // 응답 DTO 생성
        return savedPlaces.stream()
                .map(PlaceResponseDto::fromEntityWithDay)
                .collect(Collectors.toList());
    }
    
    /**
     * 일정 총 비용 자동 계산 및 업데이트 
     * (일정에 장소가 추가/수정/삭제될 때마다 자동으로 호출)
     * 
     * @param planId 일정 ID
     */
    private void updatePlanTotalCost(Integer planId) {
        log.info("일정 총 비용 자동 계산: 일정 ID {}", planId);
        
        // 해당 일정의 모든 장소 비용 합산
        List<Place> places = placeRepository.findByPlanIdOrderByOrder(planId);
        
        Integer totalCost = places.stream()
                .filter(place -> place.getPlaceCost() != null)
                .mapToInt(Place::getPlaceCost)
                .sum();
        
        // 계산된 총 비용으로 일정 업데이트
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
        
        // 일정 비용 직접 업데이트
        plan.updateTotalCost(totalCost);
        planRepository.save(plan);
        
        log.info("일정 총 비용 자동 계산 완료: 일정 ID {}, 총 비용 {}", planId, totalCost);
    }
} 