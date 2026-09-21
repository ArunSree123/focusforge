package com.focusforge.web;

import com.focusforge.dto.ReportDtos.MonthlyAnalytics;
import com.focusforge.dto.ReportDtos.WeeklyReport;
import com.focusforge.dto.ScoreDtos.DayScore;
import com.focusforge.service.Dates;
import com.focusforge.service.ReportService;
import com.focusforge.service.ScoreService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final ReportService reports;
    private final ScoreService scores;
    private final Dates dates;

    public AnalyticsController(ReportService reports, ScoreService scores, Dates dates) {
        this.reports = reports;
        this.scores = scores;
        this.dates = dates;
    }

    @GetMapping("/daily")
    public DayScore daily(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scores.day(id(), dates.orToday(date));
    }

    @GetMapping("/weekly")
    public WeeklyReport weekly(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return reports.weekly(id(), weekStart);
    }

    @GetMapping("/monthly")
    public MonthlyAnalytics monthly(@RequestParam(required = false) String month) {
        return reports.monthly(id(), month);
    }
}
