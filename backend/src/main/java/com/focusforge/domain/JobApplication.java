package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "job_applications", indexes = @Index(columnList = "userId,dateApplied"))
@Getter
@Setter
public class JobApplication extends OwnedEntity {
    @Column(nullable = false, length = 150)
    private String company;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(length = 150)
    private String location;
    @Column(length = 500)
    private String jobUrl;
    @Column(nullable = false)
    private LocalDate dateApplied;
    @Column(length = 100)
    private String resumeVersion;
    @Column(length = 150)
    private String referral;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.APPLIED;
    private LocalDate followUpDate;
    @Column(length = 2000)
    private String notes;
}
