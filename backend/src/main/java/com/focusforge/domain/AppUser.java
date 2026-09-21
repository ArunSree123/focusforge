package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 200)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private LocalTime wakeTargetTime = LocalTime.of(6, 0);
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
