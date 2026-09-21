package com.focusforge.service;

import com.focusforge.common.BadRequestException;
import com.focusforge.common.NotFoundException;
import com.focusforge.domain.*;
import com.focusforge.dto.RoutineDtos.*;
import com.focusforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoutineService {
    private final RoutineItemRepository items;
    private final DailyRoutineRepository wakeLogs;
    private final MorningItemRepository morningItems;
    private final MorningCheckRepository morningChecks;
    private final RoutineCompletionRepository completions;
    private final AuthService auth;
    private final ScoreService scores;
    private final Dates dates;

    public RoutineService(RoutineItemRepository items, DailyRoutineRepository wakeLogs, MorningItemRepository morningItems,
                          MorningCheckRepository morningChecks, RoutineCompletionRepository completions,
                          AuthService auth, ScoreService scores, Dates dates) {
        this.items = items;
        this.wakeLogs = wakeLogs;
        this.morningItems = morningItems;
        this.morningChecks = morningChecks;
        this.completions = completions;
        this.auth = auth;
        this.scores = scores;
        this.dates = dates;
    }

    @Transactional(readOnly = true)
    public RoutineDayDto day(Long userId, LocalDate dateOrNull) {
        LocalDate date = dates.orToday(dateOrNull);
        LocalTime target = auth.find(userId).getWakeTargetTime();
        DailyRoutine log = wakeLogs.findByUserIdAndDate(userId, date).orElse(null);
        LocalTime snapshot = log != null ? log.getWakeTarget() : target;
        LocalTime actual = log != null ? log.getWakeActual() : null;
        WakeDto wake = new WakeDto(snapshot, actual,
                actual != null ? ScoreService.minutesBetween(snapshot, actual) : null, actual != null);

        Set<Long> checked = morningChecks.findByUserIdAndDateBetween(userId, date, date).stream()
                .map(MorningCheck::getMorningItemId).collect(Collectors.toSet());
        List<MorningItemDto> morning = morningItems.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId).stream()
                .map(m -> new MorningItemDto(m.getId(), m.getTitle(), checked.contains(m.getId()))).toList();

        List<RoutineItemDto> all = items.findByUserIdOrderBySortOrderAscIdAsc(userId).stream().map(this::toDto).toList();
        return new RoutineDayDto(date, wake, morning, scores.day(userId, date), all);
    }

    public List<RoutineItemDto> list(Long userId) {
        return items.findByUserIdOrderBySortOrderAscIdAsc(userId).stream().map(this::toDto).toList();
    }

    public RoutineItemDto create(Long userId, RoutineItemRequest req) {
        RoutineKey key = req.key() != null ? req.key() : RoutineKey.CUSTOM;
        if (key != RoutineKey.CUSTOM) {
            throw new BadRequestException("Built-in blocks already exist. Edit them, or add a custom block.");
        }
        RoutineItem it = new RoutineItem();
        it.setUserId(userId);
        it.setRoutineKey(RoutineKey.CUSTOM);
        it.setSortOrder(items.findByUserIdOrderBySortOrderAscIdAsc(userId).size());
        apply(it, req);
        return toDto(items.save(it));
    }

    public RoutineItemDto update(Long userId, Long id, RoutineItemRequest req) {
        RoutineItem it = items.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Routine block not found"));
        apply(it, req);
        return toDto(items.save(it));
    }

    /** Custom blocks are deleted; built-in blocks are switched off so history stays intact. */
    public void remove(Long userId, Long id) {
        RoutineItem it = items.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Routine block not found"));
        if (it.getRoutineKey() == RoutineKey.CUSTOM) {
            completions.deleteByUserIdAndRoutineItemId(userId, id);
            items.delete(it);
        } else {
            it.setActive(false);
            items.save(it);
        }
    }

    public void complete(Long userId, Long id, LocalDate dateOrNull) {
        LocalDate date = dates.orToday(dateOrNull);
        items.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Routine block not found"));
        if (completions.findByUserIdAndDateAndRoutineItemId(userId, date, id).isPresent()) return;
        RoutineCompletion c = new RoutineCompletion();
        c.setUserId(userId);
        c.setDate(date);
        c.setRoutineItemId(id);
        c.setCompletedAt(Instant.now());
        completions.save(c);
    }

    public void uncomplete(Long userId, Long id, LocalDate dateOrNull) {
        completions.findByUserIdAndDateAndRoutineItemId(userId, dates.orToday(dateOrNull), id).ifPresent(completions::delete);
    }

    public WakeDto setWake(Long userId, WakeRequest req) {
        LocalDate date = dates.orToday(req.date());
        DailyRoutine log = wakeLogs.findByUserIdAndDate(userId, date).orElseGet(() -> {
            DailyRoutine r = new DailyRoutine();
            r.setUserId(userId);
            r.setDate(date);
            r.setWakeTarget(auth.find(userId).getWakeTargetTime());
            return r;
        });
        log.setWakeActual(req.actual());
        wakeLogs.save(log);
        LocalTime actual = log.getWakeActual();
        return new WakeDto(log.getWakeTarget(), actual,
                actual != null ? ScoreService.minutesBetween(log.getWakeTarget(), actual) : null, actual != null);
    }

    public MorningItemDto addMorningItem(Long userId, MorningItemRequest req) {
        MorningItem m = new MorningItem();
        m.setUserId(userId);
        m.setTitle(req.title().trim());
        m.setSortOrder(morningItems.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId).size());
        m = morningItems.save(m);
        return new MorningItemDto(m.getId(), m.getTitle(), false);
    }

    public void removeMorningItem(Long userId, Long id) {
        MorningItem m = morningItems.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Checklist item not found"));
        m.setActive(false);
        morningItems.save(m);
    }

    public void setMorningCheck(Long userId, MorningCheckRequest req) {
        if (req.itemId() == null) throw new BadRequestException("Choose a checklist item");
        morningItems.findByIdAndUserId(req.itemId(), userId).orElseThrow(() -> new NotFoundException("Checklist item not found"));
        LocalDate date = dates.orToday(req.date());
        var existing = morningChecks.findByUserIdAndDateAndMorningItemId(userId, date, req.itemId());
        if (req.done() && existing.isEmpty()) {
            MorningCheck c = new MorningCheck();
            c.setUserId(userId);
            c.setDate(date);
            c.setMorningItemId(req.itemId());
            morningChecks.save(c);
        } else if (!req.done()) {
            existing.ifPresent(morningChecks::delete);
        }
    }

    private void apply(RoutineItem it, RoutineItemRequest req) {
        it.setTitle(req.title().trim());
        it.setTargetMinutes(req.targetMinutes());
        it.setTargetCount(req.targetCount());
        it.setScheduledTime(req.scheduledTime());
        if (req.active() != null) it.setActive(req.active());
    }

    private RoutineItemDto toDto(RoutineItem i) {
        return new RoutineItemDto(i.getId(), i.getRoutineKey(), i.getTitle(), i.getTargetMinutes(), i.getTargetCount(),
                i.getScheduledTime(), i.isActive(), i.getSortOrder());
    }
}
