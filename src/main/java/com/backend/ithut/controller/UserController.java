package com.backend.ithut.controller;

import com.backend.ithut.config.JwtUtil;
import com.backend.ithut.entity.User;
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
import java.util.Optional;

@RestController
@RequestMapping("/v1/users/")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    R2Service r2Service;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

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

            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("id", user.getId());
                put("email", user.getEmail());
                put("username", user.getUsername());
                put("surname", user.getSurname());
                put("avatarUrl", user.getAvatarUrl());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Невалидный токен");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestPart("data") String data,
            @RequestPart("avatar") MultipartFile avatar
    ) throws IOException, NoSuchAlgorithmException {

        User user = objectMapper.readValue(data, User.class);

        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Пользователь с таким email уже существует");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        String originalFilename = avatar.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(originalFilename.getBytes());
        String hash = new BigInteger(1, md.digest()).toString(16);

        String key = "avatars/" + System.currentTimeMillis() + "_" + hash + extension;

        String avatarUrl = r2Service.uploadFile("filesithut", key, avatar);

        user.setFileName(originalFilename);
        user.setAvatarUrl(avatarUrl);

        userService.saveUser(user);

        return ResponseEntity.ok(user);
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

        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("id", user.getId());
            put("email", user.getEmail());
            put("username", user.getUsername());
            put("surname", user.getSurname());
            put("avatarUrl", user.getAvatarUrl());
        }});
    }


    @GetMapping("/get-email/{email}")
    public ResponseEntity<?> getUsername(@PathVariable String email) {
        Optional<User> optionalGetEmail = userService.getUserByEmail(email);

        if (optionalGetEmail.isEmpty()) {
            return ResponseEntity.status(401).body("Такого пользовователья нету.");
        }

        User user = optionalGetEmail.get();

        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("id", user.getId());
            put("email", user.getEmail());
            put("username", user.getUsername());
            put("surname", user.getSurname());
            put("avatarUrl", user.getAvatarUrl());
        }});
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> optionalUser = userService.getUserById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("Такой пользователь не найден.");
        }

        userService.deleteUser(id); // метод должен удалять пользователя по id
        return ResponseEntity.ok("Пользователь успешно удалён.");
    }

    @PutMapping("/update-avatar/{id}")
    public ResponseEntity<?> updateAvatar(
            @PathVariable Long id,
            @RequestPart("avatar") MultipartFile avatar
    ) throws Exception {

        Optional<User> optionalUser = userService.getUserById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("Пользователь не найден.");
        }

        User user = optionalUser.get();

        String originalFilename = avatar.getOriginalFilename();
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(originalFilename.getBytes());
        String hash = new BigInteger(1, md.digest()).toString(16);

        String key = "avatars/" + System.currentTimeMillis() + "_" + hash + extension;
        String avatarUrl = r2Service.uploadFile("filesithut", key, avatar);

        userService.updateAvatar(id, originalFilename, avatarUrl);

        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("id", user.getId());
            put("email", user.getEmail());
            put("username", user.getUsername());
            put("avatarUrl", avatarUrl);
        }});
    }
}