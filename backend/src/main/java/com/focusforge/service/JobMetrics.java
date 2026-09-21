package com.focusforge.service;

import com.focusforge.domain.JobStatus;

import java.util.Set;

public final class JobMetrics {
    private JobMetrics() {}

    private static final Set<JobStatus> RESPONSES =
            Set.of(JobStatus.ASSESSMENT, JobStatus.INTERVIEW, JobStatus.HR_ROUND, JobStatus.OFFER, JobStatus.REJECTED);

    /** Anything except a saved (not yet submitted) posting counts as an application. */
    public static boolean isApplied(JobStatus s) { return s != JobStatus.SAVED; }

    /** A response is any status that shows the employer replied, including a rejection. */
    public static boolean isResponse(JobStatus s) { return RESPONSES.contains(s); }

    public static double pct(int part, int whole) {
        return whole <= 0 ? 0 : Math.round(part * 1000.0 / whole) / 10.0;
    }
}
