package com.backend.ithut.service;

import com.backend.ithut.entity.Follow;
import com.backend.ithut.entity.User;
import com.backend.ithut.repository.FollowRepository;
import com.backend.ithut.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public Follow followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Нельзя подписаться на себя");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Optional<Follow> existing = followRepository.findByFollowerAndFollowing(follower, following);
        if (existing.isPresent()) {
            return existing.get();
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        return followRepository.save(follow);
    }

    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return followRepository.findByFollowerAndFollowing(follower, following).isPresent();
    }

    public List<Follow> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return followRepository.findAllByFollowing(user);
    }

    public List<Follow> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return followRepository.findAllByFollower(user);
    }
}
