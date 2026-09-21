package com.focusforge.service;

import com.focusforge.domain.*;
import com.focusforge.dto.DashboardDtos.*;
import com.focusforge.dto.JobDtos.JobStats;
import com.focusforge.dto.ScoreDtos.DayScore;
import com.focusforge.dto.TopicDtos.TopicListDto;
import com.focusforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final AuthService auth;
    private final ScoreService scores;
    private final FitnessService fitness;
    private final JobService jobs;
    private final TopicService topicService;
    private final TopicProgressRepository topics;
    private final ProblemRepository problems;
    private final FitnessSessionRepository fitnessRepo;
    private final JobApplicationRepository jobRepo;
    private final StudySessionRepository sessions;
    private final DailyReflectionRepository reflections;
    private final Dates dates;

    public DashboardService(AuthService auth, ScoreService scores, FitnessService fitness, JobService jobs,
                            TopicService topicService, TopicProgressRepository topics, ProblemRepository problems,
                            FitnessSessionRepository fitnessRepo, JobApplicationRepository jobRepo,
                            StudySessionRepository sessions, DailyReflectionRepository reflections, Dates dates) {
        this.auth = auth;
        this.scores = scores;
        this.fitness = fitness;
        this.jobs = jobs;
        this.topicService = topicService;
        this.topics = topics;
        this.problems = problems;
        this.fitnessRepo = fitnessRepo;
        this.jobRepo = jobRepo;
        this.sessions = sessions;
        this.reflections = reflections;
        this.dates = dates;
    }

    public TodayDto today(Long userId) {
        LocalDate today = dates.today();
        AppUser user = auth.find(userId);
        DayScore score = scores.day(userId, today);
        int streak = scores.streak(userId, today);

        List<Problem> todays = problems.findByUserIdAndDateBetween(userId, today, today).stream()
                .filter(p -> p.getKind() == ProblemKind.DSA && p.isSolved()).toList();
        long dsaLifetime = problems.countByUserIdAndKindAndSolvedTrue(userId, ProblemKind.DSA);
        DsaToday dsa = new DsaToday(todays.size(), diff(todays, Difficulty.EASY), diff(todays, Difficulty.MEDIUM),
                diff(todays, Difficulty.HARD), dsaLifetime);

        TopicListDto aws = topicService.summarize(topics.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, TopicArea.AWS));
        TopicListDto interview = topicService.summarize(topics.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, TopicArea.INTERVIEW));

        JobStats js = jobs.stats(userId);
        JobDay jobDay = new JobDay(js.appliedToday(), js.dailyTarget(), js.appliedThisWeek(), js.weeklyTarget());

        long gymCount = fitnessRepo.countByUserIdAndType(userId, ActivityType.GYM);
        long applied = jobRepo.countByUserIdAndStatusNot(userId, JobStatus.SAVED);
        List<TopicProgress> awsTopics = topics.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, TopicArea.AWS).stream()
                .filter(t -> "Fundamentals".equals(t.getGroup())).toList();
        long fundamentalsDone = awsTopics.stream().filter(t -> t.getStatus() == TopicStatus.COMPLETED).count();

        List<Milestone> milestones = List.of(
                new Milestone("flame", "7-day consistency", "Stay above 50% completion for seven days in a row",
                        streak >= 7, streak, 7),
                new Milestone("target", "100 DSA problems", "Solve 100 DSA problems",
                        dsaLifetime >= 100, dsaLifetime, 100),
                new Milestone("cloud", "AWS fundamentals", "Complete every AWS fundamentals topic",
                        !awsTopics.isEmpty() && fundamentalsDone == awsTopics.size(), fundamentalsDone, awsTopics.size()),
                new Milestone("briefcase", "100 job applications", "Submit 100 applications",
                        applied >= 100, applied, 100),
                new Milestone("dumbbell", "20 gym sessions", "Log 20 gym sessions",
                        gymCount >= 20, gymCount, 20));

        return new TodayDto(user.getName(), today, score, streak, fitness.summary(userId, today), dsa,
                new TopicSummary(aws.completed(), aws.total(), aws.completionPct()),
                new TopicSummary(interview.completed(), interview.total(), interview.readinessPct()),
                jobDay, milestones, sessions.existsByUserIdAndDemoTrue(userId),
                reflections.findByUserIdAndDate(userId, today).isPresent());
    }

    private int diff(List<Problem> list, Difficulty d) {
        return (int) list.stream().filter(p -> p.getDifficulty() == d).count();
    }
}
