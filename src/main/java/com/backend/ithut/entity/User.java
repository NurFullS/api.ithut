package com.backend.ithut.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String surname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fileName;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public enum Role {
        USER,
        ADMIN
    }
}
