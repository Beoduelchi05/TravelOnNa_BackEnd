package com.travelonna.demo.domain.log.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.travelonna.demo.domain.plan.entity.MapCode;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.plan.entity.Plan;
import com.travelonna.demo.domain.plan.repository.MapCodeRepository;
import com.travelonna.demo.domain.plan.repository.MyMapRepository;
import com.travelonna.demo.domain.plan.repository.PlaceRepository;
import com.travelonna.demo.domain.plan.repository.PlanRepository;
import com.travelonna.demo.domain.plan.service.MyMapService;
import com.travelonna.demo.domain.plan.service.PlanService;
import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.entity.UserAction.TargetType;
import com.travelonna.demo.domain.user.repository.UserRepository;
import com.travelonna.demo.domain.user.service.UserActionService;
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
    private final PlaceRepository placeRepository;
    private final UserActionService userActionService;
    private final MyMapService myMapService;
    private final MyMapRepository myMapRepository;
    private final MapCodeRepository mapCodeRepository;
    private final PlanService planService;
    
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    
    // 기록 생성
    @Transactional
    public LogResponseDto createLog(Integer userId, LogRequestDto requestDto) {
        logger.debug("여행 기록 생성 시작: 사용자 ID={}, 요청 데이터={}", userId, requestDto);
        
        // 사용자 검증
        logger.debug("사용자 조회 시도: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new ResourceNotFoundException("User not found: ID=" + userId);
                });
        logger.debug("사용자 조회 성공: user={}", user.getUserId());
        
        // 일정 검증 및 권한 확인 (그룹 멤버 포함)
        logger.debug("일정 조회 및 권한 확인 시도: planId={}", requestDto.getPlanId());
        Plan plan = planService.getPlanWithPermissionCheck(userId, requestDto.getPlanId());
        logger.debug("일정 조회 및 권한 확인 성공: plan={}, planUserId={}", plan.getPlanId(), plan.getUserId());
        
        // comment가 null이거나 비어있는지 확인 (@Valid로 검증되지만 추가 검증)
        if (requestDto.getComment() == null || requestDto.getComment().trim().isEmpty()) {
            logger.error("댓글이 비어있음");
            throw new IllegalArgumentException("Comment is required");
        }
        
        // placeId 처리 로직 디버깅
        logger.debug("placeId 처리: placeId={}", requestDto.getPlaceId());
        
        // placeId가 제공되고 유효한 경우, 해당 장소만 처리
        if (requestDto.getPlaceId() != null && requestDto.getPlaceId() > 0) {
            logger.debug("특정 장소에 대한 기록 생성: placeId={}", requestDto.getPlaceId());
            Place place = placeRepository.findByPlaceIdAndPlan_PlanId(requestDto.getPlaceId(), plan.getPlanId())
                    .orElseThrow(() -> {
                        logger.error("장소를 찾을 수 없음: placeId={}, planId={}", requestDto.getPlaceId(), plan.getPlanId());
                        return new ResourceNotFoundException("Place not found: ID=" + requestDto.getPlaceId());
                    });
            
            // 해당 장소에 대한 Log 생성
            Log logEntity = Log.builder()
                    .user(user)
                    .plan(plan)
                    .place(place)  // place 참조 추가
                    .comment(requestDto.getComment()) // 장소 이름은 place 참조로 처리하므로 comment에서 제거
                    .isPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : false)
                    .build();
            
            // images 컬렉션이 null인 경우 초기화
            if (logEntity.getImages() == null) {
                logEntity.setImages(new ArrayList<>());
            }
            
            Log savedLog = logRepository.save(logEntity);
            logger.debug("여행 기록 저장 성공 (장소 {}): logId={}", place.getPlaceId(), savedLog.getLogId());
            
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
            
            // MyMap 데이터 자동 생성
            try {
                myMapService.createMyMapFromLog(savedLog, user);
            } catch (Exception e) {
                logger.warn("MyMap 데이터 생성 실패: userId={}, logId={}, error={}", 
                           user.getUserId(), savedLog.getLogId(), e.getMessage());
            }
            
            // UserAction 기록 - POST 액션 (각 장소별 Log마다)
            try {
                userActionService.recordLogCreation(user.getUserId(), savedLog.getLogId());
            } catch (Exception e) {
                logger.warn("사용자 액션 기록 실패: userId={}, logId={}", user.getUserId(), savedLog.getLogId(), e);
            }
            
            return LogResponseDto.fromEntity(savedLog, false);
        }
        
        // placeId가 없거나 유효하지 않은 경우, 해당 planId의 모든 place 조회
        logger.debug("전체 장소에 대한 기록 생성 또는 장소 없는 기록 생성: placeId={}", requestDto.getPlaceId());
        List<Place> places = placeRepository.findByPlanIdOrderByOrder(plan.getPlanId());
        logger.debug("일정에 등록된 장소 수: {}", places.size());
        
        if (places.isEmpty()) {
            // 일정에 등록된 장소가 없는 경우 일반적인 방식으로 한 개의 Log 생성
            logger.debug("장소가 없는 일정이므로 단일 기록 생성");
            return createSingleLog(user, plan, requestDto);
        }
        
        // 각 place마다 Log 생성
        logger.debug("각 장소마다 기록 생성 시작");
        List<Log> createdLogs = new ArrayList<>();
        for (Place place : places) {
            Log logEntity = Log.builder()
                    .user(user)
                    .plan(plan)
                    .place(place)  // place 참조 추가
                    .comment(requestDto.getComment()) // 장소 이름은 place 참조로 처리하므로 comment에서 제거
                    .isPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : false)
                    .build();
            
            // images 컬렉션이 null인 경우 초기화
            if (logEntity.getImages() == null) {
                logEntity.setImages(new ArrayList<>());
            }
            
            Log savedLog = logRepository.save(logEntity);
            logger.debug("여행 기록 저장 성공 (장소 {}): logId={}", place.getPlaceId(), savedLog.getLogId());
            
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
            
            // UserAction 기록 - POST 액션 (각 장소별 Log마다)
            try {
                userActionService.recordLogCreation(user.getUserId(), savedLog.getLogId());
            } catch (Exception e) {
                logger.warn("사용자 액션 기록 실패: userId={}, logId={}", user.getUserId(), savedLog.getLogId(), e);
            }
            
            createdLogs.add(savedLog);
        }
        
        // 마지막에 생성된 Log를 반환 (또는 필요에 따라 첫 번째 Log를 반환)
        Log lastLog = createdLogs.get(createdLogs.size() - 1);
        return LogResponseDto.fromEntity(lastLog, false);
    }
    
    // 단일 Log 생성 메소드 (장소가 없는 경우 사용)
    private LogResponseDto createSingleLog(User user, Plan plan, LogRequestDto requestDto) {
        logger.debug("단일 Log 생성 시작: userId={}, planId={}", user.getUserId(), plan.getPlanId());
        
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
        logger.debug("단일 여행 기록 저장 성공: logId={}", savedLog.getLogId());
        
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
        
        // UserAction 기록 - POST 액션
        try {
            userActionService.recordLogCreation(user.getUserId(), savedLog.getLogId());
        } catch (Exception e) {
            logger.warn("사용자 액션 기록 실패: userId={}, logId={}", user.getUserId(), savedLog.getLogId(), e);
        }
        
        return LogResponseDto.fromEntity(savedLog, false);
    }
    
    // 기록 조회
    public LogResponseDto getLog(Integer logId, Integer userId) {
        Log log = logRepository.findByIdWithDetails(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        
        // 비공개 기록인 경우 권한 확인 (작성자만 접근 가능)
        // 공개 기록인 경우 로그인한 사용자라면 누구나 접근 가능
        if (!log.getIsPublic() && !log.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to view this log");
        }
        
        boolean isLiked = false;
        if (userId != null) {
            isLiked = likesRepository.findByLogLogIdAndUserUserId(logId, userId).isPresent();
            
            // UserAction 기록 - VIEW 액션 (로그인한 사용자이고 공개 기록인 경우만)
            if (log.getIsPublic()) {
                try {
                    userActionService.recordView(userId, logId, TargetType.LOG);
                } catch (Exception e) {
                    logger.warn("사용자 액션 기록 실패: userId={}, logId={}", userId, logId, e);
                }
            }
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
        
        // Log에 직접 연결된 Place가 없는 경우, Plan의 첫 번째 Place 정보 사용
        if (log.getPlace() != null) {
            responseDto.setPlaceId(log.getPlace().getPlaceId());
            responseDto.setPlaceName(log.getPlace().getName());
        } else if (!places.isEmpty()) {
            responseDto.setPlaceId(places.get(0).getPlaceId());
            responseDto.setPlaceName(places.get(0).getName());
        }
        
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
        List<LogResponseDto> result = convertToLogResponseDtoList(logs, userId);
        
        // 각 LogResponseDto에 mapcode 정보 추가
        result.forEach(dto -> setMapCodeInfoToDto(dto, dto.getLogId()));
        
        return result;
    }
    
    // 공개 기록 목록 조회
    public List<LogResponseDto> getPublicLogs(Integer userId) {
        List<Log> logs = logRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        return convertToLogResponseDtoList(logs, userId);
    }
    
    // 콜드스타트용 무작위 공개 기록 조회 (중복 제외)
    public List<LogResponseDto> getRandomPublicLogs(Integer userId, Integer limit, List<Integer> excludeLogIds) {
        logger.info("무작위 공개 기록 조회: userId={}, limit={}, 제외할 로그 수={}", 
                   userId, limit, excludeLogIds != null ? excludeLogIds.size() : 0);
        
        List<Log> allPublicLogs = logRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        
        // 제외할 로그 ID들 필터링
        List<Log> filteredLogs = allPublicLogs.stream()
                .filter(log -> excludeLogIds == null || !excludeLogIds.contains(log.getLogId()))
                .collect(Collectors.toList());
        
        // 무작위 셔플 후 제한된 개수만 선택
        List<Log> randomLogs = filteredLogs.stream()
                .sorted((a, b) -> Math.random() < 0.5 ? -1 : 1) // 간단한 무작위 정렬
                .limit(limit != null && limit > 0 ? limit : 10)
                .collect(Collectors.toList());
        
        logger.info("무작위 공개 기록 선택 완료: 전체={}, 필터링후={}, 최종선택={}", 
                   allPublicLogs.size(), filteredLogs.size(), randomLogs.size());
        
        return convertToLogResponseDtoList(randomLogs, userId);
    }
    
    // 콜드스타트용 무작위 공개 기록 조회 (페이지네이션 방식)
    public List<LogResponseDto> getRandomPublicLogsWithPagination(Integer userId, Integer limit, Integer offset) {
        logger.info("무작위 공개 기록 조회 (페이지네이션): userId={}, limit={}, offset={}", 
                   userId, limit, offset);
        
        List<Log> allPublicLogs = logRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        
        // 일관된 무작위 순서를 위해 사용자 ID를 시드로 사용
        List<Log> shuffledLogs = allPublicLogs.stream()
                .sorted((a, b) -> {
                    // 사용자 ID와 로그 ID를 조합한 해시로 일관된 정렬
                    int hashA = (userId.toString() + a.getLogId().toString()).hashCode();
                    int hashB = (userId.toString() + b.getLogId().toString()).hashCode();
                    return Integer.compare(hashA, hashB);
                })
                .collect(Collectors.toList());
        
        // 페이지네이션 적용
        int startIndex = offset != null ? offset : 0;
        int endIndex = Math.min(startIndex + (limit != null ? limit : 10), shuffledLogs.size());
        
        if (startIndex >= shuffledLogs.size()) {
            logger.info("요청한 오프셋이 전체 데이터를 초과: offset={}, total={}", startIndex, shuffledLogs.size());
            return List.of(); // 빈 리스트 반환
        }
        
        List<Log> pageLogList = shuffledLogs.subList(startIndex, endIndex);
        
        logger.info("무작위 공개 기록 페이지네이션 완료: 전체={}, 선택범위={}~{}, 최종선택={}", 
                   shuffledLogs.size(), startIndex, endIndex - 1, pageLogList.size());
        
        return convertToLogResponseDtoList(pageLogList, userId);
    }
    
    // 특정 장소별 기록 조회
    public List<LogResponseDto> getLogsByPlace(Integer placeId, Integer userId) {
        logger.info("장소별 기록 조회 시작: placeId={}, userId={}", placeId, userId);
        
        List<Log> logs = logRepository.findByPlacePlaceIdOrderByCreatedAtDesc(placeId);
        logger.info("조회된 Log 개수: {}", logs.size());
        
        if (logs.isEmpty()) {
            // 디버깅: 전체 Log 개수 확인
            long totalLogs = logRepository.count();
            logger.info("전체 Log 개수: {}", totalLogs);
        }
        
        List<LogResponseDto> result = convertToLogResponseDtoListForPlace(logs, userId, placeId);
        
        // 각 LogResponseDto에 mapcode 정보 추가
        result.forEach(dto -> setMapCodeInfoToDto(dto, dto.getLogId()));
        
        return result;
    }

    // 장소별 기록 조회를 위한 별도 변환 메소드
    private List<LogResponseDto> convertToLogResponseDtoListForPlace(List<Log> logs, Integer userId, Integer placeId) {
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
                
                // 특정 장소 정보만 설정 (해당 장소에 연결된 기록이므로)
                if (log.getPlace() != null && log.getPlace().getPlaceId().equals(placeId)) {
                    dto.setPlaceId(log.getPlace().getPlaceId());
                    dto.setPlaceName(log.getPlace().getName());
                    // 단일 장소이므로 배열에도 해당 장소만 포함
                    dto.setPlaceIds(List.of(log.getPlace().getPlaceId()));
                    dto.setPlaceNames(List.of(log.getPlace().getName()));
                }
                
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
                
                // 특정 장소 정보만 설정 (해당 장소에 연결된 기록이므로)
                if (log.getPlace() != null && log.getPlace().getPlaceId().equals(placeId)) {
                    dto.setPlaceId(log.getPlace().getPlaceId());
                    dto.setPlaceName(log.getPlace().getName());
                    // 단일 장소이므로 배열에도 해당 장소만 포함
                    dto.setPlaceIds(List.of(log.getPlace().getPlaceId()));
                    dto.setPlaceNames(List.of(log.getPlace().getName()));
                }
                
                result.add(dto);
            }
        }
        
        return result;
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
            // 새로운 일정에 대한 권한 확인 (그룹 멤버 포함)
            Plan plan = planService.getPlanWithPermissionCheck(userId, requestDto.getPlanId());
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
        
        // Log에 직접 연결된 Place가 없는 경우, Plan의 첫 번째 Place 정보 사용
        if (responseDto.getPlaceId() == null && responseDto.getPlaceName() == null && !places.isEmpty()) {
            Place firstPlace = places.get(0);
            responseDto.setPlaceId(firstPlace.getPlaceId());
            responseDto.setPlaceName(firstPlace.getName());
        }
        
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
        
        // MyMap 데이터 먼저 삭제
        try {
            myMapService.deleteMyMapByLogId(logId);
        } catch (Exception e) {
            logger.warn("MyMap 데이터 삭제 실패: logId={}, error={}", logId, e.getMessage());
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
                    
                    // UserAction 기록 - LIKE 액션 (좋아요 추가 시이고 공개 기록인 경우만)
                    if (log.getIsPublic()) {
                        try {
                            userActionService.recordLike(userId, logId);
                        } catch (Exception e) {
                            logger.warn("사용자 액션 기록 실패: userId={}, logId={}", userId, logId, e);
                        }
                    }
                    
                    return true;
                });
    }
    
    // 장소 정보를 LogResponseDto에 설정하는 공통 메소드
    private void setPlaceInfoToDto(LogResponseDto dto, Integer planId) {
        // 여행 계획에 연결된 장소 정보 가져오기
        List<Place> places = placeRepository.findByPlanIdOrderByOrder(planId);
        
        // 모든 장소 ID들 설정
        List<Integer> placeIds = places.stream()
                .map(Place::getPlaceId)
                .collect(Collectors.toList());
        dto.setPlaceIds(placeIds);
        
        // 모든 장소 이름들 설정 (p_name 컬럼 사용)
        List<String> placeNames = places.stream()
                .map(Place::getName)
                .collect(Collectors.toList());
        dto.setPlaceNames(placeNames);
        
        // 호환성을 위해 첫 번째 장소 정보도 설정
        if (!places.isEmpty()) {
            Place firstPlace = places.get(0);
            dto.setPlaceId(firstPlace.getPlaceId());
            dto.setPlaceName(firstPlace.getName());
        }
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
                
                // 장소 정보 설정
                setPlaceInfoToDto(dto, log.getPlan().getPlanId());
                
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
                
                // 모든 장소 ID들 설정
                List<Integer> placeIds = places.stream()
                        .map(Place::getPlaceId)
                        .collect(Collectors.toList());
                dto.setPlaceIds(placeIds);
                
                // 모든 장소 이름들 설정 (p_name 컬럼 사용)
                List<String> placeNames = places.stream()
                        .map(Place::getName)
                        .collect(Collectors.toList());
                dto.setPlaceNames(placeNames);
                
                // 호환성을 위해 첫 번째 장소 정보도 설정
                if (!places.isEmpty()) {
                    Place firstPlace = places.get(0);
                    dto.setPlaceId(firstPlace.getPlaceId());
                    dto.setPlaceName(firstPlace.getName());
                }
                
                result.add(dto);
            }
        }
        
        return result;
    }
    
    /**
     * LogResponseDto에 mapcode 정보를 설정하는 메소드
     */
    private void setMapCodeInfoToDto(LogResponseDto dto, Integer logId) {
        try {
            // MyMap에서 해당 로그의 mapcode 정보 조회
            myMapRepository.findByLogLogId(logId)
                    .ifPresentOrElse(
                            myMap -> {
                                MapCode mapCode = myMap.getMapCode();
                                if (mapCode != null) {
                                    dto.setMapCodeId(mapCode.getMapCodeId());
                                    dto.setMapCodeCity(mapCode.getCity());
                                    dto.setMapCodeDistrict(mapCode.getDistrict());
                                }
                            },
                            () -> {
                                // MyMap이 없는 경우 Plan의 location 정보로부터 mapcode 추출 시도
                                setMapCodeFromPlanLocation(dto, logId);
                            }
                    );
        } catch (Exception e) {
            logger.warn("MapCode 정보 설정 실패: logId={}, error={}", logId, e.getMessage());
        }
    }
    
    /**
     * Plan의 location 정보로부터 mapcode를 찾아서 설정하는 메소드
     */
    private void setMapCodeFromPlanLocation(LogResponseDto dto, Integer logId) {
        try {
            logRepository.findById(logId).ifPresent(log -> {
                Plan plan = log.getPlan();
                if (plan != null && plan.getLocation() != null) {
                    String location = plan.getLocation();
                    
                    // location 문자열을 파싱해서 도시와 구/군 추출
                    String[] locationParts = parseLocationString(location);
                    String city = locationParts[0];
                    String district = locationParts[1];
                    
                    // mapcode 조회
                    if (district != null && !district.trim().isEmpty()) {
                        mapCodeRepository.findByCityAndDistrict(city, district)
                                .ifPresent(mapCode -> {
                                    dto.setMapCodeId(mapCode.getMapCodeId());
                                    dto.setMapCodeCity(mapCode.getCity());
                                    dto.setMapCodeDistrict(mapCode.getDistrict());
                                });
                    } else {
                        mapCodeRepository.findByCityOnly(city)
                                .ifPresent(mapCode -> {
                                    dto.setMapCodeId(mapCode.getMapCodeId());
                                    dto.setMapCodeCity(mapCode.getCity());
                                    dto.setMapCodeDistrict(mapCode.getDistrict());
                                });
                    }
                }
            });
        } catch (Exception e) {
            logger.warn("Plan location으로부터 MapCode 정보 설정 실패: logId={}, error={}", logId, e.getMessage());
        }
    }
    
    /**
     * location 문자열을 파싱해서 도시와 구/군을 추출하는 메소드
     * 예: "서울특별시 강남구" -> ["서울특별시", "강남구"]
     */
    private String[] parseLocationString(String location) {
        if (location == null || location.trim().isEmpty()) {
            return new String[]{null, null};
        }
        
        location = location.trim();
        
        // 공백으로 분리
        String[] parts = location.split("\\s+");
        
        if (parts.length >= 2) {
            return new String[]{parts[0], parts[1]};
        } else if (parts.length == 1) {
            return new String[]{parts[0], null};
        } else {
            return new String[]{null, null};
        }
    }
} 