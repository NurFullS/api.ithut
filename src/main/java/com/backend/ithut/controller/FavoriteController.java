package com.backend.ithut.controller;

import com.backend.ithut.entity.Post;
import com.backend.ithut.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{postId}/toggle")
    public boolean toggleFavorite(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        return favoriteService.toggleFavorite(postId, userId);
    }

    @GetMapping("/user/{userId}")
    public List<Post> getUserFavorites(@PathVariable Long userId) {
        return favoriteService.getFavoritePosts(userId);
    }

    @GetMapping("/{postId}/count")
    public Integer getFavoriteCount(@PathVariable Long postId) {
        return favoriteService.getFavoriteCount(postId);
    }
}
