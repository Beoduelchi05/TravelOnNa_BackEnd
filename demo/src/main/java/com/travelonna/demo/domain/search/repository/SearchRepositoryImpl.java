package com.travelonna.demo.domain.search.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.travelonna.demo.domain.log.entity.Log;
import com.travelonna.demo.domain.plan.entity.Place;
import com.travelonna.demo.domain.user.entity.Profile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SearchRepositoryImpl implements SearchRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Place> searchPlacesByKeyword(String keyword) {
        // p_name에서 키워드 검색 (isPublic = true인 경우만)
        String jpql = "SELECT p FROM Place p WHERE p.name LIKE :keyword AND p.isPublic = true";
        System.out.println("장소 검색 쿼리: " + jpql + ", 키워드: %" + keyword + "%");
        TypedQuery<Place> query = em.createQuery(jpql, Place.class);
        query.setParameter("keyword", "%" + keyword + "%");
        List<Place> results = query.getResultList();
        System.out.println("검색된 공개 장소 수: " + results.size());
        
        // 검색된 각 장소의 이름 출력
        for (Place place : results) {
            try {
                java.lang.reflect.Field nameField = place.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                String placeName = (String) nameField.get(place);
                System.out.println("검색된 공개 장소: " + placeName);
            } catch (Exception e) {
                System.out.println("장소명 조회 오류: " + e.getMessage());
            }
        }
        
        return results;
    }

    @Override
    public List<Profile> searchProfilesByKeyword(String keyword) {
        // nickname에서 키워드 검색 (모든 닉네임 대상)
        String jpql = "SELECT p FROM Profile p WHERE p.nickname LIKE :keyword";
        System.out.println("닉네임 검색 쿼리: " + jpql + ", 키워드: %" + keyword + "%");
        TypedQuery<Profile> query = em.createQuery(jpql, Profile.class);
        query.setParameter("keyword", "%" + keyword + "%");
        List<Profile> results = query.getResultList();
        System.out.println("검색된 닉네임 수: " + results.size());
        
        // 검색된 각 닉네임 출력
        for (Profile profile : results) {
            try {
                java.lang.reflect.Field nicknameField = profile.getClass().getDeclaredField("nickname");
                nicknameField.setAccessible(true);
                String nickname = (String) nicknameField.get(profile);
                System.out.println("검색된 닉네임: " + nickname);
            } catch (Exception e) {
                System.out.println("닉네임 조회 오류: " + e.getMessage());
            }
        }
        
        return results;
    }

    @Override
    public List<Log> searchLogsByKeyword(String keyword) {
        String jpql = "SELECT DISTINCT l FROM Log l " +
                      "WHERE l.isPublic = true " +
                      "AND l.comment LIKE :keyword";
        
        TypedQuery<Log> query = em.createQuery(jpql, Log.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }
} 