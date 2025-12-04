package com.backend.ithut.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category;
    private String fileName;
    private String postUrl;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;
}
