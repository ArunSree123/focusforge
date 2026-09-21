package com.focusforge.service;

import com.focusforge.common.NotFoundException;
import com.focusforge.domain.JobApplication;
import com.focusforge.domain.JobStatus;
import com.focusforge.domain.RoutineKey;
import com.focusforge.dto.JobDtos.*;
import com.focusforge.repository.JobApplicationRepository;
import com.focusforge.repository.RoutineItemRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class JobService {
    private final JobApplicationRepository repo;
    private final RoutineItemRepository routine;
    private final Dates dates;

    public JobService(JobApplicationRepository repo, RoutineItemRepository routine, Dates dates) {
        this.repo = repo;
        this.routine = routine;
        this.dates = dates;
    }

    /** Filters are all optional; text filters match case-insensitively on part of the value. */
    @Transactional(readOnly = true)
    public JobListDto list(Long userId, String company, String role, JobStatus status, String location,
                           LocalDate from, LocalDate to) {
        Specification<JobApplication> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("userId"), userId));
            if (company != null && !company.isBlank()) p.add(cb.like(cb.lower(root.get("company")), like(company)));
            if (role != null && !role.isBlank()) p.add(cb.like(cb.lower(root.get("title")), like(role)));
            if (location != null && !location.isBlank()) p.add(cb.like(cb.lower(root.get("location")), like(location)));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("dateApplied"), from));
            if (to != null) p.add(cb.lessThanOrEqualTo(root.get("dateApplied"), to));
            return cb.and(p.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(Sort.Direction.DESC, "dateApplied").and(Sort.by(Sort.Direction.DESC, "id"));
        List<JobDto> jobs = repo.findAll(spec, sort).stream().map(this::toDto).toList();
        return new JobListDto(jobs, stats(userId));
    }

    public JobDto create(Long userId, JobRequest req) {
        JobApplication j = new JobApplication();
        j.setUserId(userId);
        apply(j, req);
        return toDto(repo.save(j));
    }

    public JobDto update(Long userId, Long id, JobRequest req) {
        JobApplication j = repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Job application not found"));
        apply(j, req);
        return toDto(repo.save(j));
    }

    public void delete(Long userId, Long id) {
        repo.delete(repo.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Job application not found")));
    }

    @Transactional(readOnly = true)
    public JobStats stats(Long userId) {
        LocalDate today = dates.today();
        LocalDate weekStart = dates.weekStart(today);
        List<JobApplication> all = repo.findAll((root, q, cb) -> cb.equal(root.get("userId"), userId));
        int dailyTarget = routine.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId).stream()
                .filter(r -> r.getRoutineKey() == RoutineKey.JOBS).mapToInt(r -> r.getTargetCount()).findFirst().orElse(10);

        Map<JobStatus, Integer> byStatus = new EnumMap<>(JobStatus.class);
        for (JobStatus s : JobStatus.values()) byStatus.put(s, 0);
        int applied = 0, responses = 0, today0 = 0, week = 0, due = 0;
        for (JobApplication j : all) {
            byStatus.merge(j.getStatus(), 1, Integer::sum);
            if (!JobMetrics.isApplied(j.getStatus())) continue;
            applied++;
            if (JobMetrics.isResponse(j.getStatus())) responses++;
            if (j.getDateApplied().equals(today)) today0++;
            if (!j.getDateApplied().isBefore(weekStart) && !j.getDateApplied().isAfter(today)) week++;
            boolean open = j.getStatus() != JobStatus.REJECTED && j.getStatus() != JobStatus.OFFER;
            if (open && j.getFollowUpDate() != null && !j.getFollowUpDate().isAfter(today)) due++;
        }
        return new JobStats(all.size(), today0, dailyTarget, week, dailyTarget * 7, responses,
                JobMetrics.pct(responses, applied), byStatus, due);
    }

    private static String like(String v) { return "%" + v.trim().toLowerCase() + "%"; }

    private void apply(JobApplication j, JobRequest r) {
        j.setCompany(r.company().trim());
        j.setTitle(r.title().trim());
        j.setLocation(StudySessionService.blankToNull(r.location()));
        j.setJobUrl(StudySessionService.blankToNull(r.jobUrl()));
        j.setDateApplied(r.dateApplied());
        j.setResumeVersion(StudySessionService.blankToNull(r.resumeVersion()));
        j.setReferral(StudySessionService.blankToNull(r.referral()));
        j.setStatus(r.status());
        j.setFollowUpDate(r.followUpDate());
        j.setNotes(StudySessionService.blankToNull(r.notes()));
    }

    private JobDto toDto(JobApplication j) {
        return new JobDto(j.getId(), j.getCompany(), j.getTitle(), j.getLocation(), j.getJobUrl(), j.getDateApplied(),
                j.getResumeVersion(), j.getReferral(), j.getStatus(), j.getFollowUpDate(), j.getNotes(), j.isDemo());
    }
}
