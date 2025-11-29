package com.emsi.blog.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emsi.blog.user.User;
import com.emsi.blog.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public User updateProfilePicture(Integer userId, String profilePictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Delete old profile picture from S3 if exists
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            try {
                s3Service.deleteFileByUrl(user.getProfilePictureUrl());
            } catch (Exception e) {
                // Log error but continue
                System.err.println("Failed to delete old profile picture: " + e.getMessage());
            }
        }

        user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
