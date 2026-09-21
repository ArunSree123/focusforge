package com.focusforge.dto;

import com.focusforge.domain.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class JobDtos {
    private JobDtos() {}

    public record JobRequest(
            @NotBlank(message = "Company is required") @Size(max = 150) String company,
            @NotBlank(message = "Job title is required") @Size(max = 150) String title,
            @Size(max = 150) String location,
            @Size(max = 500) String jobUrl,
            @NotNull(message = "Date applied is required") LocalDate dateApplied,
            @Size(max = 100) String resumeVersion,
            @Size(max = 150) String referral,
            @NotNull(message = "Choose a status") JobStatus status,
            LocalDate followUpDate,
            @Size(max = 2000) String notes) {}

    public record JobDto(Long id, String company, String title, String location, String jobUrl, LocalDate dateApplied,
                         String resumeVersion, String referral, JobStatus status, LocalDate followUpDate,
                         String notes, boolean demo) {}

    public record JobStats(int total, int appliedToday, int dailyTarget, int appliedThisWeek, int weeklyTarget,
                           int responses, double responseRate, Map<JobStatus, Integer> byStatus, int followUpsDue) {}

    public record JobListDto(List<JobDto> jobs, JobStats stats) {}
}
