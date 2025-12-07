package com.backend.ithut.service;

import com.backend.ithut.entity.Favorite;
import com.backend.ithut.entity.Post;
import com.backend.ithut.entity.User;
import com.backend.ithut.repository.FavoriteRepository;
import com.backend.ithut.repository.PostRepository;
import com.backend.ithut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public boolean toggleFavorite(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepository.findByPostAndUser(post, user)
                .map(fav -> {
                    favoriteRepository.delete(fav);
                    return false; // убрано из избранного
                })
                .orElseGet(() -> {
                    Favorite f = new Favorite();
                    f.setPost(post);
                    f.setUser(user);
                    favoriteRepository.save(f);
                    return true; // добавлено в избранное
                });
    }

    public List<Post> getFavoritePosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepository.findAllByUser(user)
                .stream()
                .map(Favorite::getPost)
                .toList();
    }

    public Integer getFavoriteCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return favoriteRepository.countByPost(post);
    }
}
