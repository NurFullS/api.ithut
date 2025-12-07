package com.backend.ithut.service;

import com.backend.ithut.entity.Like;
import com.backend.ithut.entity.Post;
import com.backend.ithut.entity.User;
import com.backend.ithut.repository.LikeRepository;
import com.backend.ithut.repository.PostRepository;
import com.backend.ithut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public int toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return likeRepository.findByPostAndUser(post, user)
                .map(like -> {
                    likeRepository.delete(like);
                    return countLikes(post);
                })
                .orElseGet(() -> {
                    Like like = new Like();
                    like.setPost(post);
                    like.setUser(user);
                    likeRepository.save(like);
                    return countLikes(post);
                });
    }

    public int countLikes(Post post) {
        return likeRepository.countByPost(post);
    }

    public int countLikesByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return countLikes(post);
    }

    public List<Long> getLikedPostsByUser(Long userId) {
        return likeRepository.findPostIdsByUserId(userId); // метод в репозитории
    }


    public List<Long> getUserLikedPostIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return likeRepository.findAllByUser(user)
                .stream()
                .map(l -> l.getPost().getId())
                .collect(Collectors.toList());
    }
}
