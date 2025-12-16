package com.backend.ithut.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "followers")
@Getter
@Setter
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Кто подписан
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // На кого подписан
    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    // Когда подписка создана
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
