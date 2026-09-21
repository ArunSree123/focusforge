package com.focusforge.service;

import com.focusforge.domain.*;
import com.focusforge.dto.ReportDtos.*;
import com.focusforge.dto.ScoreDtos.DayScore;
import com.focusforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Builds weekly reports and the week-over-week comparison from recorded data only. */
@Service
@Transactional(readOnly = true)
public class ReportService {
    public record Target(int minutes, int count) {}

    private final StudySessionRepository sessions;
    private final FitnessSessionRepository fitness;
    private final JobApplicationRepository jobs;
    private final ProblemRepository problems;
    private final TopicProgressRepository topics;
    private final RoutineItemRepository routineItems;
    private final WeeklyPlanRepository plans;
    private final ScoreService scores;
    private final Dates dates;

    public ReportService(StudySessionRepository sessions, FitnessSessionRepository fitness, JobApplicationRepository jobs,
                         ProblemRepository problems, TopicProgressRepository topics, RoutineItemRepository routineItems,
                         WeeklyPlanRepository plans, ScoreService scores, Dates dates) {
        this.sessions = sessions;
        this.fitness = fitness;
        this.jobs = jobs;
        this.problems = problems;
        this.topics = topics;
        this.routineItems = routineItems;
        this.plans = plans;
        this.scores = scores;
        this.dates = dates;
    }

    public WeeklyReport weekly(Long userId, LocalDate anyDay) {
        LocalDate ws = dates.weekStartOrCurrent(anyDay);
        int streak = scores.streak(userId, dates.today());
        WeeklyReport cur = core(userId, ws, streak);
        WeeklyReport prev = core(userId, ws.minusWeeks(1), streak);
        return new WeeklyReport(cur.weekStart(), cur.weekEnd(), cur.hasData(), cur.overall(), cur.learning(), cur.career(),
                cur.fitness(), cur.problemSolving(), cur.dsa(), cur.sql(), cur.java(), cur.aws(), cur.days(),
                compare(cur, prev));
    }

    public MonthlyAnalytics monthly(Long userId, String month) {
        java.time.YearMonth ym = month == null || month.isBlank() ? java.time.YearMonth.from(dates.today()) : java.time.YearMonth.parse(month);
        LocalDate from = ym.atDay(1), to = ym.atEndOfMonth();
        Map<LocalDate, DayScore> range = scores.range(userId, from, to);
        List<MonthPoint> pts = new ArrayList<>();
        int study = 0, gym = 0, apps = 0, solved = 0, active = 0, pctSum = 0, counted = 0;
        LocalDate today = dates.today();
        for (DayScore s : range.values()) {
            pts.add(new MonthPoint(s.date(), s.studyMinutes(), s.gymMinutes(), s.jobsApplied(), s.problemsSolved(), s.overallPercent()));
            study += s.studyMinutes();
            gym += s.gymMinutes();
            apps += s.jobsApplied();
            solved += s.problemsSolved();
            if (s.overallPercent() >= Defaults.ACTIVE_DAY_PERCENT) active++;
            if (!s.date().isAfter(today)) { pctSum += s.overallPercent(); counted++; }
        }
        return new MonthlyAnalytics(ym.toString(), pts, study, gym, apps, solved, active, counted == 0 ? 0 : pctSum / counted);
    }

    /** Targets for a week: saved plan rows override the daily routine targets multiplied by seven. */
    public Map<RoutineKey, Target> targets(Long userId, LocalDate weekStart) {
        Map<RoutineKey, Target> t = routineTargets(userId);
        for (WeeklyPlan p : plans.findByUserIdAndWeekStart(userId, weekStart)) {
            t.put(RoutineKey.valueOf(p.getPlanKey()), new Target(p.getTargetMinutes(), p.getTargetCount()));
        }
        return t;
    }

    /** Weekly targets implied by the daily routine (daily target x 7). */
    public Map<RoutineKey, Target> routineTargets(Long userId) {
        Map<RoutineKey, Target> t = new EnumMap<>(RoutineKey.class);
        for (RoutineItem it : routineItems.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId)) {
            if (it.getRoutineKey() == RoutineKey.CUSTOM) continue;
            t.put(it.getRoutineKey(), new Target(it.getTargetMinutes() * 7, it.getTargetCount() * 7));
        }
        return t;
    }

    private WeeklyReport core(Long userId, LocalDate ws, int streak) {
        LocalDate we = ws.plusDays(6);
        LocalDate today = dates.today();
        List<StudySession> sess = sessions.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, ws, we);
        List<FitnessSession> fit = fitness.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, ws, we);
        List<JobApplication> apps = jobs.findByUserIdAndDateAppliedBetween(userId, ws, we).stream()
                .filter(j -> JobMetrics.isApplied(j.getStatus())).toList();
        List<Problem> probs = problems.findByUserIdAndDateBetween(userId, ws, we);
        List<TopicProgress> doneTopics = topics.findByUserIdAndStatusAndCompletedOnBetween(userId, TopicStatus.COMPLETED, ws, we);
        Map<LocalDate, DayScore> dayScores = scores.range(userId, ws, we);
        Map<RoutineKey, Target> targets = targets(userId, ws);

        Map<StudyCategory, Integer> byCat = new EnumMap<>(StudyCategory.class);
        sess.forEach(s -> byCat.merge(s.getCategory(), s.getDurationMinutes(), Integer::sum));
        int gymMinutes = fit.stream().filter(f -> f.getType() == ActivityType.GYM).mapToInt(FitnessSession::getDurationMinutes).sum();
        int totalStudy = byCat.values().stream().mapToInt(Integer::intValue).sum();

        // Learning rows + overall planned/completed
        List<CategoryRow> learning = new ArrayList<>();
        int planned = 0, completed = 0;
        for (RoutineKey key : Defaults.LEARNING_ORDER) {
            Target t = targets.get(key);
            if (t == null) continue;
            int actual = byCat.getOrDefault(StudyCategory.valueOf(key.name()), 0);
            learning.add(new CategoryRow(key.name(), Defaults.LABELS.get(key), t.minutes(), actual, JobMetrics.pct(actual, t.minutes())));
            planned += t.minutes();
            completed += Math.min(actual, t.minutes());
        }
        Target gymT = targets.get(RoutineKey.GYM);
        if (gymT != null) {
            planned += gymT.minutes();
            completed += Math.min(gymMinutes, gymT.minutes());
        }

        long elapsed = today.isBefore(ws) ? 0 : Math.min(7, ChronoUnit.DAYS.between(ws, today) + 1);
        int avg = elapsed <= 0 ? 0 : (int) Math.round(totalStudy / (double) elapsed);

        List<DayPoint> days = new ArrayList<>();
        LocalDate best = null;
        int bestMinutes = 0;
        for (LocalDate d = ws; !d.isAfter(we); d = d.plusDays(1)) {
            Map<String, Integer> perCat = new LinkedHashMap<>();
            for (StudyCategory c : StudyCategory.values()) perCat.put(c.name(), 0);
            int dayStudy = 0, dayGym = 0;
            for (StudySession s : sess) if (s.getDate().equals(d)) {
                perCat.merge(s.getCategory().name(), s.getDurationMinutes(), Integer::sum);
                dayStudy += s.getDurationMinutes();
            }
            for (FitnessSession f : fit) if (f.getDate().equals(d) && f.getType() == ActivityType.GYM) dayGym += f.getDurationMinutes();
            perCat.put("GYM", dayGym);
            DayScore ds = dayScores.get(d);
            final LocalDate day = d;
            int dayApps = (int) apps.stream().filter(j -> j.getDateApplied().equals(day)).count();
            days.add(new DayPoint(d, perCat, dayStudy, dayGym, dayApps, ds.problemsSolved(), ds.overallPercent()));
            if (dayStudy > bestMinutes) { bestMinutes = dayStudy; best = d; }
        }

        int longest = sess.stream().mapToInt(StudySession::getDurationMinutes).max().orElse(0);
        Overall overall = new Overall(planned, completed, JobMetrics.pct(completed, planned), avg, best, bestMinutes,
                longest, streak, totalStudy);

        int responses = (int) apps.stream().filter(j -> JobMetrics.isResponse(j.getStatus())).count();
        int assessments = (int) apps.stream().filter(j -> j.getStatus() == JobStatus.ASSESSMENT).count();
        int interviews = (int) apps.stream().filter(j -> j.getStatus() == JobStatus.INTERVIEW || j.getStatus() == JobStatus.HR_ROUND).count();
        int offers = (int) apps.stream().filter(j -> j.getStatus() == JobStatus.OFFER).count();
        Target jobT = targets.get(RoutineKey.JOBS);
        Career career = new Career(apps.size(), jobT == null ? 0 : jobT.count(), responses, assessments, interviews, offers,
                JobMetrics.pct(responses, apps.size()));

        int gymSessions = (int) fit.stream().filter(f -> f.getType() == ActivityType.GYM).count();
        int walk = (int) fit.stream().filter(f -> f.getType() == ActivityType.WALK).map(FitnessSession::getDate).distinct().count();
        int ball = (int) fit.stream().filter(f -> f.getType() == ActivityType.BASKETBALL).map(FitnessSession::getDate).distinct().count();
        int activeMornings = (int) dayScores.values().stream().filter(DayScore::activeMorning).count();
        Fitness fitnessSection = new Fitness(gymSessions, Defaults.WEEKLY_GYM_SESSIONS, gymMinutes, walk, ball, activeMornings);

        List<Problem> dsaProbs = probs.stream().filter(p -> p.getKind() == ProblemKind.DSA).toList();
        List<Problem> dsaSolved = dsaProbs.stream().filter(Problem::isSolved).toList();
        Dsa dsa = new Dsa(dsaSolved.size(), diff(dsaSolved, Difficulty.EASY), diff(dsaSolved, Difficulty.MEDIUM),
                diff(dsaSolved, Difficulty.HARD),
                (int) doneTopics.stream().filter(t -> t.getArea() == TopicArea.DSA).count(),
                problems.countByUserIdAndKindAndSolvedTrue(userId, ProblemKind.DSA));

        List<TopicProgress> awsAll = topics.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, TopicArea.AWS);
        int awsTopicsTotal = (int) awsAll.stream().filter(t -> !TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())).count();
        int awsTopicsDone = (int) awsAll.stream().filter(t -> !TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())
                && t.getStatus() == TopicStatus.COMPLETED).count();
        int awsWeekTopics = (int) doneTopics.stream().filter(t -> t.getArea() == TopicArea.AWS
                && !TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())).count();
        int awsWeekHands = (int) doneTopics.stream().filter(t -> t.getArea() == TopicArea.AWS
                && TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())).count();

        boolean hasData = totalStudy > 0 || !apps.isEmpty() || !probs.isEmpty() || !fit.isEmpty();
        return new WeeklyReport(ws, we, hasData, overall, learning, career, fitnessSection,
                solve(probs, ProblemKind.REASONING), dsa, solve(probs, ProblemKind.SQL), solve(probs, ProblemKind.JAVA),
                new Aws(awsWeekTopics, awsWeekHands, awsTopicsDone, awsTopicsTotal), days, List.of());
    }

    private int diff(List<Problem> list, Difficulty d) {
        return (int) list.stream().filter(p -> p.getDifficulty() == d).count();
    }

    private SolveStats solve(List<Problem> all, ProblemKind kind) {
        List<Problem> ofKind = all.stream().filter(p -> p.getKind() == kind).toList();
        int solved = (int) ofKind.stream().filter(Problem::isSolved).count();
        double avg = ofKind.stream().filter(p -> p.isSolved() && p.getTimeMinutes() > 0).mapToInt(Problem::getTimeMinutes).average().orElse(0);
        return new SolveStats(ofKind.size(), solved, JobMetrics.pct(solved, ofKind.size()), (int) Math.round(avg));
    }

    private List<Comparison> compare(WeeklyReport cur, WeeklyReport prev) {
        List<Comparison> rows = new ArrayList<>();
        rows.add(row("STUDY", "Study time", "minutes", prev.overall().totalStudyMinutes(), cur.overall().totalStudyMinutes()));
        for (String key : List.of("JAVA", "DSA", "SQL", "AWS", "PROBLEM_SOLVING")) {
            String label = Defaults.LABELS.get(RoutineKey.valueOf(key));
            rows.add(row(key, label, "minutes", minutes(prev, key), minutes(cur, key)));
        }
        rows.add(row("JOBS", "Job applications", "count", prev.career().applications(), cur.career().applications()));
        rows.add(row("GYM", "Gym", "minutes", prev.fitness().gymMinutes(), cur.fitness().gymMinutes()));
        rows.add(row("WALK", "Walking", "days", prev.fitness().walkDays(), cur.fitness().walkDays()));
        rows.add(row("BASKETBALL", "Basketball", "days", prev.fitness().basketballDays(), cur.fitness().basketballDays()));
        return rows;
    }

    static int minutes(WeeklyReport r, String key) {
        return r.learning().stream().filter(c -> c.key().equals(key)).mapToInt(CategoryRow::actualMinutes).sum();
    }

    /** Neutral wording only: it states the direction and size of change and never judges it. */
    private Comparison row(String key, String label, String unit, int last, int now) {
        Double change = last > 0 ? Math.round((now - last) * 1000.0 / last) / 10.0 : null;
        String note;
        if (last == 0 && now == 0) note = "No activity recorded in either week";
        else if (last == 0) note = "No baseline from last week";
        else if (now == last) note = "Same as last week";
        else note = (now > last ? "Up " : "Down ") + Math.abs(change) + "% from last week";
        return new Comparison(key, label, unit, last, now, change, note);
    }
}
