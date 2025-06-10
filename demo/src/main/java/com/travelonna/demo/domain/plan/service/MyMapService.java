package com.travelonna.demo.domain.plan.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.plan.entity.MapCode;
import com.travelonna.demo.domain.plan.entity.MyMap;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.plan.repository.MapCodeRepository;
import com.travelonna.demo.domain.plan.repository.MyMapRepository;
import com.travelonna.demo.domain.plan.repository.PlaceRepository;
import com.travelonna.demo.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyMapService {
    
    private final MyMapRepository myMapRepository;
    private final MapCodeRepository mapCodeRepository;
    private final PlaceRepository placeRepository;
    
    /**
     * 로그 생성 시 MyMap 데이터 자동 생성 (임시 구현)
     */
    public void createMyMapFromLog(Log travelLog, User user) {
        try {
            log.info("MyMap 데이터 생성 시작");
            
            // 임시로 간단하게 구현 - 실제 엔티티 메서드가 구현되면 수정 필요
            String city = "서울특별시"; // 임시값
            String district = "강남구"; // 임시값
            
            // MapCode 조회 또는 생성
            MapCode mapCode = findOrCreateMapCode(city, district);
            
            // MyMap 생성 - builder 패턴이 작동하지 않으면 기본 생성자 사용
            MyMap myMap = new MyMap();
            myMap.setLog(travelLog);
            myMap.setUser(user);
            myMap.setMapCode(mapCode);
            
            myMapRepository.save(myMap);
            log.info("MyMap 데이터 생성 완료");
            
        } catch (Exception e) {
            log.error("MyMap 데이터 생성 실패: {}", e.getMessage());
            // 에러가 발생해도 메인 로직에 영향을 주지 않도록 예외를 먹습니다
        }
    }
    
    /**
     * 로그 삭제 시 MyMap 데이터도 함께 삭제
     */
    public void deleteMyMapByLogId(Integer logId) {
        try {
            log.info("MyMap 데이터 삭제 시작: logId={}", logId);
            myMapRepository.deleteByLogLogId(logId);
            log.info("MyMap 데이터 삭제 완료: logId={}", logId);
        } catch (Exception e) {
            log.error("MyMap 데이터 삭제 실패: logId={}, error={}", logId, e.getMessage());
        }
    }
    
    /**
     * MapCode 조회 또는 생성
     */
    private MapCode findOrCreateMapCode(String city, String district) {
        Optional<MapCode> existingMapCode;
        
        if (district != null && !district.trim().isEmpty()) {
            existingMapCode = mapCodeRepository.findByCityAndDistrict(city, district);
        } else {
            existingMapCode = mapCodeRepository.findByCityOnly(city);
        }
        
        if (existingMapCode.isPresent()) {
            return existingMapCode.get();
        }
        
        // 새로운 MapCode 생성 - builder 패턴이 작동하지 않으면 기본 생성자 사용
        MapCode newMapCode = new MapCode();
        newMapCode.setCity(city);
        newMapCode.setDistrict(district);
        
        return mapCodeRepository.save(newMapCode);
    }
} 