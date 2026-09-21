package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Activity tracker entry: gym, walk or basketball. Not medical advice. */
@Entity
@Table(name = "fitness_sessions", indexes = @Index(columnList = "userId,date"))
@Getter
@Setter
public class FitnessSession extends OwnedEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType type;
    @Column(nullable = false)
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    @Column(nullable = false)
    private int durationMinutes;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WorkoutType workoutType;
    private BigDecimal distanceKm;
    private Integer steps;
    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Exercise> exercises = new ArrayList<>();
}
