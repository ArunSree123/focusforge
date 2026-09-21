package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "study_sessions", indexes = @Index(columnList = "userId,date"))
@Getter
@Setter
public class StudySession extends OwnedEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudyCategory category;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private int durationMinutes;
    @Column(length = 200)
    private String topic;
    @Column(length = 200)
    private String source;
    @Column(length = 2000)
    private String notes;
}
