package com.focusforge.service;

import com.focusforge.common.NotFoundException;
import com.focusforge.domain.StudySession;
import com.focusforge.dto.StudyDtos.*;
import com.focusforge.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class StudySessionService {
    private final StudySessionRepository repo;
    private final Dates dates;

    public StudySessionService(StudySessionRepository repo, Dates dates) {
        this.repo = repo;
        this.dates = dates;
    }

    @Transactional(readOnly = true)
    public List<StudySessionDto> list(Long userId, LocalDate from, LocalDate to) {
        LocalDate end = dates.orToday(to);
        LocalDate start = from != null ? from : end.minusDays(29);
        return repo.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId, start, end).stream().map(this::toDto).toList();
    }

    public StudySessionDto create(Long userId, StudySessionRequest req) {
        StudySession s = new StudySession();
        s.setUserId(userId);
        apply(s, req);
        return toDto(repo.save(s));
    }

    public StudySessionDto update(Long userId, Long id, StudySessionRequest req) {
        StudySession s = repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Study session not found"));
        apply(s, req);
        return toDto(repo.save(s));
    }

    public void delete(Long userId, Long id) {
        repo.delete(repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Study session not found")));
    }

    private void apply(StudySession s, StudySessionRequest r) {
        s.setCategory(r.category());
        s.setDate(dates.orToday(r.date()));
        s.setDurationMinutes(r.durationMinutes());
        s.setTopic(blankToNull(r.topic()));
        s.setSource(blankToNull(r.source()));
        s.setNotes(blankToNull(r.notes()));
    }

    static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }

    private StudySessionDto toDto(StudySession s) {
        return new StudySessionDto(s.getId(), s.getCategory(), s.getDate(), s.getDurationMinutes(), s.getTopic(),
                s.getSource(), s.getNotes(), s.isDemo());
    }
}
