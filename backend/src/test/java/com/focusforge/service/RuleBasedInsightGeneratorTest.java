package com.focusforge.service;

import com.focusforge.dto.ReportDtos.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedInsightGeneratorTest {
    private final RuleBasedInsightGenerator generator = new RuleBasedInsightGenerator(new Dates("Asia/Kolkata"));

    private WeeklyReport report(int studyMinutes, int applications, List<CategoryRow> learning) {
        LocalDate ws = LocalDate.of(2026, 9, 14);
        return new WeeklyReport(ws, ws.plusDays(6), studyMinutes > 0,
                new Overall(0, 0, 0, studyMinutes / 7, null, 0, 0, 0, studyMinutes),
                learning, new Career(applications, 70, 0, 0, 0, 0, 0), new Fitness(0, 6, 0, 0, 0, 0),
                new SolveStats(0, 0, 0, 0), new Dsa(0, 0, 0, 0, 0, 0), new SolveStats(0, 0, 0, 0),
                new SolveStats(0, 0, 0, 0), new Aws(0, 0, 0, 20), List.of(), List.of());
    }

    @Test
    void refusesToInventInsightsWithoutData() {
        Insights in = generator.generate(report(0, 0, List.of()));
        assertFalse(in.hasEnoughData());
        assertEquals("Not enough data to generate a reliable insight yet.", in.message());
    }

    @Test
    void mentionsOnlyRecordedNumbers() {
        Insights in = generator.generate(report(600, 40, List.of(
                new CategoryRow("JAVA", "Java", 840, 800, 95.2),
                new CategoryRow("AWS", "AWS", 420, 120, 28.6))));
        assertTrue(in.hasEnoughData());
        assertTrue(in.wentWell().stream().anyMatch(s -> s.contains("Java") && s.contains("95.2%")));
        assertTrue(in.needsAttention().stream().anyMatch(s -> s.contains("AWS") && s.contains("28.6%")));
    }

    @Test
    void formatsDurations() {
        assertEquals("2h 30m", RuleBasedInsightGenerator.hm(150));
        assertEquals("45m", RuleBasedInsightGenerator.hm(45));
        assertEquals("3h", RuleBasedInsightGenerator.hm(180));
    }
}
