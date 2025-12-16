package com.backend.ithut.controller;

import com.backend.ithut.entity.Follow;
import com.backend.ithut.entity.User;
import com.backend.ithut.service.FollowService;
import com.backend.ithut.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{followingId}")
    public ResponseEntity<?> followUser(@RequestParam Long followerId, @PathVariable Long followingId) {
        try {
            Follow follow = followService.followUser(followerId, followingId);
            return ResponseEntity.ok(follow);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при подписке");
        }
    }

    @GetMapping("/following-ids/{userId}")
    public ResponseEntity<List<Long>> getFollowingIds(@PathVariable Long userId) {
        List<Long> followingIds = followService.getFollowing(userId)
                .stream()
                .map(f -> f.getFollowing().getId())
                .toList();
        return ResponseEntity.ok(followingIds);
    }


    @DeleteMapping("/{followingId}")
    public ResponseEntity<?> unfollowUser(@RequestParam Long followerId, @PathVariable Long followingId) {
        try {
            followService.unfollowUser(followerId, followingId);
            return ResponseEntity.ok("Отписка успешна");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при отписке");
        }
    }

    @GetMapping("/{followerId}/{followingId}")
    public ResponseEntity<?> isFollowing(@PathVariable Long followerId, @PathVariable Long followingId) {
        boolean following = followService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getFollowers(@PathVariable Long userId) {
        List<Follow> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<?> getFollowing(@PathVariable Long userId) {
        List<Follow> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }

}
