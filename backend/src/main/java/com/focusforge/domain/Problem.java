package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** One table for DSA, SQL, Java and reasoning problems, distinguished by kind. */
@Entity
@Table(name = "problems", indexes = @Index(columnList = "userId,kind,date"))
@Getter
@Setter
public class Problem extends OwnedEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProblemKind kind;
    @Column(nullable = false, length = 200)
    private String title;
    private Integer problemNumber;
    @Column(length = 500)
    private String url;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty = Difficulty.MEDIUM;
    @Column(length = 100)
    private String topic;
    private int attempts = 1;
    private int timeMinutes;
    private boolean solved;
    @Column(nullable = false)
    private LocalDate date;
    @Column(length = 2000)
    private String notes;
    @Column(length = 5000)
    private String solution;
}
