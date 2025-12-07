package com.backend.ithut.repository;

import com.backend.ithut.entity.Like;
import com.backend.ithut.entity.Post;
import com.backend.ithut.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
    List<Like> findAllByUser(User user);
    int countByPost(Post post);
    @Query("SELECT l.post.id FROM Like l WHERE l.user.id = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
