package com.focusforge.service;

import com.focusforge.dto.ReportDtos.Insights;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InsightService {
    private final ReportService reports;
    private final InsightGenerator generator;

    public InsightService(ReportService reports, InsightGenerator generator) {
        this.reports = reports;
        this.generator = generator;
    }

    public Insights weekly(Long userId, LocalDate anyDay) {
        return generator.generate(reports.weekly(userId, anyDay));
    }
}
