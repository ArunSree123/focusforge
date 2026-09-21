package com.focusforge.service;

import com.focusforge.dto.ReportDtos.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Every sentence below is built from numbers in the report; nothing is estimated or invented. */
@Component
public class RuleBasedInsightGenerator implements InsightGenerator {
    private final Dates dates;

    public RuleBasedInsightGenerator(Dates dates) { this.dates = dates; }

    static final String NOT_ENOUGH = "Not enough data to generate a reliable insight yet.";

    public static String hm(int minutes) {
        int h = minutes / 60, m = minutes % 60;
        if (h == 0) return m + "m";
        return m == 0 ? h + "h" : h + "h " + m + "m";
    }

    @Override
    public Insights generate(WeeklyReport r) {
        boolean anyPast = r.comparison().stream().anyMatch(c -> c.lastWeek() > 0);
        int fitnessDays = r.fitness().gymSessions() + r.fitness().walkDays() + r.fitness().basketballDays();
        boolean enough = r.overall().totalStudyMinutes() >= 60 || r.career().applications() >= 3 || fitnessDays > 0;
        if (!enough) {
            return new Insights(false, NOT_ENOUGH, NOT_ENOUGH, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                    "rule-based", Instant.now());
        }

        List<String> well = new ArrayList<>(), attention = new ArrayList<>(), pattern = new ArrayList<>(),
                career = new ArrayList<>(), fitness = new ArrayList<>(), focus = new ArrayList<>();

        List<CategoryRow> withTarget = r.learning().stream().filter(c -> c.targetMinutes() > 0).toList();
        for (CategoryRow c : withTarget) {
            if (c.completionPct() >= 90) {
                well.add(c.label() + " reached " + c.completionPct() + "% of its target (" + hm(c.actualMinutes()) + " of " + hm(c.targetMinutes()) + ").");
            }
        }
        r.comparison().stream().filter(c -> c.key().equals("STUDY") && c.changePct() != null && c.changePct() >= 10).findFirst()
                .ifPresent(c -> well.add("Total study time was " + hm(c.thisWeek()) + " compared with " + hm(c.lastWeek()) + " last week."));
        if (r.career().applicationTarget() > 0 && r.career().applications() >= 0.9 * r.career().applicationTarget()) {
            well.add("You submitted " + r.career().applications() + " of " + r.career().applicationTarget() + " planned applications.");
        }
        if (r.overall().streak() >= 3) well.add("You are on a " + r.overall().streak() + "-day consistency streak.");
        if (r.fitness().gymSessions() >= r.fitness().gymTarget() && r.fitness().gymTarget() > 0) {
            well.add("You completed all " + r.fitness().gymTarget() + " planned gym sessions.");
        }
        if (well.isEmpty()) well.add("No category reached 90% of its target this week; the sections below show where your time went.");

        withTarget.stream().filter(c -> c.completionPct() < 60)
                .sorted(Comparator.comparingDouble(CategoryRow::completionPct))
                .forEach(c -> attention.add(c.label() + " received " + hm(c.actualMinutes()) + " against a " + hm(c.targetMinutes()) + " target (" + c.completionPct() + "%)."));
        long emptyDays = r.days().stream().filter(d -> d.studyMinutes() == 0 && !d.date().isAfter(dates.today())).count();
        if (emptyDays > 0 && r.days().stream().anyMatch(d -> d.studyMinutes() > 0)) {
            attention.add(emptyDays + (emptyDays == 1 ? " day" : " days") + " had no study time logged.");
        }
        if (r.career().applications() >= 20 && r.career().responses() == 0) {
            attention.add("No responses are recorded yet across " + r.career().applications() + " applications; it may be worth reviewing which resume versions you are using.");
        }
        if (attention.isEmpty()) attention.add("Nothing stood out as falling well below target this week.");

        withTarget.stream().max(Comparator.comparingDouble(CategoryRow::completionPct))
                .ifPresent(c -> pattern.add("Your strongest area against target was " + c.label() + " at " + c.completionPct() + "%."));
        if (r.overall().bestDay() != null) {
            pattern.add(r.overall().bestDay().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    + " was your highest study day with " + hm(r.overall().bestDayMinutes()) + ".");
        }
        pattern.add("You averaged " + hm(r.overall().avgStudyMinutesPerDay()) + " of study per day, and your longest single session was "
                + hm(r.overall().longestSessionMinutes()) + ".");

        career.add("You submitted " + r.career().applications() + " applications"
                + (r.career().applicationTarget() > 0 ? " against a target of " + r.career().applicationTarget() : "") + ".");
        r.comparison().stream().filter(c -> c.key().equals("JOBS") && c.lastWeek() > 0).findFirst()
                .ifPresent(c -> career.add("Last week you submitted " + c.lastWeek() + ". " + c.note() + "."));
        career.add("Responses: " + r.career().responses() + " (" + r.career().responseRate() + "%), assessments: "
                + r.career().assessments() + ", interviews: " + r.career().interviews() + ", offers: " + r.career().offers() + ".");

        fitness.add("Gym: " + r.fitness().gymSessions() + " of " + r.fitness().gymTarget() + " sessions, " + hm(r.fitness().gymMinutes()) + " in total.");
        fitness.add("Walking on " + r.fitness().walkDays() + " days and basketball on " + r.fitness().basketballDays() + " days.");
        fitness.add("Active morning checklist fully completed on " + r.fitness().activeMornings() + " days.");

        withTarget.stream().sorted(Comparator.comparingDouble(CategoryRow::completionPct)).limit(2)
                .filter(c -> c.completionPct() < 90)
                .forEach(c -> focus.add("Make a little more room for " + c.label() + " next week; it reached " + c.completionPct() + "% this week."));
        if (r.career().applicationTarget() > 0 && r.career().applications() < r.career().applicationTarget()) {
            focus.add("Keep the application pace steady: " + r.career().applications() + " of " + r.career().applicationTarget() + " this week.");
        }
        if (focus.isEmpty()) focus.add("Keep the current pace; every learning category is close to its target.");
        focus.add("Review the suggested plan for next week and adjust it to fit your schedule.");

        StringBuilder sum = new StringBuilder("You logged " + hm(r.overall().totalStudyMinutes()) + " of study this week");
        withTarget.stream().max(Comparator.comparingDouble(CategoryRow::completionPct))
                .ifPresent(c -> sum.append(", with ").append(c.label()).append(" closest to its target"));
        withTarget.stream().min(Comparator.comparingDouble(CategoryRow::completionPct))
                .filter(c -> c.completionPct() < 90 && withTarget.size() > 1)
                .ifPresent(c -> sum.append(" while ").append(c.label()).append(" received less time than planned"));
        sum.append(". You submitted ").append(r.career().applications()).append(" applications");
        r.comparison().stream().filter(c -> c.key().equals("JOBS") && c.lastWeek() > 0).findFirst()
                .ifPresent(c -> sum.append(" (").append(c.lastWeek()).append(" last week)"));
        sum.append(".");
        if (!anyPast) sum.append(" There is no data from last week yet, so no comparison is shown.");

        return new Insights(true, null, sum.toString(), well, attention, pattern, career, fitness, focus, "rule-based", Instant.now());
    }
}
