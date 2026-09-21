package com.focusforge.service;

import com.focusforge.domain.*;
import com.focusforge.dto.ScoreDtos.DayScore;
import com.focusforge.dto.ScoreDtos.ItemScore;
import com.focusforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Computes daily scorecards. Loads everything for a date range in a handful of queries
 * and then derives each day in memory, so week/month views never trigger N+1 queries.
 */
@Service
@Transactional(readOnly = true)
public class ScoreService {
    private final RoutineItemRepository routineItems;
    private final StudySessionRepository sessions;
    private final FitnessSessionRepository fitness;
    private final JobApplicationRepository jobs;
    private final RoutineCompletionRepository completions;
    private final DailyRoutineRepository wakeLogs;
    private final MorningItemRepository morningItems;
    private final MorningCheckRepository morningChecks;
    private final ProblemRepository problems;
    private final TopicProgressRepository topics;
    private final Dates dates;

    public ScoreService(RoutineItemRepository routineItems, StudySessionRepository sessions,
                        FitnessSessionRepository fitness, JobApplicationRepository jobs,
                        RoutineCompletionRepository completions, DailyRoutineRepository wakeLogs,
                        MorningItemRepository morningItems, MorningCheckRepository morningChecks,
                        ProblemRepository problems, TopicProgressRepository topics, Dates dates) {
        this.routineItems = routineItems;
        this.sessions = sessions;
        this.fitness = fitness;
        this.jobs = jobs;
        this.completions = completions;
        this.wakeLogs = wakeLogs;
        this.morningItems = morningItems;
        this.morningChecks = morningChecks;
        this.problems = problems;
        this.topics = topics;
        this.dates = dates;
    }

    public DayScore day(Long userId, LocalDate date) {
        return range(userId, date, date).get(date);
    }

    /** Streak = consecutive active days ending today (or yesterday if today is not yet active). */
    public int streak(Long userId, LocalDate today) {
        Map<LocalDate, DayScore> scores = range(userId, today.minusDays(59), today);
        LocalDate cursor = today;
        if (scores.get(cursor).overallPercent() < Defaults.ACTIVE_DAY_PERCENT) cursor = cursor.minusDays(1);
        int streak = 0;
        while (!cursor.isBefore(today.minusDays(59)) && scores.get(cursor).overallPercent() >= Defaults.ACTIVE_DAY_PERCENT) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public Map<LocalDate, DayScore> range(Long userId, LocalDate from, LocalDate to) {
        List<RoutineItem> items = routineItems.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId);
        List<MorningItem> morning = morningItems.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId);

        Map<LocalDate, Map<StudyCategory, Integer>> study = new HashMap<>();
        for (StudySession s : sessions.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, from, to)) {
            study.computeIfAbsent(s.getDate(), d -> new EnumMap<>(StudyCategory.class))
                    .merge(s.getCategory(), s.getDurationMinutes(), Integer::sum);
        }

        Map<LocalDate, Map<ActivityType, Integer>> fit = new HashMap<>();
        for (FitnessSession f : fitness.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, from, to)) {
            fit.computeIfAbsent(f.getDate(), d -> new EnumMap<>(ActivityType.class))
                    .merge(f.getType(), Math.max(f.getDurationMinutes(), 1), Integer::sum);
        }

        Map<LocalDate, Integer> applied = new HashMap<>();
        for (JobApplication j : jobs.findByUserIdAndDateAppliedBetween(userId, from, to)) {
            if (JobMetrics.isApplied(j.getStatus())) applied.merge(j.getDateApplied(), 1, Integer::sum);
        }

        Set<String> manual = new HashSet<>();
        for (RoutineCompletion c : completions.findByUserIdAndDateBetween(userId, from, to)) {
            manual.add(c.getDate() + ":" + c.getRoutineItemId());
        }

        Map<LocalDate, DailyRoutine> wake = new HashMap<>();
        for (DailyRoutine r : wakeLogs.findByUserIdAndDateBetween(userId, from, to)) wake.put(r.getDate(), r);

        Map<LocalDate, Set<Long>> checks = new HashMap<>();
        for (MorningCheck c : morningChecks.findByUserIdAndDateBetween(userId, from, to)) {
            checks.computeIfAbsent(c.getDate(), d -> new HashSet<>()).add(c.getMorningItemId());
        }

        Map<LocalDate, Integer> solved = new HashMap<>();
        for (Problem p : problems.findByUserIdAndDateBetween(userId, from, to)) {
            if (p.isSolved()) solved.merge(p.getDate(), 1, Integer::sum);
        }

        Map<LocalDate, Integer> awsDone = new HashMap<>();
        for (TopicProgress t : topics.findByUserIdAndStatusAndCompletedOnBetween(userId, TopicStatus.COMPLETED, from, to)) {
            if (t.getArea() == TopicArea.AWS && !TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())) {
                awsDone.merge(t.getCompletedOn(), 1, Integer::sum);
            }
        }

        LocalDate today = dates.today();
        Map<LocalDate, DayScore> result = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            result.put(d, build(d, today, items, morning, study.getOrDefault(d, Map.of()), fit.getOrDefault(d, Map.of()),
                    applied.getOrDefault(d, 0), manual, wake.get(d), checks.getOrDefault(d, Set.of()),
                    solved.getOrDefault(d, 0), awsDone.getOrDefault(d, 0)));
        }
        return result;
    }

    private DayScore build(LocalDate d, LocalDate today, List<RoutineItem> items, List<MorningItem> morning,
                           Map<StudyCategory, Integer> study, Map<ActivityType, Integer> fit, int applied,
                           Set<String> manual, DailyRoutine wake, Set<Long> checked, int solved, int awsDone) {
        List<ItemScore> scores = new ArrayList<>();

        int checkedCount = (int) morning.stream().filter(m -> checked.contains(m.getId())).count();
        boolean wakeLogged = wake != null && wake.getWakeActual() != null;
        double morningRatio = (double) ((wakeLogged ? 1 : 0) + checkedCount) / (1 + morning.size());
        scores.add(new ItemScore(null, "MORNING", "Morning routine", "MORNING", 0, 0, 0, 0,
                morningRatio, morningRatio >= 1.0, false, wake != null ? wake.getWakeTarget() : null));

        int planned = 0, completedMin = 0;
        for (RoutineItem it : items) {
            boolean isManual = manual.contains(d + ":" + it.getId());
            ItemScore s;
            switch (it.getRoutineKey()) {
                case JOBS -> {
                    int target = it.getTargetCount();
                    double ratio = isManual ? 1 : target > 0 ? Math.min(1.0, applied / (double) target) : (applied > 0 ? 1 : 0);
                    s = new ItemScore(it.getId(), "JOBS", it.getTitle(), "COUNT", 0, 0, target, applied, ratio,
                            ratio >= 1.0, isManual, it.getScheduledTime());
                }
                case CUSTOM -> s = new ItemScore(it.getId(), "CUSTOM", it.getTitle(), "CUSTOM", 0, 0, 0, 0,
                        isManual ? 1 : 0, isManual, isManual, it.getScheduledTime());
                default -> {
                    int actual = it.getRoutineKey() == RoutineKey.GYM
                            ? fit.getOrDefault(ActivityType.GYM, 0)
                            : study.getOrDefault(StudyCategory.valueOf(it.getRoutineKey().name()), 0);
                    int target = it.getTargetMinutes();
                    double ratio = isManual ? 1 : target > 0 ? Math.min(1.0, actual / (double) target) : (actual > 0 ? 1 : 0);
                    planned += target;
                    completedMin += isManual ? target : Math.min(actual, target);
                    s = new ItemScore(it.getId(), it.getRoutineKey().name(), it.getTitle(), "TIME", target, actual, 0, 0,
                            ratio, ratio >= 1.0, isManual, it.getScheduledTime());
                }
            }
            scores.add(s);
        }

        int done = (int) scores.stream().filter(ItemScore::completed).count();
        int remaining = scores.size() - done;
        double mean = scores.stream().mapToDouble(ItemScore::ratio).average().orElse(0);
        int studyTotal = study.values().stream().mapToInt(Integer::intValue).sum();
        return new DayScore(d, scores, (int) Math.round(mean * 100), planned, completedMin, studyTotal,
                fit.getOrDefault(ActivityType.GYM, 0), done, remaining, d.isBefore(today) ? remaining : 0,
                solved, applied, awsDone, fit.containsKey(ActivityType.WALK), fit.containsKey(ActivityType.BASKETBALL),
                !morning.isEmpty() && checkedCount == morning.size());
    }

    /** Minutes between two clock times (used for wake-up drift). */
    public static int minutesBetween(java.time.LocalTime from, java.time.LocalTime to) {
        return (int) Duration.between(from, to).toMinutes();
    }
}
