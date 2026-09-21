package com.focusforge.service;

import com.focusforge.common.BadRequestException;
import com.focusforge.dto.ReportDtos.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.focusforge.service.RuleBasedInsightGenerator.hm;

/** Renders the weekly report as a clean A4 PDF. */
@Service
public class PdfService {
    private static final Color INK = new Color(0x2B, 0x26, 0x22);
    private static final Color MOSS = new Color(0x4F, 0x7A, 0x64);
    private static final Color SAND = new Color(0xF3, 0xEE, 0xE6);
    private static final Color MUTED = new Color(0x6E, 0x65, 0x5B);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final ReportService reports;
    private final PlanService plans;
    private final InsightService insights;

    public PdfService(ReportService reports, PlanService plans, InsightService insights) {
        this.reports = reports;
        this.plans = plans;
        this.insights = insights;
    }

    public byte[] weekly(Long userId, LocalDate anyDay, String userName) {
        WeeklyReport r = reports.weekly(userId, anyDay);
        Insights in = insights.weekly(userId, anyDay);
        NextWeekPlan plan = plans.get(userId, r.weekStart().plusWeeks(1));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 48, 48);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, INK);
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
            doc.add(new Paragraph("Weekly Career & Productivity Report", h1));
            doc.add(new Paragraph(userName + "  |  " + r.weekStart().format(FMT) + " - " + r.weekEnd().format(FMT), sub));
            doc.add(spacer());

            // 1. Overview
            section(doc, "1. Weekly overview");
            Overall o = r.overall();
            doc.add(table(new float[]{3, 2}, new String[]{"Measure", "Value"}, List.of(
                    row("Planned time", hm(o.plannedMinutes())),
                    row("Completed time (capped at target)", hm(o.completedMinutes())),
                    row("Completion", o.completionPct() + "%"),
                    row("Total study time", hm(o.totalStudyMinutes())),
                    row("Average study per day", hm(o.avgStudyMinutesPerDay())),
                    row("Best day", o.bestDay() == null ? "-" : o.bestDay().format(FMT) + " (" + hm(o.bestDayMinutes()) + ")"),
                    row("Longest session", hm(o.longestSessionMinutes())),
                    row("Current streak", o.streak() + " days"))));

            // 2. Target vs actual
            section(doc, "2. Target vs actual");
            List<String[]> tva = new ArrayList<>();
            for (CategoryRow c : r.learning()) tva.add(row(c.label(), hm(c.targetMinutes()), hm(c.actualMinutes()), c.completionPct() + "%"));
            tva.add(row("Applications", String.valueOf(r.career().applicationTarget()), String.valueOf(r.career().applications()),
                    pct(r.career().applications(), r.career().applicationTarget())));
            doc.add(table(new float[]{3, 2, 2, 2}, new String[]{"Area", "Target", "Actual", "Completion"}, tva));

            // 3. Learning breakdown by day
            section(doc, "3. Learning breakdown by day");
            List<String[]> byDay = new ArrayList<>();
            for (DayPoint d : r.days()) {
                byDay.add(row(d.date().format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)), hm(d.studyMinutes()),
                        hm(d.minutesByCategory().getOrDefault("JAVA", 0)), hm(d.minutesByCategory().getOrDefault("DSA", 0)),
                        hm(d.minutesByCategory().getOrDefault("SQL", 0)), hm(d.minutesByCategory().getOrDefault("AWS", 0))));
            }
            doc.add(table(new float[]{2, 2, 2, 2, 2, 2}, new String[]{"Day", "Study", "Java", "DSA", "SQL", "AWS"}, byDay));

            // 4-7 Progress sections
            section(doc, "4. DSA progress");
            doc.add(table(new float[]{3, 2}, new String[]{"Measure", "Value"}, List.of(
                    row("Solved this week", String.valueOf(r.dsa().solved())),
                    row("Easy / Medium / Hard", r.dsa().easy() + " / " + r.dsa().medium() + " / " + r.dsa().hard()),
                    row("Topics completed", String.valueOf(r.dsa().topicsLearned())),
                    row("Lifetime solved", String.valueOf(r.dsa().lifetimeSolved())))));
            section(doc, "5. SQL progress");
            doc.add(solveTable(r.sql()));
            section(doc, "6. Java progress");
            doc.add(solveTable(r.java()));
            section(doc, "7. AWS progress");
            doc.add(table(new float[]{3, 2}, new String[]{"Measure", "Value"}, List.of(
                    row("Topics completed this week", String.valueOf(r.aws().topicsCompleted())),
                    row("Hands-on tasks this week", String.valueOf(r.aws().handsOnCompleted())),
                    row("Overall topics completed", r.aws().totalCompleted() + " / " + r.aws().totalTopics()))));

            // 8. Job applications
            section(doc, "8. Job applications");
            Career c = r.career();
            doc.add(table(new float[]{3, 2}, new String[]{"Measure", "Value"}, List.of(
                    row("Applications", c.applications() + " / " + c.applicationTarget()),
                    row("Responses", c.responses() + " (" + c.responseRate() + "%)"),
                    row("Assessments", String.valueOf(c.assessments())),
                    row("Interviews", String.valueOf(c.interviews())),
                    row("Offers", String.valueOf(c.offers())))));

            // 9. Fitness
            section(doc, "9. Fitness");
            Fitness f = r.fitness();
            doc.add(table(new float[]{3, 2}, new String[]{"Measure", "Value"}, List.of(
                    row("Gym sessions", f.gymSessions() + " / " + f.gymTarget()),
                    row("Gym time", hm(f.gymMinutes())),
                    row("Walking days", f.walkDays() + " / 7"),
                    row("Basketball days", f.basketballDays() + " / 7"),
                    row("Active mornings", f.activeMornings() + " / 7"))));

            // 10. Insights
            section(doc, "10. Weekly insights");
            if (!in.hasEnoughData()) {
                doc.add(body(in.message()));
            } else {
                doc.add(body(in.summary()));
                bullets(doc, "What went well", in.wentWell());
                bullets(doc, "What needs attention", in.needsAttention());
                bullets(doc, "Suggested focus", in.suggestedFocus());
            }

            // 11. Plan
            section(doc, "11. Next week's plan");
            doc.add(body(plan.basis()));
            List<String[]> planRows = new ArrayList<>();
            for (PlanRow p : plan.rows()) {
                planRows.add(row(p.label(), p.key().equals("JOBS") ? p.targetCount() + " applications" : hm(p.targetMinutes())));
            }
            doc.add(table(new float[]{3, 2}, new String[]{"Area", "Target"}, planRows));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BadRequestException("The report could not be generated");
        }
    }

    private PdfPTable solveTable(SolveStats s) {
        return table(new float[]{3, 2}, new String[]{"Measure", "Value"}, List.of(
                row("Attempted", String.valueOf(s.attempted())),
                row("Solved", String.valueOf(s.solved())),
                row("Success rate", s.successRate() + "%"),
                row("Average solve time", s.avgSolveMinutes() > 0 ? hm(s.avgSolveMinutes()) : "-")));
    }

    private static String pct(int part, int whole) { return whole <= 0 ? "-" : JobMetrics.pct(part, whole) + "%"; }

    private static String[] row(String... cells) { return cells; }

    private Paragraph spacer() { return new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 6)); }

    private void section(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, MOSS));
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private Paragraph body(String text) {
        Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 10, INK));
        p.setSpacingAfter(4);
        return p;
    }

    private void bullets(Document doc, String heading, List<String> lines) throws DocumentException {
        doc.add(new Paragraph(heading, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, INK)));
        for (String line : lines) doc.add(body("- " + line));
    }

    private PdfPTable table(float[] widths, String[] header, List<String[]> rows) throws DocumentException {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        Font hf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font bf = FontFactory.getFont(FontFactory.HELVETICA, 9, INK);
        for (String h : header) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hf));
            cell.setBackgroundColor(MOSS);
            cell.setPadding(5);
            cell.setBorderColor(MOSS);
            t.addCell(cell);
        }
        boolean shade = false;
        for (String[] r : rows) {
            for (String v : r) {
                PdfPCell cell = new PdfPCell(new Phrase(v, bf));
                cell.setPadding(5);
                cell.setBorderColor(new Color(0xE2, 0xDB, 0xCF));
                if (shade) cell.setBackgroundColor(SAND);
                t.addCell(cell);
            }
            shade = !shade;
        }
        return t;
    }
}
