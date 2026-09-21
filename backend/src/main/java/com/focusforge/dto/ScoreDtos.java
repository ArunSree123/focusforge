package com.focusforge.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Read models for the daily scorecard. */
public final class ScoreDtos {
    private ScoreDtos() {}

    /** type is one of MORNING, TIME, COUNT, CUSTOM. */
    public record ItemScore(Long routineItemId, String key, String title, String type,
                            int targetMinutes, int actualMinutes, int targetCount, int actualCount,
                            double ratio, boolean completed, boolean manual, LocalTime scheduledTime) {}

    public record DayScore(LocalDate date, List<ItemScore> items, int overallPercent,
                           int plannedMinutes, int completedMinutes, int studyMinutes, int gymMinutes,
                           int completedTasks, int remainingTasks, int missedTasks,
                           int problemsSolved, int jobsApplied, int awsTopicsCompleted,
                           boolean walked, boolean basketball, boolean activeMorning) {}
}
