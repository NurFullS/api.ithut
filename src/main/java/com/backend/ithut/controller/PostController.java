package com.backend.ithut.controller;

import com.backend.ithut.config.JwtUtil;
import com.backend.ithut.entity.Post;
import com.backend.ithut.entity.User;
import com.backend.ithut.service.PostService;
import com.backend.ithut.service.R2Service;
import com.backend.ithut.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/post/")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    R2Service r2Service;

    @Autowired
    UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/all-posts")
    public ResponseEntity<?> allPosts() {
        List<Post> allPosts = postService.getAllPosts();
        return ResponseEntity.ok(allPosts);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestPart("data") String data,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request
    ) throws IOException, NoSuchAlgorithmException {

        // 1. Берем токен из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Пользователь не авторизован");
        }
        String token = authHeader.substring(7);

        // 2. Получаем email пользователя из токена
        String email;
        try {
            email = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Невалидный токен");
        }

        // 3. Ищем пользователя в БД
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 4. Создаём объект поста
        Post post = new ObjectMapper().readValue(data, Post.class);
        post.setUser(user);

        // 5. Обработка файла
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) extension = originalFilename.substring(dotIndex);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(originalFilename.getBytes());
        String hash = new BigInteger(1, md.digest()).toString(16);

        String key = "posts/" + System.currentTimeMillis() + "_" + hash + extension;

        // 6. Загружаем файл в R2
        String postUrl = r2Service.uploadFile("filesithut", key, file);
        post.setFileName(originalFilename);
        post.setPostUrl(postUrl);

        // 7. Сохраняем пост
        Post savedPost = postService.savePost(post);

        return ResponseEntity.ok(savedPost);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException, NoSuchAlgorithmException {

        Post existingPost = postService.getPostById(id)
                .orElseThrow(() -> new RuntimeException("Пост не найден"));

        Post updatedPost = new ObjectMapper().readValue(data, Post.class);

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setDescription(updatedPost.getDescription());
        existingPost.setCategory(updatedPost.getCategory());

        // Если пришел новый файл, загружаем его
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex >= 0) extension = originalFilename.substring(dotIndex);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(originalFilename.getBytes());
            String hash = new BigInteger(1, md.digest()).toString(16);

            String key = "posts/" + System.currentTimeMillis() + "_" + hash + extension;
            String postUrl = r2Service.uploadFile("filesithut", key, file);

            existingPost.setFileName(originalFilename);
            existingPost.setPostUrl(postUrl);
        }

        Post savedPost = postService.savePost(existingPost);
        return ResponseEntity.ok(savedPost);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        Optional<Post> optionalPost = postService.getPostById(id);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(404).body("Пост не найден");
        }

        postService.deletePost(id);
        return ResponseEntity.ok("Пост успешно удалён");
    }
}
