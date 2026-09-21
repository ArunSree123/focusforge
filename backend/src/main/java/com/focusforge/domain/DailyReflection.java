package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_reflections", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "date"}))
@Getter
@Setter
public class DailyReflection extends OwnedEntity {
    @Column(nullable = false)
    private LocalDate date;
    private Integer mood;
    private Integer energy;
    private Integer focus;
    @Column(length = 2000)
    private String wentWell;
    @Column(length = 2000)
    private String distractions;
    @Column(length = 2000)
    private String improveTomorrow;
}
