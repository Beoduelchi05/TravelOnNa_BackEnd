package com.travelonna.demo.domain.recommendation.repository;

import java.time.LocalDateTime;

public interface RecommendationProjection {
    Integer getItemId();
    Float getScore();
    Integer getLogId();
    Integer getLogUserId();
    Integer getPlanId();
    String getComment();
    LocalDateTime getCreatedAt();
    Boolean getIsPublic();
} 