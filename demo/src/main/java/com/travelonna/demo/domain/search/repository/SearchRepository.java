package com.travelonna.demo.domain.search.repository;

import java.util.List;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.user.entity.Profile;

public interface SearchRepository {
    
    /**
     * 키워드로 장소를 검색합니다. (장소 이름이 키워드를 포함하는 경우)
     * 
     * @param keyword 검색 키워드
     * @return 검색된 장소 목록
     */
    List<Place> searchPlacesByKeyword(String keyword);
    
    /**
     * 키워드로 사용자 프로필을 검색합니다. (닉네임이 키워드를 포함하는 경우)
     * 
     * @param keyword 검색 키워드
     * @return 검색된 프로필 목록
     */
    List<Profile> searchProfilesByKeyword(String keyword);
    
    /**
     * 키워드로 여행 기록을 검색합니다. (댓글이 키워드를 포함하거나, 관련 장소가 키워드를 포함하는 경우)
     * 
     * @param keyword 검색 키워드
     * @return 검색된 여행 기록 목록
     */
    List<Log> searchLogsByKeyword(String keyword);
} 