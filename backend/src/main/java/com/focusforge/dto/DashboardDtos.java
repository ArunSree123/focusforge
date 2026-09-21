package com.focusforge.dto;

import com.focusforge.dto.FitnessDtos.FitnessSummary;
import com.focusforge.dto.ScoreDtos.DayScore;

import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {}

    public record DsaToday(int solvedToday, int easy, int medium, int hard, long lifetimeSolved) {}

    public record TopicSummary(int completed, int total, int percent) {}

    public record JobDay(int today, int target, int week, int weekTarget) {}

    public record Milestone(String icon, String title, String description, boolean achieved, long progress, long goal) {}

    public record TodayDto(String name, LocalDate date, DayScore score, int streak, FitnessSummary fitness,
                           DsaToday dsa, TopicSummary aws, TopicSummary interview, JobDay jobs,
                           List<Milestone> milestones, boolean hasDemoData, boolean reflectionLogged) {}
}
