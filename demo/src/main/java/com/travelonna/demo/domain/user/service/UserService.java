package com.travelonna.demo.domain.user.service;

import java.util.Optional;

import com.travelonna.demo.domain.user.entity.Profile;
import com.travelonna.demo.domain.user.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelonna.demo.domain.user.entity.User;
import com.travelonna.demo.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User processOAuth2User(String email, String name, String providerId) {
        return userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, name))
                .orElseGet(() -> createNewUser(email, name, providerId));
    }

    private User updateExistingUser(User existingUser, String name) {
        existingUser.setName(name);
        return userRepository.save(existingUser);
    }

    private User createNewUser(String email, String name, String providerId) {
        // 새로운 사용자 생성
        User user = User.builder()
                .email(email)
                .name(name)
                .providerId(providerId)
                .build();

        user = userRepository.save(user);

        // 프로필 생성
        Profile profile = Profile.builder()
                .user(user)
                .nickname(name)
                .introduction("안녕하세요!")
                .build();

        profileRepository.save(profile);

        return user;
    }
}
