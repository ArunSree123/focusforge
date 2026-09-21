package com.focusforge.service;

import com.focusforge.common.BadRequestException;
import com.focusforge.common.NotFoundException;
import com.focusforge.domain.ActivityType;
import com.focusforge.domain.Exercise;
import com.focusforge.domain.FitnessSession;
import com.focusforge.dto.FitnessDtos.*;
import com.focusforge.dto.ScoreDtos.DayScore;
import com.focusforge.repository.FitnessSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/** Activity tracking only. Nothing here gives medical or training advice. */
@Service
@Transactional
public class FitnessService {
    private final FitnessSessionRepository repo;
    private final ScoreService scores;
    private final Dates dates;

    public FitnessService(FitnessSessionRepository repo, ScoreService scores, Dates dates) {
        this.repo = repo;
        this.scores = scores;
        this.dates = dates;
    }

    @Transactional(readOnly = true)
    public List<FitnessDto> list(Long userId, LocalDate from, LocalDate to) {
        LocalDate end = dates.orToday(to);
        LocalDate start = from != null ? from : end.minusDays(29);
        return repo.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, start, end).stream().map(this::toDto).toList();
    }

    public FitnessDto create(Long userId, FitnessRequest req) {
        FitnessSession s = new FitnessSession();
        s.setUserId(userId);
        apply(s, req);
        return toDto(repo.save(s));
    }

    public FitnessDto update(Long userId, Long id, FitnessRequest req) {
        FitnessSession s = repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Activity not found"));
        apply(s, req);
        return toDto(repo.save(s));
    }

    public void delete(Long userId, Long id) {
        repo.delete(repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Activity not found")));
    }

    @Transactional(readOnly = true)
    public FitnessSummary summary(Long userId, LocalDate anyDayInWeek) {
        LocalDate start = dates.weekStartOrCurrent(anyDayInWeek);
        LocalDate end = start.plusDays(6);
        List<FitnessSession> week = repo.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, start, end);
        int gymSessions = (int) week.stream().filter(f -> f.getType() == ActivityType.GYM).count();
        int gymMinutes = week.stream().filter(f -> f.getType() == ActivityType.GYM).mapToInt(FitnessSession::getDurationMinutes).sum();
        int walk = (int) week.stream().filter(f -> f.getType() == ActivityType.WALK).map(FitnessSession::getDate).distinct().count();
        int ball = (int) week.stream().filter(f -> f.getType() == ActivityType.BASKETBALL).map(FitnessSession::getDate).distinct().count();
        int active = (int) scores.range(userId, start, end).values().stream().filter(DayScore::activeMorning).count();
        return new FitnessSummary(start, gymSessions, Defaults.WEEKLY_GYM_SESSIONS, gymMinutes, walk, ball, active);
    }

    private void apply(FitnessSession s, FitnessRequest r) {
        int minutes = r.durationMinutes();
        if (minutes == 0 && r.startTime() != null && r.endTime() != null) {
            long diff = Duration.between(r.startTime(), r.endTime()).toMinutes();
            minutes = (int) (diff < 0 ? diff + 1440 : diff);
        }
        if (minutes <= 0) throw new BadRequestException("Duration must be greater than zero");

        s.setType(r.type());
        s.setDate(dates.orToday(r.date()));
        s.setStartTime(r.startTime());
        s.setEndTime(r.endTime());
        s.setDurationMinutes(minutes);
        s.setWorkoutType(r.type() == ActivityType.GYM ? r.workoutType() : null);
        s.setDistanceKm(r.distanceKm());
        s.setSteps(r.steps());
        s.setNotes(StudySessionService.blankToNull(r.notes()));

        s.getExercises().clear();
        if (r.type() == ActivityType.GYM && r.exercises() != null) {
            int order = 0;
            for (ExerciseDto e : r.exercises()) {
                Exercise ex = new Exercise();
                ex.setSession(s);
                ex.setName(e.name().trim());
                ex.setSets(e.sets());
                ex.setReps(e.reps());
                ex.setWeightKg(e.weightKg());
                ex.setSortOrder(order++);
                s.getExercises().add(ex);
            }
        }
    }

    private FitnessDto toDto(FitnessSession s) {
        List<ExerciseDto> ex = s.getExercises().stream()
                .map(e -> new ExerciseDto(e.getName(), e.getSets(), e.getReps(), e.getWeightKg())).toList();
        return new FitnessDto(s.getId(), s.getType(), s.getDate(), s.getStartTime(), s.getEndTime(), s.getDurationMinutes(),
                s.getWorkoutType(), s.getDistanceKm(), s.getSteps(), s.getNotes(), ex, s.isDemo());
    }
}
