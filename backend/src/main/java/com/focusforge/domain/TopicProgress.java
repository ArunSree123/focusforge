package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** A learning topic (AWS service, DSA pattern, interview subject...) with a 4-level status. */
@Entity
@Table(name = "topic_progress", indexes = @Index(columnList = "userId,area"))
@Getter
@Setter
public class TopicProgress extends OwnedEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TopicArea area;
    @Column(name = "topic_group", nullable = false, length = 100)
    private String group;
    @Column(nullable = false, length = 150)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TopicStatus status = TopicStatus.NOT_STARTED;
    private LocalDate completedOn;
    @Column(length = 1000)
    private String notes;
    private int sortOrder;
}
