package com.backend.ithut.controller;

import com.backend.ithut.config.JwtUtil;
import com.backend.ithut.entity.User;
import com.backend.ithut.service.PostService;
import com.backend.ithut.service.R2Service;
import com.backend.ithut.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private R2Service r2Service;

    @Autowired
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<?> userMe(@CookieValue(name = "jwt", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("Пользователь не авторизован");
        }

        try {
            String email = jwtUtil.extractUsername(token);
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            return ResponseEntity.ok(toUserDTO(user));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Невалидный токен");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestPart("data") String data,
            @RequestPart("avatar") MultipartFile avatar
    ) throws Exception {

        User user = objectMapper.readValue(data, User.class);

        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Пользователь с таким email уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String avatarUrl = uploadAvatar(avatar);
        user.setAvatarUrl(avatarUrl);
        user.setFileName(avatar.getOriginalFilename());

        userService.saveUser(user);

        return ResponseEntity.ok(toUserDTO(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser, HttpServletResponse response) {

        Optional<User> optionalUser = userService.getUserByEmail(loginUser.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Такого пользователя нет.");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Неправильный пароль.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.EXPIRATION_MS / 1000));
        response.addCookie(cookie);

        return ResponseEntity.ok(toUserDTO(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(toUserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/get-email/{email}")
    public ResponseEntity<?> getUserByEmailPath(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(toUserDTO(user)))
                .orElseGet(() -> {
                    HashMap<String, Object> error = new HashMap<>();
                    error.put("error", "Пользователь не найден");
                    return ResponseEntity.status(401).body(error);
                });
    }

    @GetMapping("/posts-by-email")
    public ResponseEntity<?> postsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(postService.findPostsByUserEmail(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.status(404).body("Пользователь не найден.");
        }

        userService.deleteUser(id);
        return ResponseEntity.ok("Пользователь успешно удалён.");
    }

    @PutMapping("/update-avatar/{id}")
    public ResponseEntity<?> updateAvatar(
            @PathVariable Long id,
            @RequestPart("avatar") MultipartFile avatar
    ) throws Exception {

        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String avatarUrl = uploadAvatar(avatar);

        userService.updateAvatar(id, avatar.getOriginalFilename(), avatarUrl);

        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("id", user.getId());
            put("email", user.getEmail());
            put("username", user.getUsername());
            put("avatarUrl", avatarUrl);
        }});
    }

    private HashMap<String, Object> toUserDTO(User user) {
        return new HashMap<>() {{
            put("id", user.getId());
            put("email", user.getEmail());
            put("username", user.getUsername());
            put("surname", user.getSurname());
            put("avatarUrl", user.getAvatarUrl());
        }};
    }

    private String uploadAvatar(MultipartFile avatar) throws Exception {
        String originalFilename = avatar.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(originalFilename.getBytes());
        String hash = new BigInteger(1, md.digest()).toString(16);

        String key = "avatars/" + System.currentTimeMillis() + "_" + hash + extension;

        return r2Service.uploadFile("filesithut", key, avatar);
    }
}
