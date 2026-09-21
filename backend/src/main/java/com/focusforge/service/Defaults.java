package com.focusforge.service;

import com.focusforge.domain.RoutineKey;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** Default targets from the product brief. Every value is editable per user afterwards. */
public final class Defaults {
    private Defaults() {}

    public record RoutineDefault(RoutineKey key, String title, int minutes, int count, LocalTime time) {}

    public static final List<RoutineDefault> ROUTINE = List.of(
            new RoutineDefault(RoutineKey.TECH_GK, "Tech news + GK", 30, 0, LocalTime.of(7, 0)),
            new RoutineDefault(RoutineKey.SQL, "SQL + MySQL", 60, 0, LocalTime.of(7, 30)),
            new RoutineDefault(RoutineKey.PROBLEM_SOLVING, "Problem solving", 120, 0, LocalTime.of(8, 30)),
            new RoutineDefault(RoutineKey.JAVA, "Java", 120, 0, LocalTime.of(10, 30)),
            new RoutineDefault(RoutineKey.AWS, "AWS", 60, 0, LocalTime.of(13, 0)),
            new RoutineDefault(RoutineKey.PROJECT_INTERVIEW, "Project + Interview prep", 60, 0, LocalTime.of(14, 0)),
            new RoutineDefault(RoutineKey.DSA, "DSA + LeetCode", 120, 0, LocalTime.of(15, 0)),
            new RoutineDefault(RoutineKey.GYM, "Gym", 120, 0, LocalTime.of(18, 0)),
            new RoutineDefault(RoutineKey.JOBS, "Job applications", 0, 10, LocalTime.of(20, 30)));

    public static final List<String> MORNING = List.of("Drink water", "Stretching", "Freshening up", "Personal preparation");

    /** A day counts toward the streak when overall completion reaches this percentage. */
    public static final int ACTIVE_DAY_PERCENT = 50;
    public static final int WEEKLY_GYM_SESSIONS = 6;

    public static final Map<RoutineKey, String> LABELS = Map.of(
            RoutineKey.JAVA, "Java",
            RoutineKey.DSA, "DSA",
            RoutineKey.SQL, "SQL",
            RoutineKey.PROBLEM_SOLVING, "Problem solving",
            RoutineKey.AWS, "AWS",
            RoutineKey.PROJECT_INTERVIEW, "Project + Interview",
            RoutineKey.TECH_GK, "Tech + GK",
            RoutineKey.GYM, "Gym",
            RoutineKey.JOBS, "Applications");

    /** Display order for learning categories in reports. */
    public static final List<RoutineKey> LEARNING_ORDER = List.of(
            RoutineKey.JAVA, RoutineKey.DSA, RoutineKey.SQL, RoutineKey.PROBLEM_SOLVING,
            RoutineKey.AWS, RoutineKey.PROJECT_INTERVIEW, RoutineKey.TECH_GK);
}
