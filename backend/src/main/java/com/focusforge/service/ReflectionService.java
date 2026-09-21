package com.focusforge.service;

import com.focusforge.domain.DailyReflection;
import com.focusforge.dto.ReflectionDtos.*;
import com.focusforge.repository.DailyReflectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ReflectionService {
    private final DailyReflectionRepository repo;
    private final Dates dates;

    public ReflectionService(DailyReflectionRepository repo, Dates dates) {
        this.repo = repo;
        this.dates = dates;
    }

    @Transactional(readOnly = true)
    public ReflectionDto get(Long userId, LocalDate dateOrNull) {
        LocalDate date = dates.orToday(dateOrNull);
        return repo.findByUserIdAndDate(userId, date).map(this::toDto)
                .orElse(new ReflectionDto(date, null, null, null, null, null, null, false));
    }

    public ReflectionDto save(Long userId, ReflectionRequest r) {
        LocalDate date = dates.orToday(r.date());
        DailyReflection d = repo.findByUserIdAndDate(userId, date).orElseGet(() -> {
            DailyReflection n = new DailyReflection();
            n.setUserId(userId);
            n.setDate(date);
            return n;
        });
        d.setMood(r.mood());
        d.setEnergy(r.energy());
        d.setFocus(r.focus());
        d.setWentWell(StudySessionService.blankToNull(r.wentWell()));
        d.setDistractions(StudySessionService.blankToNull(r.distractions()));
        d.setImproveTomorrow(StudySessionService.blankToNull(r.improveTomorrow()));
        return toDto(repo.save(d));
    }

    private ReflectionDto toDto(DailyReflection d) {
        return new ReflectionDto(d.getDate(), d.getMood(), d.getEnergy(), d.getFocus(), d.getWentWell(),
                d.getDistractions(), d.getImproveTomorrow(), true);
    }
}
