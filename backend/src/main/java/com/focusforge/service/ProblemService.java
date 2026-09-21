package com.focusforge.service;

import com.focusforge.common.NotFoundException;
import com.focusforge.domain.Difficulty;
import com.focusforge.domain.Problem;
import com.focusforge.domain.ProblemKind;
import com.focusforge.dto.ProblemDtos.*;
import com.focusforge.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProblemService {
    private final ProblemRepository repo;
    private final Dates dates;

    public ProblemService(ProblemRepository repo, Dates dates) {
        this.repo = repo;
        this.dates = dates;
    }

    /** Maps the URL segment to a problem kind. */
    public static ProblemKind kindFor(String area) {
        return switch (area.toLowerCase()) {
            case "dsa" -> ProblemKind.DSA;
            case "sql" -> ProblemKind.SQL;
            case "java" -> ProblemKind.JAVA;
            case "problem-solving" -> ProblemKind.REASONING;
            default -> throw new NotFoundException("Unknown problem area: " + area);
        };
    }

    @Transactional(readOnly = true)
    public ProblemListDto list(Long userId, String area) {
        ProblemKind kind = kindFor(area);
        List<Problem> all = repo.findByUserIdAndKindOrderByDateDescIdDesc(userId, kind);
        return new ProblemListDto(all.stream().map(this::toDto).toList(), stats(userId, kind, all));
    }

    public ProblemDto create(Long userId, String area, ProblemRequest req) {
        Problem p = new Problem();
        p.setUserId(userId);
        p.setKind(kindFor(area));
        apply(p, req);
        return toDto(repo.save(p));
    }

    public ProblemDto update(Long userId, String area, Long id, ProblemRequest req) {
        Problem p = find(userId, area, id);
        apply(p, req);
        return toDto(repo.save(p));
    }

    public void delete(Long userId, String area, Long id) {
        repo.delete(find(userId, area, id));
    }

    private Problem find(Long userId, String area, Long id) {
        ProblemKind kind = kindFor(area);
        return repo.findByIdAndUserId(id, userId).filter(p -> p.getKind() == kind)
                .orElseThrow(() -> new NotFoundException("Problem not found"));
    }

    private ProblemStats stats(Long userId, ProblemKind kind, List<Problem> all) {
        int attempted = all.size();
        int solved = (int) all.stream().filter(Problem::isSolved).count();
        double avg = all.stream().filter(p -> p.isSolved() && p.getTimeMinutes() > 0)
                .mapToInt(Problem::getTimeMinutes).average().orElse(0);
        var today = dates.today();
        List<Problem> solvedToday = all.stream().filter(p -> p.isSolved() && today.equals(p.getDate())).toList();
        return new ProblemStats(attempted, solved, JobMetrics.pct(solved, attempted), (int) Math.round(avg),
                solvedToday.size(), countDifficulty(solvedToday, Difficulty.EASY), countDifficulty(solvedToday, Difficulty.MEDIUM),
                countDifficulty(solvedToday, Difficulty.HARD), repo.countByUserIdAndKindAndSolvedTrue(userId, kind));
    }

    private int countDifficulty(List<Problem> list, Difficulty d) {
        return (int) list.stream().filter(p -> p.getDifficulty() == d).count();
    }

    private void apply(Problem p, ProblemRequest r) {
        p.setTitle(r.title().trim());
        p.setProblemNumber(r.problemNumber());
        p.setUrl(StudySessionService.blankToNull(r.url()));
        p.setDifficulty(r.difficulty());
        p.setTopic(StudySessionService.blankToNull(r.topic()));
        p.setAttempts(Math.max(1, r.attempts()));
        p.setTimeMinutes(r.timeMinutes());
        p.setSolved(r.solved());
        p.setDate(dates.orToday(r.date()));
        p.setNotes(StudySessionService.blankToNull(r.notes()));
        p.setSolution(StudySessionService.blankToNull(r.solution()));
    }

    private ProblemDto toDto(Problem p) {
        return new ProblemDto(p.getId(), p.getKind().name(), p.getTitle(), p.getProblemNumber(), p.getUrl(), p.getDifficulty(),
                p.getTopic(), p.getAttempts(), p.getTimeMinutes(), p.isSolved(), p.getDate(), p.getNotes(), p.getSolution(), p.isDemo());
    }
}
