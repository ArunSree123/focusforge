package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "morning_checks", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "date", "morningItemId"}))
@Getter
@Setter
public class MorningCheck extends OwnedEntity {
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private Long morningItemId;
}
