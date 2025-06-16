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
        // 1. 기본 검색 (기존 방식)
        // 2. 공백 제거 검색 추가
        // 3. 부분 단어 검색 추가
        
        String jpql = "SELECT DISTINCT p FROM Place p WHERE p.isPublic = true AND (" +
                      "p.name LIKE :keyword OR " +                                    // 기본 검색
                      "REPLACE(p.name, ' ', '') LIKE :keywordNoSpace OR " +          // 공백 제거 검색
                      "REPLACE(REPLACE(p.name, ' ', ''), '-', '') LIKE :keywordNormalized)"; // 공백, 하이픈 제거 검색
        
        log.info("장소 검색 쿼리 (개선): {}, 키워드: {}", jpql, keyword);
        
        TypedQuery<Place> query = em.createQuery(jpql, Place.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("keywordNoSpace", "%" + keyword.replace(" ", "") + "%");
        query.setParameter("keywordNormalized", "%" + keyword.replace(" ", "").replace("-", "") + "%");
        
        List<Place> results = query.getResultList();
        log.info("검색된 공개 장소 수 (개선): {}", results.size());
        
        return results;
    }

    @Override
    public List<Profile> searchProfilesByKeyword(String keyword) {
        // 닉네임도 동일하게 개선
        String jpql = "SELECT DISTINCT p FROM Profile p WHERE (" +
                      "p.nickname LIKE :keyword OR " +
                      "REPLACE(p.nickname, ' ', '') LIKE :keywordNoSpace OR " +
                      "REPLACE(REPLACE(p.nickname, ' ', ''), '-', '') LIKE :keywordNormalized)";
        
        log.info("닉네임 검색 쿼리 (개선): {}, 키워드: {}", jpql, keyword);
        
        TypedQuery<Profile> query = em.createQuery(jpql, Profile.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("keywordNoSpace", "%" + keyword.replace(" ", "") + "%");
        query.setParameter("keywordNormalized", "%" + keyword.replace(" ", "").replace("-", "") + "%");
        
        List<Profile> results = query.getResultList();
        log.info("검색된 닉네임 수 (개선): {}", results.size());
        
        return results;
    }

    @Override
    public List<Log> searchLogsByKeyword(String keyword) {
        String jpql = "SELECT DISTINCT l FROM Log l " +
                      "WHERE l.isPublic = true " +
                      "AND (l.comment LIKE :keyword OR " +
                      "REPLACE(l.comment, ' ', '') LIKE :keywordNoSpace OR " +
                      "REPLACE(REPLACE(l.comment, ' ', ''), '-', '') LIKE :keywordNormalized)";
        
        log.info("로그 검색 쿼리 (개선): {}, 키워드: {}", jpql, keyword);
        
        TypedQuery<Log> query = em.createQuery(jpql, Log.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("keywordNoSpace", "%" + keyword.replace(" ", "") + "%");
        query.setParameter("keywordNormalized", "%" + keyword.replace(" ", "").replace("-", "") + "%");
        
        return query.getResultList();
    }

    @Override
    public List<Place> searchPlacesByKeyword(String keyword, Integer offset, Integer limit) {
        String jpql = "SELECT DISTINCT p FROM Place p WHERE p.isPublic = true AND (" +
                      "p.name LIKE :keyword OR " +
                      "REPLACE(p.name, ' ', '') LIKE :keywordNoSpace OR " +
                      "REPLACE(REPLACE(p.name, ' ', ''), '-', '') LIKE :keywordNormalized) " +
                      "ORDER BY p.placeId";
        
        log.info("장소 검색 쿼리 (페이지네이션, 개선): {}, 키워드: {}, offset: {}, limit: {}", jpql, keyword, offset, limit);
        
        TypedQuery<Place> query = em.createQuery(jpql, Place.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("keywordNoSpace", "%" + keyword.replace(" ", "") + "%");
        query.setParameter("keywordNormalized", "%" + keyword.replace(" ", "").replace("-", "") + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        List<Place> results = query.getResultList();
        log.info("검색된 공개 장소 수 (페이지네이션, 개선): {}", results.size());
        
        return results;
    }

    @Override
    public List<Profile> searchProfilesByKeyword(String keyword, Integer offset, Integer limit) {
        String jpql = "SELECT DISTINCT p FROM Profile p WHERE (" +
                      "p.nickname LIKE :keyword OR " +
                      "REPLACE(p.nickname, ' ', '') LIKE :keywordNoSpace OR " +
                      "REPLACE(REPLACE(p.nickname, ' ', ''), '-', '') LIKE :keywordNormalized) " +
                      "ORDER BY p.userId";
        
        log.info("닉네임 검색 쿼리 (페이지네이션, 개선): {}, 키워드: {}, offset: {}, limit: {}", jpql, keyword, offset, limit);
        
        TypedQuery<Profile> query = em.createQuery(jpql, Profile.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("keywordNoSpace", "%" + keyword.replace(" ", "") + "%");
        query.setParameter("keywordNormalized", "%" + keyword.replace(" ", "").replace("-", "") + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        List<Profile> results = query.getResultList();
        log.info("검색된 닉네임 수 (페이지네이션, 개선): {}", results.size());
        
        return results;
    }

    @Override
    public List<Log> searchLogsByKeyword(String keyword, Integer offset, Integer limit) {
        String jpql = "SELECT DISTINCT l FROM Log l " +
                      "WHERE l.isPublic = true " +
                      "AND (l.comment LIKE :keyword OR " +
                      "REPLACE(l.comment, ' ', '') LIKE :keywordNoSpace OR " +
                      "REPLACE(REPLACE(l.comment, ' ', ''), '-', '') LIKE :keywordNormalized) " +
                      "ORDER BY l.logId DESC";
        
        log.info("로그 검색 쿼리 (페이지네이션, 개선): {}, 키워드: {}, offset: {}, limit: {}", jpql, keyword, offset, limit);
        
        TypedQuery<Log> query = em.createQuery(jpql, Log.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("keywordNoSpace", "%" + keyword.replace(" ", "") + "%");
        query.setParameter("keywordNormalized", "%" + keyword.replace(" ", "").replace("-", "") + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        List<Log> results = query.getResultList();
        log.info("검색된 로그 수 (페이지네이션, 개선): {}", results.size());
        
        return results;
    }
} 