package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects", indexes = @Index(columnList = "userId"))
@Getter
@Setter
public class Project extends OwnedEntity {
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 3000)
    private String problemStatement;
    @Column(length = 3000)
    private String features;
    @Column(length = 1000)
    private String techStack;
    @Column(length = 3000)
    private String architecture;
    @Column(length = 3000)
    private String databaseDesign;
    @Column(length = 3000)
    private String apiFlow;
    @Column(length = 3000)
    private String challenges;
    @Column(length = 3000)
    private String solutions;
    @Column(length = 3000)
    private String deployment;
    @Column(length = 3000)
    private String futureImprovements;
}
