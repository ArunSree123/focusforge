package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "daily_routines", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "date"}))
@Getter
@Setter
public class DailyRoutine extends OwnedEntity {
    @Column(nullable = false)
    private LocalDate date;
    /** Snapshot of the target at the time, so changing the target later never rewrites history. */
    @Column(nullable = false)
    private LocalTime wakeTarget;
    private LocalTime wakeActual;
}
