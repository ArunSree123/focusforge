package com.focusforge.service;

import com.focusforge.domain.*;
import com.focusforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Loads three weeks of realistic sample activity. Every row is flagged demo=true so it can be
 * removed in one call without touching anything the user entered themselves.
 */
@Service
public class DemoDataService {
    private record P(String title, Integer number, Difficulty difficulty, String topic) {}

    private static final List<P> DSA = List.of(
            new P("Two Sum", 1, Difficulty.EASY, "Hashing"), new P("Best Time to Buy and Sell Stock", 121, Difficulty.EASY, "Arrays"),
            new P("Longest Substring Without Repeating Characters", 3, Difficulty.MEDIUM, "Sliding Window"),
            new P("3Sum", 15, Difficulty.MEDIUM, "Two Pointers"), new P("Binary Search", 704, Difficulty.EASY, "Binary Search"),
            new P("Reverse Linked List", 206, Difficulty.EASY, "Linked List"), new P("Valid Parentheses", 20, Difficulty.EASY, "Stack"),
            new P("Maximum Subarray", 53, Difficulty.MEDIUM, "Dynamic Programming"), new P("Number of Islands", 200, Difficulty.MEDIUM, "Graphs"),
            new P("Binary Tree Level Order Traversal", 102, Difficulty.MEDIUM, "Trees"),
            new P("Kth Largest Element in an Array", 215, Difficulty.MEDIUM, "Heap"), new P("Combination Sum", 39, Difficulty.MEDIUM, "Backtracking"),
            new P("Climbing Stairs", 70, Difficulty.EASY, "Dynamic Programming"), new P("Koko Eating Bananas", 875, Difficulty.MEDIUM, "Binary Search"),
            new P("Trapping Rain Water", 42, Difficulty.HARD, "Two Pointers"), new P("Merge k Sorted Lists", 23, Difficulty.HARD, "Heap"),
            new P("Group Anagrams", 49, Difficulty.MEDIUM, "Hashing"), new P("Container With Most Water", 11, Difficulty.MEDIUM, "Two Pointers"));
    private static final List<P> SQL = List.of(
            new P("Second Highest Salary", 176, Difficulty.MEDIUM, "Subqueries"), new P("Employees Earning More Than Their Managers", 181, Difficulty.EASY, "SELF JOIN"),
            new P("Duplicate Emails", 182, Difficulty.EASY, "GROUP BY"), new P("Department Highest Salary", 184, Difficulty.MEDIUM, "JOIN"),
            new P("Rank Scores", 178, Difficulty.MEDIUM, "Window Functions"), new P("Consecutive Numbers", 180, Difficulty.MEDIUM, "Window Functions"),
            new P("Customers Who Never Order", 183, Difficulty.EASY, "JOIN"), new P("Nth Highest Salary", 177, Difficulty.MEDIUM, "Subqueries"),
            new P("Managers with at Least 5 Direct Reports", 570, Difficulty.MEDIUM, "HAVING"), new P("Trips and Users", 262, Difficulty.HARD, "JOIN"));
    private static final List<P> JAVA = List.of(
            new P("Reverse a string without built-ins", null, Difficulty.EASY, "Strings"), new P("LRU cache with LinkedHashMap", null, Difficulty.MEDIUM, "Collections"),
            new P("Producer-consumer with BlockingQueue", null, Difficulty.HARD, "Multithreading"), new P("Group anagrams using Streams", null, Difficulty.MEDIUM, "Streams"),
            new P("Custom exception hierarchy", null, Difficulty.EASY, "Exception Handling"), new P("Thread-safe singleton", null, Difficulty.MEDIUM, "Multithreading"),
            new P("Generic Stack<T>", null, Difficulty.EASY, "Generics"), new P("Word frequency with HashMap", null, Difficulty.EASY, "Collections"));
    private static final List<P> REASONING = List.of(
            new P("Number series: 2, 6, 12, 20, ?", null, Difficulty.EASY, "Pattern problems"), new P("Seating arrangement puzzle", null, Difficulty.MEDIUM, "Logical reasoning"),
            new P("Trains meeting on a track", null, Difficulty.MEDIUM, "Mathematical reasoning"), new P("Pyramid of stars", null, Difficulty.EASY, "Coding logic"),
            new P("Blood relations puzzle", null, Difficulty.MEDIUM, "Logical reasoning"), new P("Find the odd coin in 2 weighings", null, Difficulty.HARD, "Analytical thinking"),
            new P("Two-egg drop problem", null, Difficulty.HARD, "Interview problems"), new P("Probability of a sum of 7 on two dice", null, Difficulty.EASY, "Mathematical reasoning"));

    private static final String[] COMPANIES = {"Zoho", "Freshworks", "Infosys", "TCS", "Wipro", "Cognizant", "HCLTech", "Amazon",
            "Flipkart", "Swiggy", "PhonePe", "Razorpay", "Thoughtworks", "Accenture", "Capgemini", "Kissflow", "Chargebee", "Postman"};
    private static final String[] ROLES = {"Software Engineer", "Java Developer", "Backend Engineer", "Associate Software Engineer", "Full Stack Developer"};
    private static final String[] PLACES = {"Chennai", "Bengaluru", "Hyderabad", "Remote", "Coimbatore"};
    private static final String[] CHECK_TOPICS = {"Cloud", "AI", "IT industry", "Current affairs", "General knowledge"};

    private final StudySessionRepository sessions;
    private final FitnessSessionRepository fitness;
    private final JobApplicationRepository jobs;
    private final ProblemRepository problems;
    private final TopicProgressRepository topics;
    private final ProjectRepository projects;
    private final DailyRoutineRepository wakeLogs;
    private final MorningItemRepository morningItems;
    private final MorningCheckRepository morningChecks;
    private final RoutineCompletionRepository completions;
    private final DailyReflectionRepository reflections;
    private final UserSetupService setup;
    private final Dates dates;

    public DemoDataService(StudySessionRepository sessions, FitnessSessionRepository fitness, JobApplicationRepository jobs,
                           ProblemRepository problems, TopicProgressRepository topics, ProjectRepository projects,
                           DailyRoutineRepository wakeLogs, MorningItemRepository morningItems,
                           MorningCheckRepository morningChecks, RoutineCompletionRepository completions,
                           DailyReflectionRepository reflections, UserSetupService setup, Dates dates) {
        this.sessions = sessions;
        this.fitness = fitness;
        this.jobs = jobs;
        this.problems = problems;
        this.topics = topics;
        this.projects = projects;
        this.wakeLogs = wakeLogs;
        this.morningItems = morningItems;
        this.morningChecks = morningChecks;
        this.completions = completions;
        this.reflections = reflections;
        this.setup = setup;
        this.dates = dates;
    }

    @Transactional(readOnly = true)
    public boolean hasDemo(Long userId) { return sessions.existsByUserIdAndDemoTrue(userId); }

    /** Removes only demo rows; topics touched by the demo go back to "Not started". */
    @Transactional
    public void purge(Long userId) {
        sessions.deleteByUserIdAndDemoTrue(userId);
        fitness.deleteByUserIdAndDemoTrue(userId);
        jobs.deleteByUserIdAndDemoTrue(userId);
        problems.deleteByUserIdAndDemoTrue(userId);
        projects.deleteByUserIdAndDemoTrue(userId);
        wakeLogs.deleteByUserIdAndDemoTrue(userId);
        morningChecks.deleteByUserIdAndDemoTrue(userId);
        completions.deleteByUserIdAndDemoTrue(userId);
        reflections.deleteByUserIdAndDemoTrue(userId);
        for (TopicProgress t : topics.findByUserIdAndDemoTrue(userId)) {
            t.setStatus(TopicStatus.NOT_STARTED);
            t.setCompletedOn(null);
            t.setDemo(false);
            topics.save(t);
        }
    }

    @Transactional
    public void load(Long userId) {
        purge(userId);
        setup.ensureTopics(userId);
        LocalDate today = dates.today();
        Random rnd = new Random(7);

        List<MorningItem> morning = morningItems.findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(userId);
        int[] cursor = new int[4]; // dsa, sql, java, reasoning pools

        for (int back = 21; back >= 0; back--) {
            LocalDate d = today.minusDays(back);
            boolean isToday = back == 0;
            boolean sunday = d.getDayOfWeek() == DayOfWeek.SUNDAY;

            // Never overwrite something the user logged themselves (unique per user+date).
            boolean hasWake = wakeLogs.findByUserIdAndDate(userId, d).isPresent();
            int wakeOffset = isToday ? 8 : rnd.nextInt(26) - 5;
            if (!hasWake) {
                DailyRoutine wake = new DailyRoutine();
                wake.setUserId(userId);
                wake.setDemo(true);
                wake.setDate(d);
                wake.setWakeTarget(LocalTime.of(6, 0));
                wake.setWakeActual(LocalTime.of(6, 0).plusMinutes(wakeOffset));
                wakeLogs.save(wake);
            }

            for (int i = 0; i < morning.size(); i++) {
                boolean want = isToday ? i < 3 : rnd.nextInt(10) < 8;
                if (want && morningChecks.findByUserIdAndDateAndMorningItemId(userId, d, morning.get(i).getId()).isEmpty()) {
                    MorningCheck c = new MorningCheck();
                    c.setUserId(userId);
                    c.setDemo(true);
                    c.setDate(d);
                    c.setMorningItemId(morning.get(i).getId());
                    morningChecks.save(c);
                }
            }

            List<StudySession> day = new ArrayList<>();
            for (Defaults.RoutineDefault def : Defaults.ROUTINE) {
                if (def.key() == RoutineKey.GYM || def.key() == RoutineKey.JOBS) continue;
                double f = sunday ? 0.3 + 0.4 * rnd.nextDouble() : 0.6 + 0.5 * rnd.nextDouble();
                if (def.key() == RoutineKey.AWS && rnd.nextInt(5) == 0) f = 0;
                if (isToday) f = switch (def.key()) {
                    case TECH_GK, SQL -> 1.0;
                    case PROBLEM_SOLVING -> 0.5;
                    case JAVA -> 0.4;
                    default -> 0;
                };
                int minutes = (int) (Math.round(def.minutes() * f / 5.0) * 5);
                if (minutes <= 0) continue;
                boolean split = minutes >= 90 && rnd.nextBoolean();
                int first = split ? minutes / 2 : minutes;
                day.add(session(userId, d, def.key(), first, rnd));
                if (split) day.add(session(userId, d, def.key(), minutes - first, rnd));
            }
            sessions.saveAll(day);

            if (!isToday && !sunday && rnd.nextInt(10) < 8) fitness.save(gym(userId, d, rnd));
            if (rnd.nextInt(10) < 7) fitness.save(simple(userId, d, ActivityType.WALK, 25 + rnd.nextInt(30), rnd));
            if (rnd.nextInt(10) < 4) fitness.save(simple(userId, d, ActivityType.BASKETBALL, 45 + rnd.nextInt(30), rnd));

            int applications = isToday ? 7 : 5 + rnd.nextInt(6);
            List<JobApplication> apps = new ArrayList<>();
            for (int i = 0; i < applications; i++) apps.add(job(userId, d, back, rnd));
            jobs.saveAll(apps);

            cursor[0] = problemsForDay(userId, d, DSA, cursor[0], isToday ? 1 : 1 + rnd.nextInt(3), ProblemKind.DSA, rnd);
            cursor[1] = problemsForDay(userId, d, SQL, cursor[1], isToday ? 1 : rnd.nextInt(3), ProblemKind.SQL, rnd);
            cursor[2] = problemsForDay(userId, d, JAVA, cursor[2], isToday ? 0 : rnd.nextInt(3), ProblemKind.JAVA, rnd);
            cursor[3] = problemsForDay(userId, d, REASONING, cursor[3], isToday ? 1 : 1 + rnd.nextInt(3), ProblemKind.REASONING, rnd);

            if (back >= 1 && back <= 3 && reflections.findByUserIdAndDate(userId, d).isEmpty()) reflections.save(reflection(userId, d, rnd));
        }

        markTopics(userId, TopicArea.AWS, new int[]{10, 2, 1}, today);
        markTopics(userId, TopicArea.DSA, new int[]{6, 3, 2}, today);
        markTopics(userId, TopicArea.SQL, new int[]{7, 2, 1}, today);
        markTopics(userId, TopicArea.JAVA, new int[]{8, 3, 2}, today);
        markTopics(userId, TopicArea.INTERVIEW, new int[]{2, 3, 3}, today);
        projects.saveAll(demoProjects(userId));
    }

    private StudySession session(Long userId, LocalDate d, RoutineKey key, int minutes, Random rnd) {
        StudySession s = new StudySession();
        s.setUserId(userId);
        s.setDemo(true);
        s.setDate(d);
        s.setCategory(StudyCategory.valueOf(key.name()));
        s.setDurationMinutes(Math.max(minutes, 5));
        s.setTopic(switch (key) {
            case JAVA -> pick(rnd, "Collections", "Streams", "Multithreading", "Generics", "JVM internals");
            case SQL -> pick(rnd, "Joins", "Window functions", "Subqueries", "Indexes", "Transactions");
            case AWS -> pick(rnd, "IAM policies", "S3 buckets", "EC2 basics", "Lambda", "API Gateway");
            case DSA -> pick(rnd, "Sliding window", "Trees", "Graphs", "Dynamic programming", "Heaps");
            case TECH_GK -> pick(rnd, CHECK_TOPICS);
            case PROBLEM_SOLVING -> pick(rnd, "Pattern problems", "Logical reasoning", "Coding logic");
            default -> pick(rnd, "Spring Boot", "REST APIs", "Project walkthrough", "HR questions");
        });
        if (key == RoutineKey.TECH_GK) s.setSource(pick(rnd, "Hacker News", "The Hindu", "InfoQ", "AWS Blog"));
        return s;
    }

    private FitnessSession gym(Long userId, LocalDate d, Random rnd) {
        FitnessSession f = new FitnessSession();
        f.setUserId(userId);
        f.setDemo(true);
        f.setType(ActivityType.GYM);
        f.setDate(d);
        f.setStartTime(LocalTime.of(18, 0));
        int minutes = 90 + rnd.nextInt(41);
        f.setEndTime(LocalTime.of(18, 0).plusMinutes(minutes));
        f.setDurationMinutes(minutes);
        WorkoutType wt = WorkoutType.values()[rnd.nextInt(3)]; // push, pull, legs
        f.setWorkoutType(wt);
        String[][] exercises = switch (wt) {
            case PUSH -> new String[][]{{"Bench press", "4", "8", "60"}, {"Overhead press", "3", "10", "35"}, {"Triceps pushdown", "3", "12", "25"}};
            case PULL -> new String[][]{{"Lat pulldown", "4", "10", "50"}, {"Barbell row", "3", "8", "50"}, {"Biceps curl", "3", "12", "15"}};
            default -> new String[][]{{"Squat", "4", "8", "70"}, {"Romanian deadlift", "3", "10", "60"}, {"Leg press", "3", "12", "120"}};
        };
        int order = 0;
        for (String[] e : exercises) {
            Exercise ex = new Exercise();
            ex.setSession(f);
            ex.setName(e[0]);
            ex.setSets(Integer.parseInt(e[1]));
            ex.setReps(Integer.parseInt(e[2]));
            ex.setWeightKg(new BigDecimal(e[3]));
            ex.setSortOrder(order++);
            f.getExercises().add(ex);
        }
        return f;
    }

    private FitnessSession simple(Long userId, LocalDate d, ActivityType type, int minutes, Random rnd) {
        FitnessSession f = new FitnessSession();
        f.setUserId(userId);
        f.setDemo(true);
        f.setType(type);
        f.setDate(d);
        f.setDurationMinutes(minutes);
        if (type == ActivityType.WALK) {
            f.setDistanceKm(BigDecimal.valueOf(minutes / 12.0).setScale(1, java.math.RoundingMode.HALF_UP));
            f.setSteps(minutes * 105);
        }
        return f;
    }

    private JobApplication job(Long userId, LocalDate d, int back, Random rnd) {
        JobApplication j = new JobApplication();
        j.setUserId(userId);
        j.setDemo(true);
        j.setCompany(pick(rnd, COMPANIES));
        j.setTitle(pick(rnd, ROLES));
        j.setLocation(pick(rnd, PLACES));
        j.setDateApplied(d);
        j.setResumeVersion(rnd.nextBoolean() ? "v3-backend" : "v2-java");
        j.setStatus(JobStatus.APPLIED);
        if (back > 2) {
            int roll = rnd.nextInt(100);
            if (roll < 14) j.setStatus(JobStatus.REJECTED);
            else if (roll < 21) j.setStatus(JobStatus.ASSESSMENT);
            else if (roll < 25) j.setStatus(JobStatus.INTERVIEW);
            else if (roll < 26) j.setStatus(JobStatus.HR_ROUND);
            else if (roll < 30) j.setStatus(JobStatus.ON_HOLD);
        }
        if (j.getStatus() == JobStatus.APPLIED && back <= 7) j.setFollowUpDate(d.plusDays(7));
        return j;
    }

    private int problemsForDay(Long userId, LocalDate d, List<P> pool, int cursor, int count, ProblemKind kind, Random rnd) {
        for (int i = 0; i < count; i++) {
            P p = pool.get(cursor++ % pool.size());
            Problem pr = new Problem();
            pr.setUserId(userId);
            pr.setDemo(true);
            pr.setKind(kind);
            pr.setTitle(p.title());
            pr.setProblemNumber(p.number());
            if (kind == ProblemKind.DSA) pr.setUrl("https://leetcode.com/problems/" + p.title().toLowerCase().replaceAll("[^a-z0-9]+", "-") + "/");
            pr.setDifficulty(p.difficulty());
            pr.setTopic(p.topic());
            pr.setAttempts(1 + rnd.nextInt(3));
            boolean solved = rnd.nextInt(100) < 78;
            pr.setSolved(solved);
            int base = switch (p.difficulty()) { case EASY -> 12; case MEDIUM -> 28; case HARD -> 50; };
            pr.setTimeMinutes(base + rnd.nextInt(base));
            pr.setDate(d);
            problems.save(pr);
        }
        return cursor;
    }

    private DailyReflection reflection(Long userId, LocalDate d, Random rnd) {
        DailyReflection r = new DailyReflection();
        r.setUserId(userId);
        r.setDemo(true);
        r.setDate(d);
        r.setMood(3 + rnd.nextInt(3));
        r.setEnergy(3 + rnd.nextInt(3));
        r.setFocus(3 + rnd.nextInt(3));
        r.setWentWell("Finished the planned Java block without breaks.");
        r.setDistractions("Phone notifications in the afternoon.");
        r.setImproveTomorrow("Start DSA earlier, before lunch.");
        return r;
    }

    /** counts = {completed, learning, practicing}; the rest stay untouched. */
    private void markTopics(Long userId, TopicArea area, int[] counts, LocalDate today) {
        List<TopicProgress> all = topics.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, area).stream()
                .filter(t -> !TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())).toList();
        int i = 0;
        for (TopicProgress t : all) {
            if (i < counts[0]) {
                t.setStatus(TopicStatus.COMPLETED);
                t.setCompletedOn(today.minusDays((long) (all.size() - i) * 2 % 21));
            } else if (i < counts[0] + counts[1]) {
                t.setStatus(TopicStatus.LEARNING);
            } else if (i < counts[0] + counts[1] + counts[2]) {
                t.setStatus(TopicStatus.PRACTICING);
            } else break;
            t.setDemo(true);
            topics.save(t);
            i++;
        }
        if (area == TopicArea.AWS) {
            topics.findByUserIdAndAreaOrderBySortOrderAscIdAsc(userId, area).stream()
                    .filter(t -> TopicCatalog.HANDS_ON_GROUP.equals(t.getGroup())).limit(2).forEach(t -> {
                        t.setStatus(TopicStatus.COMPLETED);
                        t.setCompletedOn(today.minusDays(3));
                        t.setDemo(true);
                        topics.save(t);
                    });
        }
    }

    private List<Project> demoProjects(Long userId) {
        Project a = new Project();
        a.setUserId(userId);
        a.setDemo(true);
        a.setName("Library Management System");
        a.setProblemStatement("Small libraries track books on paper, which causes lost records and overdue confusion.");
        a.setFeatures("Book catalogue, member management, issue/return, overdue fines, search.");
        a.setTechStack("Java 21, Spring Boot, PostgreSQL, React");
        a.setArchitecture("Layered REST API: controller, service, repository; React SPA consumes JSON.");
        a.setDatabaseDesign("books, members, loans (member_id, book_id, issued_on, due_on, returned_on).");
        a.setApiFlow("POST /api/loans checks availability, creates the loan, decrements copies in one transaction.");
        a.setChallenges("Preventing two members from borrowing the last copy at the same time.");
        a.setSolutions("Optimistic locking with a version column on the books table.");

        Project b = new Project();
        b.setUserId(userId);
        b.setDemo(true);
        b.setName("Expense Tracker API");
        b.setProblemStatement("Track personal expenses by category and see monthly summaries.");
        b.setTechStack("Spring Boot, MySQL");
        b.setFeatures("Add expenses, categories, monthly totals.");
        return List.of(a, b);
    }

    private static String pick(Random rnd, String... options) { return options[rnd.nextInt(options.length)]; }
}
