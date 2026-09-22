/** Mirrors the backend DTOs. Durations are always minutes; dates are ISO yyyy-MM-dd. */
export type Category = 'TECH_GK' | 'SQL' | 'PROBLEM_SOLVING' | 'JAVA' | 'AWS' | 'PROJECT_INTERVIEW' | 'DSA'
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type TopicStatus = 'NOT_STARTED' | 'LEARNING' | 'PRACTICING' | 'COMPLETED'
export type JobStatus = 'SAVED' | 'APPLIED' | 'ASSESSMENT' | 'INTERVIEW' | 'HR_ROUND' | 'OFFER' | 'REJECTED' | 'ON_HOLD'
export type ActivityType = 'GYM' | 'WALK' | 'BASKETBALL'
export type WorkoutType = 'PUSH' | 'PULL' | 'LEGS' | 'FULL_BODY' | 'CARDIO' | 'OTHER'

export interface User { id: number; name: string; email: string; wakeTargetTime: string }
export interface AuthResponse { token: string; user: User }

export interface ItemScore {
  routineItemId: number | null; key: string; title: string; type: 'MORNING' | 'TIME' | 'COUNT' | 'CUSTOM'
  targetMinutes: number; actualMinutes: number; targetCount: number; actualCount: number
  ratio: number; completed: boolean; manual: boolean; scheduledTime: string | null
}
export interface DayScore {
  date: string; items: ItemScore[]; overallPercent: number; plannedMinutes: number; completedMinutes: number
  studyMinutes: number; gymMinutes: number; completedTasks: number; remainingTasks: number; missedTasks: number
  problemsSolved: number; jobsApplied: number; awsTopicsCompleted: number
  walked: boolean; basketball: boolean; activeMorning: boolean
}

export interface RoutineItem {
  id: number; key: string; title: string; targetMinutes: number; targetCount: number
  scheduledTime: string | null; active: boolean; sortOrder: number
}
export interface Wake { target: string; actual: string | null; diffMinutes: number | null; completed: boolean }
export interface MorningItem { id: number; title: string; done: boolean }
export interface RoutineDay { date: string; wake: Wake; morning: MorningItem[]; score: DayScore; items: RoutineItem[] }

export interface StudySession {
  id: number; category: Category; date: string; durationMinutes: number
  topic: string | null; source: string | null; notes: string | null; demo: boolean
}

export interface Problem {
  id: number; kind: string; title: string; problemNumber: number | null; url: string | null
  difficulty: Difficulty; topic: string | null; attempts: number; timeMinutes: number
  solved: boolean; date: string; notes: string | null; solution: string | null; demo: boolean
}
export interface ProblemStats {
  attempted: number; solved: number; successRate: number; avgSolveMinutes: number
  solvedToday: number; easyToday: number; mediumToday: number; hardToday: number; lifetimeSolved: number
}
export interface ProblemList { problems: Problem[]; stats: ProblemStats }

export interface Topic { id: number; group: string; name: string; status: TopicStatus; notes: string | null; completedOn: string | null }
export interface TopicList { topics: Topic[]; completed: number; total: number; completionPct: number; readinessPct: number }

export interface Job {
  id: number; company: string; title: string; location: string | null; jobUrl: string | null
  dateApplied: string; resumeVersion: string | null; referral: string | null; status: JobStatus
  followUpDate: string | null; notes: string | null; demo: boolean
}
export interface JobStats {
  total: number; appliedToday: number; dailyTarget: number; appliedThisWeek: number; weeklyTarget: number
  responses: number; responseRate: number; byStatus: Record<JobStatus, number>; followUpsDue: number
}
export interface JobList { jobs: Job[]; stats: JobStats }

export interface ChecklistItem { field: string; label: string; done: boolean }
export interface Project {
  id: number; name: string; problemStatement: string | null; features: string | null; techStack: string | null
  architecture: string | null; databaseDesign: string | null; apiFlow: string | null; challenges: string | null
  solutions: string | null; deployment: string | null; futureImprovements: string | null
  checklist: ChecklistItem[]; readinessPct: number; demo: boolean
}

export interface Exercise { name: string; sets: number; reps: number; weightKg: number | null }
export interface Fitness {
  id: number; type: ActivityType; date: string; startTime: string | null; endTime: string | null
  durationMinutes: number; workoutType: WorkoutType | null; distanceKm: number | null; steps: number | null
  notes: string | null; exercises: Exercise[]; demo: boolean
}
export interface FitnessSummary {
  weekStart: string; gymSessions: number; gymTarget: number; gymMinutes: number
  walkDays: number; basketballDays: number; activeMornings: number
}

export interface Reflection {
  date: string; mood: number | null; energy: number | null; focus: number | null
  wentWell: string | null; distractions: string | null; improveTomorrow: string | null; exists: boolean
}

export interface Milestone { icon: string; title: string; description: string; achieved: boolean; progress: number; goal: number }
export interface Today {
  name: string; date: string; score: DayScore; streak: number; fitness: FitnessSummary
  dsa: { solvedToday: number; easy: number; medium: number; hard: number; lifetimeSolved: number }
  aws: { completed: number; total: number; percent: number }
  interview: { completed: number; total: number; percent: number }
  jobs: { today: number; target: number; week: number; weekTarget: number }
  milestones: Milestone[]; hasDemoData: boolean; reflectionLogged: boolean
}

export interface CategoryRow { key: string; label: string; targetMinutes: number; actualMinutes: number; completionPct: number }
export interface SolveStats { attempted: number; solved: number; successRate: number; avgSolveMinutes: number }
export interface DayPoint {
  date: string; minutesByCategory: Record<string, number>; studyMinutes: number; gymMinutes: number
  applications: number; problemsSolved: number; completionPct: number
}
export interface Comparison { key: string; label: string; unit: string; lastWeek: number; thisWeek: number; changePct: number | null; note: string }
export interface WeeklyReport {
  weekStart: string; weekEnd: string; hasData: boolean
  overall: {
    plannedMinutes: number; completedMinutes: number; completionPct: number; avgStudyMinutesPerDay: number
    bestDay: string | null; bestDayMinutes: number; longestSessionMinutes: number; streak: number; totalStudyMinutes: number
  }
  learning: CategoryRow[]
  career: { applications: number; applicationTarget: number; responses: number; assessments: number; interviews: number; offers: number; responseRate: number }
  fitness: { gymSessions: number; gymTarget: number; gymMinutes: number; walkDays: number; basketballDays: number; activeMornings: number }
  problemSolving: SolveStats
  dsa: { solved: number; easy: number; medium: number; hard: number; topicsLearned: number; lifetimeSolved: number }
  sql: SolveStats; java: SolveStats
  aws: { topicsCompleted: number; handsOnCompleted: number; totalCompleted: number; totalTopics: number }
  days: DayPoint[]; comparison: Comparison[]
}
export interface Insights {
  hasEnoughData: boolean; message: string | null; summary: string
  wentWell: string[]; needsAttention: string[]; learningPattern: string[]; careerProgress: string[]
  fitnessConsistency: string[]; suggestedFocus: string[]; source: string; generatedAt: string
}
export interface PlanRow { key: string; label: string; targetMinutes: number; targetCount: number }
export interface NextWeekPlan { weekStart: string; saved: boolean; basis: string; rows: PlanRow[] }
export interface MonthPoint { date: string; studyMinutes: number; gymMinutes: number; applications: number; problemsSolved: number; completionPct: number }
export interface MonthlyAnalytics {
  month: string; days: MonthPoint[]; totalStudyMinutes: number; totalGymMinutes: number
  totalApplications: number; totalProblemsSolved: number; activeDays: number; avgCompletionPct: number
}
