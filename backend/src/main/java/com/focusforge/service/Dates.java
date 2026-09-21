package com.focusforge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/** One place for "today" and week boundaries (weeks start on Monday). */
@Component
public class Dates {
    private final ZoneId zone;

    public Dates(@Value("${app.timezone}") String zone) {
        this.zone = ZoneId.of(zone);
    }

    public LocalDate today() { return LocalDate.now(zone); }

    public LocalDate orToday(LocalDate d) { return d != null ? d : today(); }

    public LocalDate weekStart(LocalDate d) { return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); }

    public LocalDate weekStartOrCurrent(LocalDate d) { return weekStart(orToday(d)); }
}
