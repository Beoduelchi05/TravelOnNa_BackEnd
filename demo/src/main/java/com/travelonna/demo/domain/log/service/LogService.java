package com.travelonna.demo.domain.log.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.log.dto.LogRequestDto;
import com.travelonna.demo.domain.log.dto.LogResponseDto;
import com.travelonna.demo.domain.log.entity.Likes;
import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.log.entity.LogImage;
import com.travelonna.demo.domain.log.repository.LikesRepository;
import com.travelonna.demo.domain.log.repository.LogImageRepository;
import com.travelonna.demo.domain.log.repository.LogRepository;
import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.plan.repository.PlaceRepository;
import com.travelonna.demo.domain.plan.repository.PlanRepository;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;
import com.travelonna.demo.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogService {
    
    private final LogRepository logRepository;
    private final LogImageRepository logImageRepository;
    private final LikesRepository likesRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PlaceRepository placeRepository;
    
    private static final Logger log = LoggerFactory.getLogger(LogService.class);
    
    // 기록 생성
    @Transactional
    public LogResponseDto createLog(Integer userId, LogRequestDto requestDto) {
        log.debug("여행 기록 생성 시작: 사용자 ID={}, 요청 데이터={}", userId, requestDto);
        
        // 사용자 검증
        log.debug("사용자 조회 시도: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new ResourceNotFoundException("User not found: ID=" + userId);
                });
        log.debug("사용자 조회 성공: user={}", user.getUserId());
        
        // 일정 검증
        log.debug("일정 조회 시도: planId={}", requestDto.getPlanId());
        Plan plan = planRepository.findById(requestDto.getPlanId())
                .orElseThrow(() -> {
                    log.error("일정을 찾을 수 없음: planId={}", requestDto.getPlanId());
                    return new ResourceNotFoundException("Plan not found: ID=" + requestDto.getPlanId());
                });
        log.debug("일정 조회 성공: plan={}, planUserId={}", plan.getPlanId(), plan.getUserId());
        
        // 사용자가 해당 일정의 소유자인지 확인
        if (!plan.getUserId().equals(userId)) {
            log.error("사용자가 일정의 소유자가 아님: userId={}, planUserId={}", userId, plan.getUserId());
            throw new IllegalArgumentException("User is not the owner of the plan");
        }
        
        // comment가 null이거나 비어있는지 확인 (@Valid로 검증되지만 추가 검증)
        if (requestDto.getComment() == null || requestDto.getComment().trim().isEmpty()) {
            log.error("댓글이 비어있음");
            throw new IllegalArgumentException("Comment is required");
        }
        
        Log logEntity = Log.builder()
                .user(user)
                .plan(plan)
                .comment(requestDto.getComment())
                .isPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : false)
                .build();
        
        // images 컬렉션이 null인 경우 초기화
        if (logEntity.getImages() == null) {
            logEntity.setImages(new ArrayList<>());
        }
        
        Log savedLog = logRepository.save(logEntity);
        log.debug("여행 기록 저장 성공: logId={}", savedLog.getLogId());
        
        // 이미지가 제공된 경우에만 처리 (선택 사항)
        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            int maxImages = Math.min(requestDto.getImageUrls().size(), 10);
            for (int i = 0; i < maxImages; i++) {
                LogImage image = LogImage.builder()
                        .log(savedLog)
                        .imageUrl(requestDto.getImageUrls().get(i))
                        .orderNum(i + 1)
                        .build();
                savedLog.addImage(image);
            }
        }
        
        return LogResponseDto.fromEntity(savedLog, false);
    }
    
    // 기록 조회
    public LogResponseDto getLog(Integer logId, Integer userId) {
        Log log = logRepository.findByIdWithDetails(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        
        // 비공개 기록인 경우 권한 확인
        if (!log.getIsPublic() && !log.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to view this log");
        }
        
        boolean isLiked = false;
        if (userId != null) {
            isLiked = likesRepository.findByLogLogIdAndUserUserId(logId, userId).isPresent();
        }
        
        LogResponseDto responseDto = LogResponseDto.fromEntity(log, isLiked);
        
        // 이미지 URL 목록 추가
        List<String> imageUrls = log.getImages().stream()
                .sorted((i1, i2) -> i1.getOrderNum().compareTo(i2.getOrderNum()))
                .map(LogImage::getImageUrl)
                .collect(Collectors.toList());
        responseDto.setImageUrls(imageUrls);
        
        // 여행 계획에 연결된 장소 정보 가져오기
        List<Place> places = placeRepository.findByPlanIdOrderByOrder(log.getPlan().getPlanId());
        List<String> placeNames = places.stream()
                .map(Place::getPlace)
                .collect(Collectors.toList());
        responseDto.setPlaceNames(placeNames);
        
        return responseDto;
    }
    
    // 사용자별 기록 목록 조회
    public List<LogResponseDto> getLogsByUser(Integer userId, Integer currentUserId) {
        List<Log> logs = logRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        return convertToLogResponseDtoList(logs, currentUserId);
    }
    
    // 일정별 기록 조회
    public List<LogResponseDto> getLogsByPlan(Integer planId, Integer userId) {
        List<Log> logs = logRepository.findByPlanPlanIdOrderByCreatedAtDesc(planId);
        return convertToLogResponseDtoList(logs, userId);
    }
    
    // 공개 기록 목록 조회
    public List<LogResponseDto> getPublicLogs(Integer userId) {
        List<Log> logs = logRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        return convertToLogResponseDtoList(logs, userId);
    }
    
    // 기록 수정
    @Transactional
    public LogResponseDto updateLog(Integer logId, Integer userId, LogRequestDto requestDto) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        
        // 기록 작성자 확인
        if (!log.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to update this log");
        }
        
        // comment가 null이거나 비어있는지 확인 (@Valid로 검증되지만 추가 검증)
        if (requestDto.getComment() == null || requestDto.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment is required");
        }
        
        log.updateComment(requestDto.getComment());
        
        // isPublic이 null이면 기본값 false 설정
        log.updateIsPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : false);
        
        // 일정 수정이 필요한 경우
        if (requestDto.getPlanId() != null && !requestDto.getPlanId().equals(log.getPlan().getPlanId())) {
            Plan plan = planRepository.findById(requestDto.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
            
            // 사용자가 해당 일정의 소유자인지 확인
            if (!plan.getUserId().equals(userId)) {
                throw new IllegalArgumentException("User is not the owner of the plan");
            }
            
            log.setPlan(plan);
        }
        
        // 이미지 수정 - 이미지는 선택 사항이므로 null이면 기존 이미지 유지
        if (requestDto.getImageUrls() != null) {
            // 기존 이미지 삭제
            logImageRepository.deleteByLogLogId(logId);
            log.getImages().clear();
            
            // 새 이미지가 있는 경우에만 추가
            if (!requestDto.getImageUrls().isEmpty()) {
                // 새 이미지 추가 (최대 10개)
                int maxImages = Math.min(requestDto.getImageUrls().size(), 10);
                for (int i = 0; i < maxImages; i++) {
                    LogImage image = LogImage.builder()
                            .log(log)
                            .imageUrl(requestDto.getImageUrls().get(i))
                            .orderNum(i + 1)
                            .build();
                    log.addImage(image);
                }
            }
        }
        
        boolean isLiked = likesRepository.findByLogLogIdAndUserUserId(logId, userId).isPresent();
        
        LogResponseDto responseDto = LogResponseDto.fromEntity(log, isLiked);
        
        // 이미지 URL 목록 추가
        List<String> imageUrls = log.getImages().stream()
                .sorted((i1, i2) -> i1.getOrderNum().compareTo(i2.getOrderNum()))
                .map(LogImage::getImageUrl)
                .collect(Collectors.toList());
        responseDto.setImageUrls(imageUrls);
        
        // 여행 계획에 연결된 장소 정보 가져오기
        List<Place> places = placeRepository.findByPlanIdOrderByOrder(log.getPlan().getPlanId());
        List<String> placeNames = places.stream()
                .map(Place::getPlace)
                .collect(Collectors.toList());
        responseDto.setPlaceNames(placeNames);
        
        return responseDto;
    }
    
    // 기록 삭제
    @Transactional
    public void deleteLog(Integer logId, Integer userId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        
        // 기록 작성자 확인
        if (!log.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this log");
        }
        
        logRepository.delete(log);
    }
    
    // 기록 좋아요 토글
    @Transactional
    public boolean toggleLike(Integer logId, Integer userId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // 비공개 기록인 경우 권한 확인
        if (!log.getIsPublic() && !log.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to like this log");
        }
        
        // 좋아요가 이미 있는지 확인
        return likesRepository.findByLogLogIdAndUserUserId(logId, userId)
                .map(like -> {
                    // 좋아요가 있으면 취소
                    likesRepository.delete(like);
                    return false;
                })
                .orElseGet(() -> {
                    // 좋아요가 없으면 추가
                    Likes likes = Likes.builder()
                            .log(log)
                            .user(user)
                            .build();
                    likesRepository.save(likes);
                    return true;
                });
    }
    
    // 엔티티 리스트를 DTO 리스트로 변환
    private List<LogResponseDto> convertToLogResponseDtoList(List<Log> logs, Integer userId) {
        List<LogResponseDto> result = new ArrayList<>();
        
        if (userId != null) {
            // 사용자가 좋아요한 기록 ID 리스트 조회
            List<Integer> likedLogIds = likesRepository.findLogIdsByUserUserId(userId);
            
            for (Log log : logs) {
                // 비공개 기록인 경우 작성자만 볼 수 있음
                if (!log.getIsPublic() && !log.getUser().getUserId().equals(userId)) {
                    continue;
                }
                
                LogResponseDto dto = LogResponseDto.fromEntity(log, likedLogIds.contains(log.getLogId()));
                
                // 이미지 URL 목록 추가
                List<String> imageUrls = log.getImages().stream()
                        .sorted((i1, i2) -> i1.getOrderNum().compareTo(i2.getOrderNum()))
                        .map(LogImage::getImageUrl)
                        .collect(Collectors.toList());
                dto.setImageUrls(imageUrls);
                
                // 여행 계획에 연결된 장소 정보 가져오기
                List<Place> places = placeRepository.findByPlanIdOrderByOrder(log.getPlan().getPlanId());
                List<String> placeNames = places.stream()
                        .map(Place::getPlace)
                        .collect(Collectors.toList());
                dto.setPlaceNames(placeNames);
                
                result.add(dto);
            }
        } else {
            // 로그인하지 않은 사용자는 공개 기록만 볼 수 있음
            for (Log log : logs) {
                if (!log.getIsPublic()) {
                    continue;
                }
                
                LogResponseDto dto = LogResponseDto.fromEntity(log, false);
                
                // 이미지 URL 목록 추가
                List<String> imageUrls = log.getImages().stream()
                        .sorted((i1, i2) -> i1.getOrderNum().compareTo(i2.getOrderNum()))
                        .map(LogImage::getImageUrl)
                        .collect(Collectors.toList());
                dto.setImageUrls(imageUrls);
                
                // 여행 계획에 연결된 장소 정보 가져오기
                List<Place> places = placeRepository.findByPlanIdOrderByOrder(log.getPlan().getPlanId());
                List<String> placeNames = places.stream()
                        .map(Place::getPlace)
                        .collect(Collectors.toList());
                dto.setPlaceNames(placeNames);
                
                result.add(dto);
            }
        }
        
        return result;
    }
} 