package com.emsi.blog.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByBlogAndUser(Blog blog, User user);
    Optional<Like> findByBlogAndUser(Blog blog, User user);
}