package com.focusforge.service;

import com.focusforge.dto.ReportDtos.Insights;
import com.focusforge.dto.ReportDtos.WeeklyReport;

/**
 * Extension point for weekly analysis. The default implementation is rule-based and uses only recorded numbers.
 * An LLM-backed implementation can be added by providing another bean, as long as it only receives the report data.
 */
public interface InsightGenerator {
    Insights generate(WeeklyReport report);
}
