package com.backend.ithut.repository;

import com.backend.ithut.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTitleContainingIgnoreCase(String title);
    List<Post> findByUserId(Long userId);
    List<Post> findByUserEmail(String email);
}
