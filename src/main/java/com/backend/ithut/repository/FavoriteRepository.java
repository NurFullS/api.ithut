package com.backend.ithut.repository;

import com.backend.ithut.entity.Favorite;
import com.backend.ithut.entity.Post;
import com.backend.ithut.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByPostAndUser(Post post, User user);

    List<Favorite> findAllByUser(User user);

    Integer countByPost(Post post);
}
