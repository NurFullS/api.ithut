package com.backend.ithut.controller;

import com.backend.ithut.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/toggle")
    public int toggleLike(@PathVariable Long postId, @RequestParam Long userId) {
        return likeService.toggleLike(postId, userId);
    }

    @GetMapping("/user/{userId}")
    public List<Long> getUserLikedPosts(@PathVariable Long userId) {

        return likeService.getUserLikedPostIds(userId);
    }

    @GetMapping("/{postId}/count")
    public int getLikeCount(@PathVariable Long postId) {
        return likeService.countLikesByPostId(postId);
    }

    @GetMapping("/user/posts/{userId}")
    public List<Long> getLikedPostsByUser(@PathVariable Long userId) {
        return likeService.getLikedPostsByUser(userId); // возвращает список ID постов
    }
}
