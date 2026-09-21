package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** Manual "mark complete" for a routine block on a given day. */
@Entity
@Table(name = "routine_completions", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "date", "routineItemId"}))
@Getter
@Setter
public class RoutineCompletion extends OwnedEntity {
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private Long routineItemId;
    private Instant completedAt;
}
