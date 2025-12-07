package com.backend.ithut.controller;

import com.backend.ithut.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LikeWebSocketController {

    private final LikeService likeService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/like/toggle")
    public void toggleLike(LikeMessage message) {
        int newCount = likeService.toggleLike(message.getPostId(), message.getUserId());

        template.convertAndSend("/topic/post/" + message.getPostId(), newCount);
    }

    public static class LikeMessage {
        private Long postId;
        private Long userId;

        public Long getPostId() { return postId; }
        public void setPostId(Long postId) { this.postId = postId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
