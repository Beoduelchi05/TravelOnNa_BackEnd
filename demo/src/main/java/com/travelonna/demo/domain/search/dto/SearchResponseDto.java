package com.travelonna.demo.domain.search.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.user.entity.Profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 결과 응답 DTO")
public class SearchResponseDto {
    
    @Schema(description = "장소 검색 결과 목록")
    @Builder.Default
    private List<PlaceDto> places = new ArrayList<>();
    
    @Schema(description = "사용자 검색 결과 목록")
    @Builder.Default
    private List<UserDto> users = new ArrayList<>();
    
    @Schema(description = "여행 기록 검색 결과 목록")
    @Builder.Default
    private List<LogDto> logs = new ArrayList<>();
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소 검색 결과 항목")
    public static class PlaceDto {
        
        @Schema(description = "장소 ID")
        private Integer placeId;
        
        @Schema(description = "장소 이름")
        private String name;
        
        @Schema(description = "장소 주소")
        private String address;
        
        @Schema(description = "위도")
        private String lat;
        
        @Schema(description = "경도")
        private String lon;
        
        @Schema(description = "구글 ID")
        private String googleId;
        
        public static PlaceDto fromEntity(Place place) {
            PlaceDto dto = new PlaceDto();
            try {
                // 리플렉션을 사용하여 필드에 직접 접근
                java.lang.reflect.Field placeIdField = place.getClass().getDeclaredField("placeId");
                placeIdField.setAccessible(true);
                dto.placeId = (Integer) placeIdField.get(place);
                
                java.lang.reflect.Field nameField = place.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                dto.name = (String) nameField.get(place);
                
                java.lang.reflect.Field placeField = place.getClass().getDeclaredField("place");
                placeField.setAccessible(true);
                dto.address = (String) placeField.get(place);
                
                java.lang.reflect.Field latField = place.getClass().getDeclaredField("lat");
                latField.setAccessible(true);
                dto.lat = (String) latField.get(place);
                
                java.lang.reflect.Field lonField = place.getClass().getDeclaredField("lon");
                lonField.setAccessible(true);
                dto.lon = (String) lonField.get(place);
                
                java.lang.reflect.Field googleIdField = place.getClass().getDeclaredField("googleId");
                googleIdField.setAccessible(true);
                dto.googleId = (String) googleIdField.get(place);
                
                System.out.println("Place 변환 완료: " + dto.name + " (ID: " + dto.placeId + ")");
            } catch (Exception e) {
                System.out.println("Place 변환 오류: " + e.getMessage());
                // 기본값 설정
                dto.placeId = 0;
                dto.name = "오류";
                dto.address = "오류";
            }
            return dto;
        }
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 검색 결과 항목")
    public static class UserDto {
        
        @Schema(description = "사용자 ID")
        private Integer userId;
        
        @Schema(description = "닉네임")
        private String nickname;
        
        @Schema(description = "프로필 이미지 URL")
        private String profileImage;
        
        @Schema(description = "자기소개")
        private String introduction;
        
        public static UserDto fromEntity(Profile profile) {
            UserDto dto = new UserDto();
            try {
                java.lang.reflect.Field userIdField = profile.getClass().getDeclaredField("userId");
                userIdField.setAccessible(true);
                dto.userId = (Integer) userIdField.get(profile);
                
                java.lang.reflect.Field nicknameField = profile.getClass().getDeclaredField("nickname");
                nicknameField.setAccessible(true);
                dto.nickname = (String) nicknameField.get(profile);
                
                java.lang.reflect.Field profileImageField = profile.getClass().getDeclaredField("profileImage");
                profileImageField.setAccessible(true);
                dto.profileImage = (String) profileImageField.get(profile);
                
                java.lang.reflect.Field introductionField = profile.getClass().getDeclaredField("introduction");
                introductionField.setAccessible(true);
                dto.introduction = (String) introductionField.get(profile);
            } catch (Exception e) {
                dto.userId = 0;
                dto.nickname = "";
            }
            return dto;
        }
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "여행 기록 검색 결과 항목")
    public static class LogDto {
        
        @Schema(description = "여행 기록 ID")
        private Integer logId;
        
        @Schema(description = "작성자 ID")
        private Integer userId;
        
        @Schema(description = "작성자 이름")
        private String userName;
        
        @Schema(description = "내용")
        private String comment;
        
        @Schema(description = "작성일시")
        private LocalDateTime createdAt;
        
        @Schema(description = "이미지 URL 목록")
        @Builder.Default
        private List<String> imageUrls = new ArrayList<>();
        
        @Schema(description = "좋아요 수")
        private Integer likeCount;
        
        public static LogDto fromEntity(Log log) {
            LogDto dto = new LogDto();
            try {
                java.lang.reflect.Field logIdField = log.getClass().getDeclaredField("logId");
                logIdField.setAccessible(true);
                dto.logId = (Integer) logIdField.get(log);
                
                java.lang.reflect.Field userField = log.getClass().getDeclaredField("user");
                userField.setAccessible(true);
                Object user = userField.get(log);
                
                if (user != null) {
                    java.lang.reflect.Field userIdField = user.getClass().getDeclaredField("userId");
                    userIdField.setAccessible(true);
                    dto.userId = (Integer) userIdField.get(user);
                    
                    java.lang.reflect.Field nameField = user.getClass().getDeclaredField("name");
                    nameField.setAccessible(true);
                    dto.userName = (String) nameField.get(user);
                }
                
                java.lang.reflect.Field commentField = log.getClass().getDeclaredField("comment");
                commentField.setAccessible(true);
                dto.comment = (String) commentField.get(log);
                
                java.lang.reflect.Field createdAtField = log.getClass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                dto.createdAt = (java.time.LocalDateTime) createdAtField.get(log);
                
                dto.imageUrls = new ArrayList<>();
                dto.likeCount = 0;
            } catch (Exception e) {
                dto.logId = 0;
                dto.comment = "";
                dto.imageUrls = new ArrayList<>();
                dto.likeCount = 0;
            }
            
            return dto;
        }
    }
} 