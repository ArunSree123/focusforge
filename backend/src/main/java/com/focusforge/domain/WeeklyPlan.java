package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Saved target for one category in one week (Monday-based). */
@Entity
@Table(name = "weekly_plans", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "weekStart", "planKey"}))
@Getter
@Setter
public class WeeklyPlan extends OwnedEntity {
    @Column(nullable = false)
    private LocalDate weekStart;
    /** A RoutineKey name, e.g. JAVA, GYM or JOBS. */
    @Column(nullable = false, length = 30)
    private String planKey;
    private int targetMinutes;
    private int targetCount;
}
