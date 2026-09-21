package com.focusforge.service;

import com.focusforge.common.BadRequestException;
import com.focusforge.domain.RoutineKey;
import com.focusforge.domain.WeeklyPlan;
import com.focusforge.dto.ReportDtos.*;
import com.focusforge.repository.WeeklyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class PlanService {
    private static final List<RoutineKey> PLAN_KEYS = new ArrayList<>();
    static {
        PLAN_KEYS.addAll(Defaults.LEARNING_ORDER);
        PLAN_KEYS.add(RoutineKey.GYM);
        PLAN_KEYS.add(RoutineKey.JOBS);
    }

    private final WeeklyPlanRepository plans;
    private final ReportService reports;
    private final Dates dates;

    public PlanService(WeeklyPlanRepository plans, ReportService reports, Dates dates) {
        this.plans = plans;
        this.reports = reports;
        this.dates = dates;
    }

    /** Returns the saved plan for the week, or a proposal derived from last week's actual numbers. */
    @Transactional(readOnly = true)
    public NextWeekPlan get(Long userId, LocalDate weekStartOrNull) {
        LocalDate ws = weekStartOrNull != null ? dates.weekStart(weekStartOrNull) : dates.weekStart(dates.today()).plusWeeks(1);
        Map<RoutineKey, ReportService.Target> defaults = reports.routineTargets(userId);
        List<WeeklyPlan> saved = plans.findByUserIdAndWeekStart(userId, ws);
        boolean isSaved = !saved.isEmpty();

        WeeklyReport last = reports.weekly(userId, ws.minusWeeks(1));
        String basis = isSaved ? "Saved plan for this week."
                : last.hasData() ? "Proposed from your actual numbers last week, kept close to your daily targets."
                : "Based on your daily targets. There is not enough history yet to tailor it.";

        List<PlanRow> rows = new ArrayList<>();
        for (RoutineKey key : PLAN_KEYS) {
            ReportService.Target def = defaults.get(key);
            if (def == null) continue;
            WeeklyPlan sp = saved.stream().filter(p -> p.getPlanKey().equals(key.name())).findFirst().orElse(null);
            if (sp != null) {
                rows.add(new PlanRow(key.name(), Defaults.LABELS.get(key), sp.getTargetMinutes(), sp.getTargetCount()));
            } else if (key == RoutineKey.JOBS) {
                int actual = last.career().applications();
                rows.add(new PlanRow(key.name(), Defaults.LABELS.get(key), 0, propose(actual, def.count(), 5, last.hasData())));
            } else {
                int actual = key == RoutineKey.GYM ? last.fitness().gymMinutes() : ReportService.minutes(last, key.name());
                rows.add(new PlanRow(key.name(), Defaults.LABELS.get(key), propose(actual, def.minutes(), 30, last.hasData()), 0));
            }
        }
        return new NextWeekPlan(ws, isSaved, basis, rows);
    }

    /**
     * Never below 60% of the default, never above the default. Otherwise last week's actual plus 15%,
     * rounded up to a comfortable step, so the plan stays realistic without being discouraging.
     */
    static int propose(int actual, int defaultTarget, int step, boolean hasData) {
        if (!hasData || defaultTarget <= 0) return defaultTarget;
        if (actual >= defaultTarget) return defaultTarget;
        int raw = (int) Math.ceil(actual * 1.15 / step) * step;
        int floor = (int) Math.ceil(defaultTarget * 0.6 / step) * step;
        return Math.min(defaultTarget, Math.max(floor, raw));
    }

    public NextWeekPlan save(Long userId, LocalDate weekStart, PlanRequest req) {
        LocalDate ws = dates.weekStart(weekStart);
        if (req == null || req.rows() == null || req.rows().isEmpty()) throw new BadRequestException("The plan has no rows to save");
        Map<String, WeeklyPlan> existing = new HashMap<>();
        plans.findByUserIdAndWeekStart(userId, ws).forEach(p -> existing.put(p.getPlanKey(), p));
        for (PlanRow row : req.rows()) {
            RoutineKey key;
            try {
                key = RoutineKey.valueOf(row.key());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new BadRequestException("Unknown plan item: " + row.key());
            }
            if (!PLAN_KEYS.contains(key)) throw new BadRequestException("Unknown plan item: " + row.key());
            if (row.targetMinutes() < 0 || row.targetMinutes() > 10080) throw new BadRequestException("Invalid target for " + row.label());
            if (row.targetCount() < 0 || row.targetCount() > 1000) throw new BadRequestException("Invalid target for " + row.label());
            WeeklyPlan p = existing.getOrDefault(key.name(), new WeeklyPlan());
            p.setUserId(userId);
            p.setWeekStart(ws);
            p.setPlanKey(key.name());
            p.setTargetMinutes(row.targetMinutes());
            p.setTargetCount(row.targetCount());
            plans.save(p);
        }
        return get(userId, ws);
    }
}
