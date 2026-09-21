package com.focusforge.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ReportDtos {
    private ReportDtos() {}

    /** Learning row: minutes for time-based categories. key is a RoutineKey name. */
    public record CategoryRow(String key, String label, int targetMinutes, int actualMinutes, double completionPct) {}

    public record Overall(int plannedMinutes, int completedMinutes, double completionPct, int avgStudyMinutesPerDay,
                          LocalDate bestDay, int bestDayMinutes, int longestSessionMinutes, int streak, int totalStudyMinutes) {}

    public record Career(int applications, int applicationTarget, int responses, int assessments, int interviews,
                         int offers, double responseRate) {}

    public record Fitness(int gymSessions, int gymTarget, int gymMinutes, int walkDays, int basketballDays,
                          int activeMornings) {}

    public record SolveStats(int attempted, int solved, double successRate, int avgSolveMinutes) {}

    public record Dsa(int solved, int easy, int medium, int hard, int topicsLearned, long lifetimeSolved) {}

    public record Aws(int topicsCompleted, int handsOnCompleted, int totalCompleted, int totalTopics) {}

    public record DayPoint(LocalDate date, Map<String, Integer> minutesByCategory, int studyMinutes, int gymMinutes,
                           int applications, int problemsSolved, int completionPct) {}

    /** unit is minutes, count or days. changePct is null when last week had no baseline. */
    public record Comparison(String key, String label, String unit, int lastWeek, int thisWeek, Double changePct, String note) {}

    public record WeeklyReport(LocalDate weekStart, LocalDate weekEnd, boolean hasData, Overall overall,
                               List<CategoryRow> learning, Career career, Fitness fitness, SolveStats problemSolving,
                               Dsa dsa, SolveStats sql, SolveStats java, Aws aws, List<DayPoint> days,
                               List<Comparison> comparison) {}

    public record Insights(boolean hasEnoughData, String message, String summary, List<String> wentWell,
                           List<String> needsAttention, List<String> learningPattern, List<String> careerProgress,
                           List<String> fitnessConsistency, List<String> suggestedFocus, String source,
                           Instant generatedAt) {}

    public record PlanRow(String key, String label, int targetMinutes, int targetCount) {}

    public record NextWeekPlan(LocalDate weekStart, boolean saved, String basis, List<PlanRow> rows) {}

    public record PlanRequest(List<PlanRow> rows) {}

    public record MonthPoint(LocalDate date, int studyMinutes, int gymMinutes, int applications,
                             int problemsSolved, int completionPct) {}

    public record MonthlyAnalytics(String month, List<MonthPoint> days, int totalStudyMinutes, int totalGymMinutes,
                                   int totalApplications, int totalProblemsSolved, int activeDays,
                                   int avgCompletionPct) {}
}
