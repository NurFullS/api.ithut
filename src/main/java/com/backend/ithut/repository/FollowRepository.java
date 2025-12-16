package com.backend.ithut.repository;

import com.backend.ithut.entity.Follow;
import com.backend.ithut.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    List<Follow> findAllByFollower(User follower);
    List<Follow> findAllByFollowing(User following);
    void deleteByFollowerAndFollowing(User follower, User following);
}
