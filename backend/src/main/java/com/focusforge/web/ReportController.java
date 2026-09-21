package com.focusforge.web;

import com.focusforge.dto.ReportDtos.*;
import com.focusforge.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;
    private final InsightService insights;
    private final PlanService plans;
    private final PdfService pdf;
    private final AuthService auth;

    public ReportController(ReportService reports, InsightService insights, PlanService plans, PdfService pdf, AuthService auth) {
        this.reports = reports;
        this.insights = insights;
        this.plans = plans;
        this.pdf = pdf;
        this.auth = auth;
    }

    @GetMapping("/weekly")
    public WeeklyReport weekly(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return reports.weekly(id(), weekStart);
    }

    @GetMapping("/weekly/pdf")
    public ResponseEntity<byte[]> weeklyPdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        Long userId = id();
        byte[] bytes = pdf.weekly(userId, weekStart, auth.find(userId).getName());
        String name = "focusforge-weekly-" + reports.weekly(userId, weekStart).weekStart() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(name).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/insights")
    public Insights insights(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return insights.weekly(id(), weekStart);
    }

    @GetMapping("/plan")
    public NextWeekPlan plan(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return plans.get(id(), weekStart);
    }

    @PutMapping("/plan")
    public NextWeekPlan savePlan(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                                 @RequestBody PlanRequest req) {
        return plans.save(id(), weekStart, req);
    }
}
