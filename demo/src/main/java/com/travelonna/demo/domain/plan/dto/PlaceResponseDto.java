package com.travelonna.demo.domain.plan.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.lang.reflect.Field;

import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.plan.entity.Plan;

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
@Schema(description = "장소 응답 DTO")
public class PlaceResponseDto {
    
    @Schema(description = "장소 ID", example = "1")
    private Integer id;
    
    @Schema(description = "장소 이름", example = "동대구역")
    private String name;
    
    @Schema(description = "장소 주소", example = "대구광역시 동구 동대구로 550")
    private String address;
    
    @Schema(description = "순서", example = "1")
    private Integer order;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
    
    @Schema(description = "방문 날짜", example = "2023-11-20T00:00:00Z")
    private LocalDateTime visitDate;
    
    @Schema(description = "일차", example = "1")
    private Integer day;
    
    @Schema(description = "장소 비용", example = "40000")
    private Integer cost;
    
    @Schema(description = "메모", example = "장소 메모 내용")
    private String memo;
    
    @Schema(description = "위도", example = "35.855415")
    private String lat;
    
    @Schema(description = "경도", example = "128.492514")
    private String lon;

    @Schema(description = "구글 ID", example = "ChIJCZ4FKFblZTURKU0R_4aNSek")
    private String googleId;
    
    public static PlaceResponseDto fromEntity(Place place) {
        PlaceResponseDto dto = new PlaceResponseDto();
        
        try {
            // 리플렉션을 사용하여 Place 필드에 접근
            Field placeIdField = place.getClass().getDeclaredField("placeId");
            placeIdField.setAccessible(true);
            dto.id = (Integer) placeIdField.get(place);
            
            Field nameField = place.getClass().getDeclaredField("name");
            nameField.setAccessible(true);
            dto.name = (String) nameField.get(place);
            
            Field placeField = place.getClass().getDeclaredField("place");
            placeField.setAccessible(true);
            dto.address = (String) placeField.get(place);
            
            Field orderField = place.getClass().getDeclaredField("order");
            orderField.setAccessible(true);
            dto.order = (Integer) orderField.get(place);
            
            Field isPublicField = place.getClass().getDeclaredField("isPublic");
            isPublicField.setAccessible(true);
            dto.isPublic = (Boolean) isPublicField.get(place);
            
            Field visitDateField = place.getClass().getDeclaredField("visitDate");
            visitDateField.setAccessible(true);
            dto.visitDate = (LocalDateTime) visitDateField.get(place);
            
            Field placeCostField = place.getClass().getDeclaredField("placeCost");
            placeCostField.setAccessible(true);
            dto.cost = (Integer) placeCostField.get(place);
            
            Field memoField = place.getClass().getDeclaredField("memo");
            memoField.setAccessible(true);
            dto.memo = (String) memoField.get(place);
            
            Field latField = place.getClass().getDeclaredField("lat");
            latField.setAccessible(true);
            dto.lat = (String) latField.get(place);
            
            Field lonField = place.getClass().getDeclaredField("lon");
            lonField.setAccessible(true);
            dto.lon = (String) lonField.get(place);
            
            Field googleIdField = place.getClass().getDeclaredField("googleId");
            googleIdField.setAccessible(true);
            dto.googleId = (String) googleIdField.get(place);
            
        } catch (Exception e) {
            // 에러 처리
            e.printStackTrace();
        }
        
        return dto;
    }
    
    public static PlaceResponseDto fromEntityWithDay(Place place) {
        PlaceResponseDto dto = fromEntity(place);
        
        try {
            // Plan 접근
            Field planField = place.getClass().getDeclaredField("plan");
            planField.setAccessible(true);
            Plan plan = (Plan) planField.get(place);
            
            // visitDate 가져오기
            Field visitDateField = place.getClass().getDeclaredField("visitDate");
            visitDateField.setAccessible(true);
            LocalDateTime visitDate = (LocalDateTime) visitDateField.get(place);
            
            // Plan의 startDate 가져오기
            if (plan != null && visitDate != null) {
                Field startDateField = plan.getClass().getDeclaredField("startDate");
                startDateField.setAccessible(true);
                LocalDate startDate = (LocalDate) startDateField.get(plan);
                
                if (startDate != null) {
                    // 여행 일차 계산
                    int day = (int) ChronoUnit.DAYS.between(startDate, visitDate.toLocalDate()) + 1;
                    dto.day = day;
                }
            }
        } catch (Exception e) {
            // 에러 처리
            e.printStackTrace();
        }
        
        return dto;
    }
} 