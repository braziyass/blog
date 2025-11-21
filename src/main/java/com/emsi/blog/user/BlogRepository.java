package com.emsi.blog.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findByUser(User user);
    List<Blog> findAllByUser(User user);
    Optional<Blog> findByPublicId(String publicId);
}

