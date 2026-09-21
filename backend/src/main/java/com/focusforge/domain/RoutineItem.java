package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/** A scheduled block in the user's day, with an editable target and time. */
@Entity
@Table(name = "routine_items", indexes = @Index(columnList = "userId"))
@Getter
@Setter
public class RoutineItem extends OwnedEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoutineKey routineKey;
    @Column(nullable = false, length = 100)
    private String title;
    /** Target in minutes (time-based blocks). */
    private int targetMinutes;
    /** Target count (job applications per day). */
    private int targetCount;
    private LocalTime scheduledTime;
    @Column(nullable = false)
    private boolean active = true;
    private int sortOrder;
}
