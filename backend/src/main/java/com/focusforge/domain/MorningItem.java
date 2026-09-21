package com.focusforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "morning_items", indexes = @Index(columnList = "userId"))
@Getter
@Setter
public class MorningItem extends OwnedEntity {
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false)
    private boolean active = true;
    private int sortOrder;
}
