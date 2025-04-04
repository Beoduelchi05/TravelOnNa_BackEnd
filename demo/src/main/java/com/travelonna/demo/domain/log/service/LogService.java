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
import com.travelonna.demo.domain.plan.repository.PlanRepository;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;
import com.travelonna.demo.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogService {
    
    private final LogRepository logRepository;
    private final LogImageRepository logImageRepository;
    private final LikesRepository likesRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    
    // 기록 생성
    @Transactional
    public LogResponseDto createLog(Integer userId, LogRequestDto requestDto) {
        // 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // 일정 검증
        Plan plan = planRepository.findById(requestDto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        
        // 사용자가 해당 일정의 소유자인지 확인
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not the owner of the plan");
        }
        
        // comment가 null이거나 비어있는지 확인 (@Valid로 검증되지만 추가 검증)
        if (requestDto.getComment() == null || requestDto.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment is required");
        }
        
        Log log = Log.builder()
                .user(user)
                .plan(plan)
                .comment(requestDto.getComment())
                .isPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : false)
                .build();
        
        // images 컬렉션이 null인 경우 초기화
        if (log.getImages() == null) {
            log.setImages(new ArrayList<>());
        }
        
        Log savedLog = logRepository.save(log);
        
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
                
                result.add(dto);
            }
        }
        
        return result;
    }
} 